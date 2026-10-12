package net.azisaba.lgw.core.syogo;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Bukkit-facing state is changed on the server thread; immutable profiles can be read by chat threads. */
public final class SyogoManager implements Listener {
    private final LeonGunWar plugin;
    private final SyogoStore store;
    private final Map<UUID, SyogoStore.Snapshot> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Player> loading = new HashMap<>();
    private final Map<UUID, Long> revisions = new HashMap<>();
    private final Set<UUID> writing = new HashSet<>();
    private long lastWarning;

    public SyogoManager(LeonGunWar plugin, SyogoStore store) { this.plugin = plugin; this.store = store; }
    public SyogoStore store() { return store; }
    public SyogoStore.Snapshot snapshot(Player player) {
        return cache.getOrDefault(player.getUniqueId(), SyogoStore.Snapshot.empty());
    }
    public String display(Player player) {
        return display(player.getUniqueId());
    }
    /** Read-only, non-blocking lookup for RyuZUPluginChat's asynchronous publish events. */
    public String display(UUID uuid) {
        String id = cache.getOrDefault(uuid, SyogoStore.Snapshot.empty()).activeId();
        var entry = plugin.getSyogoConfig().entries().get(id == null ? "" : id);
        return entry == null ? "" : entry.display() + "&r ";
    }
    @EventHandler public void join(PlayerJoinEvent event) { load(event.getPlayer(), ignored -> { }); }
    @EventHandler public void quit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cache.remove(uuid);
        loading.remove(uuid);
        revisions.remove(uuid);
    }
    public void refreshOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) load(player, ignored -> { });
    }
    public void load(Player player, Consumer<Boolean> completion) {
        UUID uuid = player.getUniqueId();
        if (writing.contains(uuid) || loading.containsKey(uuid)) {
            completion.accept(cache.containsKey(uuid));
            return;
        }
        loading.put(uuid, player);
        long revision = revisions.getOrDefault(uuid, 0L);
        store.load(uuid, player.getName()).whenComplete((value, error) -> onMain(() -> {
            if (Bukkit.getPlayer(uuid) != player) return;
            loading.remove(uuid, player);
            if (revision != revisions.getOrDefault(uuid, 0L)) { completion.accept(cache.containsKey(uuid)); return; }
            if (error == null) cache.put(uuid, value);
            else { cache.remove(uuid); warn(error); }
            completion.accept(error == null);
        }));
    }
    public void change(UUID uuid, Supplier<CompletableFuture<SyogoStore.Result>> operation,
                       Consumer<SyogoStore.Result> completion) {
        if (!writing.add(uuid)) { completion.accept(null); return; }
        revisions.merge(uuid, 1L, Long::sum);
        Player session = Bukkit.getPlayer(uuid);
        operation.get().whenComplete((result, error) -> onMain(() -> {
            writing.remove(uuid);
            if (session != null && Bukkit.getPlayer(uuid) == session) {
                if (error == null) cache.put(uuid, result.snapshot());
                else cache.remove(uuid);
            }
            if (error != null) warn(error);
            completion.accept(error == null ? result : null);
        }));
    }
    public void select(Player player, String id, Consumer<SyogoStore.Result> completion) {
        if (!plugin.getMainConfig().isLobby || (id != null && !plugin.getSyogoConfig().entries().containsKey(id))) {
            completion.accept(null); return;
        }
        change(player.getUniqueId(), () -> store.select(player.getUniqueId(), player.getName(), id), completion);
    }
    public void onMain(Runnable task) {
        if (plugin.isEnabled()) Bukkit.getScheduler().runTask(plugin, task);
    }
    private void warn(Throwable error) {
        long now = System.currentTimeMillis();
        if (now - lastWarning < 30_000) return;
        lastWarning = now;
        plugin.getLogger().warning("Syogo database operation failed: " + error);
    }
}

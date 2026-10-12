package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public final class KillActionManager implements Listener {
    private final LeonGunWar plugin;
    private final KillActionCatalog catalog;
    private final KillActionStore store;
    private final KillActionTicketStore tickets;
    private final Map<UUID, Set<KillActionTicketStore.Owned>> owned = new HashMap<>();
    private record Profile(KillActionStore.Selection selection, Set<KillActionTicketStore.Owned> owned) { }
    private final KillActionEffects effects;
    private final Map<UUID, KillActionStore.Selection> selections = new HashMap<>();
    private final Map<UUID, Player> loading = new HashMap<>();
    private final Map<UUID, Long> revisions = new HashMap<>();
    private final Set<UUID> writing = new HashSet<>();
    private long lastLoadWarning;
    private final Set<UUID> lobbyEffectEnabled = new HashSet<>();

    public KillActionManager(LeonGunWar plugin, KillActionCatalog catalog, KillActionStore store, KillActionTicketStore tickets) {
        this.plugin = plugin;
        this.catalog = catalog;
        this.store = store;
        this.tickets = tickets;
        this.effects = new KillActionEffects(plugin, catalog);
    }

    public KillActionCatalog catalog() { return catalog; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) { load(event.getPlayer(), ignored -> { }); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        selections.remove(uuid);
        owned.remove(uuid);
        loading.remove(uuid);
        revisions.remove(uuid);
        lobbyEffectEnabled.remove(uuid);
    }

    public boolean toggleLobbyEffect(Player player) {
        UUID uuid = player.getUniqueId();
        if (lobbyEffectEnabled.remove(uuid)) return false;
        lobbyEffectEnabled.add(uuid);
        return true;
    }

    public boolean isLobbyEffectEnabled(Player player) {
        return player.hasPermission("leongunwar.command.leongunwaradmin")
                && lobbyEffectEnabled.contains(player.getUniqueId());
    }

    public boolean isUnlocked(Player player, KillActionCatalog.Entry entry) {
        return player.isOp() || owned.getOrDefault(player.getUniqueId(), Set.of())
                .contains(new KillActionTicketStore.Owned(entry.category(), entry.id()))
                || !entry.permission().isBlank() && player.hasPermission(entry.permission());
    }

    public void load(Player player, Consumer<Boolean> completion) {
        refresh(player, completion);
    }

    public void refreshOnline() {
        for (Player player : Bukkit.getOnlinePlayers()) refresh(player, ignored -> { });
    }

    private void refresh(Player player, Consumer<Boolean> completion) {
        UUID uuid = player.getUniqueId();
        if (writing.contains(uuid) || loading.containsKey(uuid)) { completion.accept(selections.containsKey(uuid)); return; }
        loading.put(uuid, player);
        long revision = revisions.getOrDefault(uuid, 0L);
        store.load(uuid).thenCombine(tickets.loadOwned(uuid), Profile::new).whenComplete((profile, error) -> {
            if (!plugin.isEnabled()) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (Bukkit.getPlayer(uuid) != player) return;
                loading.remove(uuid, player);
                if (revision != revisions.getOrDefault(uuid, 0L)) { completion.accept(selections.containsKey(uuid)); return; }
                if (error == null) {
                    selections.put(uuid, profile.selection());
                    owned.put(uuid, profile.owned());
                }
                else {
                    selections.remove(uuid);
                    owned.remove(uuid);
                    long now = System.currentTimeMillis();
                    if (now - lastLoadWarning > 30_000) {
                        lastLoadWarning = now;
                        plugin.getLogger().warning("KillAction load for " + uuid + " failed: " + error.getMessage());
                    }
                }
                completion.accept(error == null);
            });
        });
    }

    public void onMain(Runnable task) {
        if (plugin.isEnabled()) Bukkit.getScheduler().runTask(plugin, task);
    }

    public void redeem(Player player, UUID token, KillActionCatalog.Entry entry, Consumer<KillActionTicketStore.Result> completion) {
        UUID uuid = player.getUniqueId();
        if (!plugin.getMainConfig().isLobby || !writing.add(uuid)) { completion.accept(null); return; }
        revisions.merge(uuid, 1L, Long::sum);
        tickets.redeem(uuid, token, entry).whenComplete((result, error) -> onMain(() -> {
            writing.remove(uuid);
            if (Bukkit.getPlayer(uuid) != player) { completion.accept(null); return; }
            if (error == null) owned.put(uuid, result.owned());
            else {
                owned.remove(uuid);
                plugin.getLogger().warning("KillAction ticket for " + uuid + " failed: " + error);
            }
            completion.accept(error == null ? result : null);
        }));
    }

    public String selectedId(Player player, KillActionCatalog.Category category) {
        KillActionStore.Selection selection = selections.get(player.getUniqueId());
        return selection == null ? null : selection.get(category);
    }

    public KillActionCatalog.Entry active(Player player, KillActionCatalog.Category category) {
        KillActionCatalog.Entry entry = catalog.get(category, selectedId(player, category));
        return entry != null && isUnlocked(player, entry) ? entry : null;
    }

    public void select(Player player, KillActionCatalog.Category category, String id, Consumer<Boolean> completion) {
        if (!plugin.getMainConfig().isLobby) { completion.accept(false); return; }
        if (id != null) {
            KillActionCatalog.Entry entry = catalog.get(category, id);
            if (entry == null || !isUnlocked(player, entry)) { completion.accept(false); return; }
        }
        UUID uuid = player.getUniqueId();
        if (!writing.add(uuid)) { completion.accept(false); return; }
        revisions.merge(uuid, 1L, Long::sum);
        store.save(uuid, category, id).whenComplete((selection, error) -> {
            if (!plugin.isEnabled()) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                writing.remove(uuid);
                if (Bukkit.getPlayer(uuid) != player) { completion.accept(false); return; }
                if (error == null) selections.put(uuid, selection);
                else selections.remove(uuid);
                if (error != null) plugin.getLogger().warning("KillAction save for " + uuid + " failed: " + error.getMessage());
                completion.accept(error == null);
            });
        });
    }

    public String formatFor(Player killer) {
        KillActionCatalog.Entry entry = active(killer, KillActionCatalog.Category.LOG);
        return entry == null ? plugin.getKillLogsConfig().getFormat(killer) : entry.format();
    }

    public boolean hasCustomSound(Player killer) {
        return active(killer, KillActionCatalog.Category.SOUND) != null;
    }

    public void playDefaultKillSound(Player killer, LivingEntity victim) {
        effects.playKillSound(killer, victim.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
    }

    public void playKillEffects(Player killer, LivingEntity victim) {
        Location death = victim.getLocation();
        KillActionCatalog.Entry sound = active(killer, KillActionCatalog.Category.SOUND);
        if (sound != null) effects.playSounds(killer, death, sound, null);
        KillActionCatalog.Entry particle = active(killer, KillActionCatalog.Category.PARTICLE);
        if (particle != null) effects.playParticles(killer, death, particle, null);
    }

    public void preview(Player player, KillActionCatalog.Entry entry) {
        if (!isUnlocked(player, entry)) return;
        switch (entry.category()) {
            case SOUND -> effects.playSounds(player, player.getLocation(), entry, player);
            case PARTICLE -> effects.playParticles(player, player.getLocation(), entry, player);
            case LOG -> {
                String example = entry.format()
                        .replace("{prefix}", LeonGunWar.GAME_PREFIX)
                        .replace("{syogo}", plugin.getSyogoManager().display(player))
                        .replace("{killer}", player.getPlayerListName())
                        .replace("{weapon}", "サンプル武器")
                        .replace("{victim}", "サンプル相手");
                player.sendMessage(AdventureUtil.legacy(ChatColor.translateAlternateColorCodes('&', example)));
            }
        }
    }
}

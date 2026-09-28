package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.AdventureUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class KillActionManager implements Listener {
    private final LeonGunWar plugin;
    private final KillActionCatalog catalog;
    private final KillActionStore store;
    private final KillActionEffects effects;
    private final Map<UUID, KillActionStore.Selection> selections = new HashMap<>();

    public KillActionManager(LeonGunWar plugin, KillActionCatalog catalog, KillActionStore store) {
        this.plugin = plugin;
        this.catalog = catalog;
        this.store = store;
        this.effects = new KillActionEffects(plugin, catalog);
    }

    public KillActionCatalog catalog() { return catalog; }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) { load(event.getPlayer(), ignored -> { }); }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) { selections.remove(event.getPlayer().getUniqueId()); }

    public void load(Player player, Consumer<Boolean> completion) {
        UUID uuid = player.getUniqueId();
        if (selections.containsKey(uuid)) { completion.accept(true); return; }
        store.load(uuid).whenComplete((selection, error) -> {
            if (!plugin.isEnabled()) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!player.isOnline()) return;
                if (error == null) selections.put(uuid, selection);
                else plugin.getLogger().warning("KillAction load for " + uuid + " failed: " + error.getMessage());
                completion.accept(error == null);
            });
        });
    }

    public String selectedId(Player player, KillActionCatalog.Category category) {
        KillActionStore.Selection selection = selections.get(player.getUniqueId());
        return selection == null ? null : selection.get(category);
    }

    public KillActionCatalog.Entry active(Player player, KillActionCatalog.Category category) {
        KillActionCatalog.Entry entry = catalog.get(category, selectedId(player, category));
        return entry != null && player.hasPermission(entry.permission()) ? entry : null;
    }

    public void select(Player player, KillActionCatalog.Category category, String id, Consumer<Boolean> completion) {
        if (id != null) {
            KillActionCatalog.Entry entry = catalog.get(category, id);
            if (entry == null || !player.hasPermission(entry.permission())) { completion.accept(false); return; }
        }
        UUID uuid = player.getUniqueId();
        store.save(uuid, category, id).whenComplete((selection, error) -> {
            if (!plugin.isEnabled()) return;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (error == null && player.isOnline()) selections.put(uuid, selection);
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

    public void playKillEffects(Player killer, Player victim) {
        Location death = victim.getLocation();
        KillActionCatalog.Entry sound = active(killer, KillActionCatalog.Category.SOUND);
        if (sound != null) effects.playSounds(killer, death, sound, null);
        KillActionCatalog.Entry particle = active(killer, KillActionCatalog.Category.PARTICLE);
        if (particle != null) effects.playParticles(killer, death, particle, null);
    }

    public void preview(Player player, KillActionCatalog.Entry entry) {
        if (!player.hasPermission(entry.permission())) return;
        switch (entry.category()) {
            case SOUND -> effects.playSounds(player, player.getLocation(), entry, player);
            case PARTICLE -> effects.playParticles(player, player.getLocation(), entry, player);
            case LOG -> {
                String example = entry.format()
                        .replace("{prefix}", LeonGunWar.GAME_PREFIX)
                        .replace("{syogo}", "")
                        .replace("{killer}", player.getPlayerListName())
                        .replace("{weapon}", "サンプル武器")
                        .replace("{victim}", "サンプル相手");
                player.sendMessage(AdventureUtil.legacy(ChatColor.translateAlternateColorCodes('&', example)));
            }
        }
    }
}

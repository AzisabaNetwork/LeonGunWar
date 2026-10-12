package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.AdventureUtil;
import net.azisaba.lgw.core.util.KillLogUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

/** Lets an OP try their selected kill actions in the lobby without changing match statistics. */
public final class LobbyKillActionListener implements Listener {
    private final LeonGunWar plugin;
    private final KillActionManager manager;

    public LobbyKillActionListener(LeonGunWar plugin, KillActionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer == victim || !canPlay(killer)) return;

        Component log = selectedLog(killer, victim.getPlayerListName());
        if (log != null) {
            event.deathMessage(null);
            broadcast(victim, log.hoverEvent(HoverEvent.showText(KillLogUtils.createDistanceText(killer, victim))));
        }
        manager.playKillEffects(killer, victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMobDeath(EntityDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player) return;
        Player killer = victim.getKiller();
        if (killer == null || !canPlay(killer)) return;

        String name = victim.getCustomName() != null ? victim.getCustomName() : victim.getType().name();
        Component log = selectedLog(killer, name);
        if (log != null) broadcast(victim, log);
        manager.playKillEffects(killer, victim);
    }

    private boolean canPlay(Player killer) {
        return plugin.getMainConfig().isLobby && !plugin.getManager().isMatching()
                && manager.isLobbyEffectEnabled(killer);
    }

    private Component selectedLog(Player killer, String victimName) {
        KillActionCatalog.Entry entry = manager.active(killer, KillActionCatalog.Category.LOG);
        if (entry == null) return null;

        ItemStack item = killer.getInventory().getItemInMainHand();
        String weapon = item.getType() == Material.AIR ? "&6素手"
                : item.hasItemMeta() && item.getItemMeta().hasDisplayName()
                ? item.getItemMeta().getDisplayName() : item.getType().name();
        String syogo = plugin.getSyogoManager().display(killer);
        String text = entry.format()
                .replace("{prefix}", LeonGunWar.GAME_PREFIX)
                .replace("{syogo}", syogo)
                .replace("{killer}", killer.getPlayerListName())
                .replace("{weapon}", weapon)
                .replace("{victim}", victimName);
        return AdventureUtil.legacy(ChatColor.translateAlternateColorCodes('&', text));
    }

    private static void broadcast(LivingEntity victim, Component message) {
        victim.getWorld().getPlayers().forEach(player -> player.sendMessage(message));
        Bukkit.getConsoleSender().sendMessage(message);
    }
}

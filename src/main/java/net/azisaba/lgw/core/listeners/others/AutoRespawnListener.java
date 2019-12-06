package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.PlayPlayerHealingAnimationTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class AutoRespawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player deader = e.getEntity();

        // リスポーン
        Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), () -> deader.spigot().respawn(), 0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        // 消火！！
        Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), () -> p.setFireTicks(0), 0);

        e.setRespawnLocation(LeonGunWar.getPlugin().getManager().getRespawnLocation(p));

        new PlayPlayerHealingAnimationTask(p).runTaskLater(LeonGunWar.getPlugin(), 0);
    }
}

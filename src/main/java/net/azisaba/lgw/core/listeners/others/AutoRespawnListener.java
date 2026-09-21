package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.PlayPlayerHealingAnimationTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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

        Bukkit.getScheduler().runTask(LeonGunWar.getPlugin(), new Runnable() {
            @Override
            public void run() {
                // リスポーン
                deader.spigot().respawn();
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        Location location = LeonGunWar.getPlugin().getManager().getRespawnLocation(p);
        if (location != null && location.getWorld() != null) {
            e.setRespawnLocation(location);
            Location expectedLocation = location.clone();
            Bukkit.getScheduler().runTask(LeonGunWar.getPlugin(), () -> {
                if (LeonGunWar.getPlugin().getManager().isPlayerMatching(p)
                        && p.getWorld() != expectedLocation.getWorld()) {
                    p.teleport(expectedLocation);
                }
            });
        }

        // 消火！！
        Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), () -> p.setFireTicks(0), 0);
        //　エフェクト！！
        new PlayPlayerHealingAnimationTask(p).runTaskLater(LeonGunWar.getPlugin(), 0);
    }
}

package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.PlayPlayerHealingAnimationTask;

public class AutoRespawnListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player deader = e.getEntity();

        // リスポーン
        deader.spigot().respawn();
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        e.setRespawnLocation(LeonGunWar.getPlugin().getManager().getRespawnLocation(p));

        new PlayPlayerHealingAnimationTask(p).runTaskLater(LeonGunWar.getPlugin(), 0);
        new BukkitRunnable() {
            @Override
            public void run() {
                p.sendMessage("あなたの満腹度: " + p.getFoodLevel() + "、あなたのHP: " + p.getHealth());
            }
        }.runTaskLater(LeonGunWar.getPlugin(), 5 * 20);
    }
}

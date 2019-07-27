package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import net.azisaba.lgw.core.LeonGunWar;

/**
 * ノックバックを無効化するためのクラス
 *
 * @author siloneco
 *
 */
public class NoKnockbackListener implements Listener {

    /**
     * プレイヤーがノックバックしたときにキャンセルするリスナー
     */
    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent e) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), () -> e.setVelocity(new Vector()), 0);
    }
}

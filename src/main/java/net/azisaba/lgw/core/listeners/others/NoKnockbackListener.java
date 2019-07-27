package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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
     * プレイヤーが爆発でノックバックしたときにキャンセルするリスナー
     */
    @EventHandler
    public void onExplosionKnockback(EntityDamageByEntityEvent e) {
        if ( e.getEntity() instanceof Player ) {
            Player p = (Player) e.getEntity();
            Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), () -> p.setVelocity(new Vector()), 0);
        }
    }

    /**
     * プレイヤーがノックバックしたときにキャンセルするリスナー
     */
    @EventHandler
    public void onKnockback(PlayerVelocityEvent e) {
        e.setCancelled(true);
    }
}

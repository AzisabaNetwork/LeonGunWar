package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

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
    public void onKnockback(PlayerVelocityEvent e) {
        e.setCancelled(true);
    }

    /**
     * プレイヤーが爆発でノックバックしたときにキャンセルするリスナー
     */
    @EventHandler
    public void onExplosionKnockback(EntityDamageByEntityEvent e) {
        if ( e.getEntity() instanceof Player ) {
            Player p = (Player) e.getEntity();
            if ( e.getDamager() instanceof TNTPrimed ) {
                e.setCancelled(true);
                p.setVelocity(new Vector());

                TNTPrimed tnt = (TNTPrimed) e.getDamager();
                if ( !tnt.hasMetadata("CS_pName") ) {
                    return;
                }
                String shooterName = tnt.getMetadata("CS_pName").get(0).asString();
                Player shooter = Bukkit.getPlayerExact(shooterName);
                if ( shooter != null ) {
                    p.damage(e.getDamage(), shooter);
                }
            }
        }
    }
}

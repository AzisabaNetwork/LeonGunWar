package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.util.Vector;

import net.azisaba.lgw.core.utils.Damage;

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
        Player p = e.getPlayer();
        EntityDamageEvent damage = p.getLastDamageCause();
        if ( damage != null && damage.getCause().toString().endsWith("_EXPLOSION") ) {
            e.setVelocity(new Vector());
        } else {
            e.setCancelled(true);
        }
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
                    Damage.damageNaturally(p, e.getDamage(), p == shooter ? null : shooter);
                }
            }
        }
    }
}

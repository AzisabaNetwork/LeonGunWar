package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
    public void onPlayerVelocity(PlayerVelocityEvent e) {
        Player p = e.getPlayer();
        e.setCancelled(true);

        EntityDamageEvent damage = p.getLastDamageCause();
        if ( damage != null && damage.getCause() != DamageCause.PROJECTILE ) {
            e.setVelocity(new Vector());
        }
    }
}

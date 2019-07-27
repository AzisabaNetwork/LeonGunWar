package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;
import java.util.List;

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

    private final List<DamageCause> useCancel = Arrays.asList(DamageCause.BLOCK_EXPLOSION, DamageCause.PROJECTILE);

    /**
     * プレイヤーがノックバックしたときにキャンセルするリスナー
     */
    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent e) {
        Player p = e.getPlayer();
        e.setVelocity(new Vector());
        EntityDamageEvent damage = p.getLastDamageCause();
        if ( damage != null && useCancel.contains(damage.getCause()) ) {
            e.setCancelled(true);
        }
    }
}

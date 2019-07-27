package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
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
    public void onKnockback(PlayerVelocityEvent e) {
        e.setCancelled(true);

        Player p = e.getPlayer();
        EntityDamageEvent damage = p.getLastDamageCause();
        if ( damage != null && damage.getCause() == DamageCause.ENTITY_EXPLOSION ) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), () -> p.setVelocity(new Vector()), 0);
        }
    }
}

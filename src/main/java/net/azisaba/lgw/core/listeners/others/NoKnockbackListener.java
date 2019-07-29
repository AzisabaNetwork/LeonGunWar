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
                // TNTを爆発させない
                e.setCancelled(true);
                // ノックバックを削除
                p.setVelocity(new Vector());

                TNTPrimed tnt = (TNTPrimed) e.getDamager();
                if ( !tnt.hasMetadata("CS_pName") ) {
                    return;
                }

                // CrackShotからTNTの作成者を取得
                String shooterName = tnt.getMetadata("CS_pName").get(0).asString();
                Player shooter = Bukkit.getPlayerExact(shooterName);

                // 作成者がいる場合
                if ( shooter != null ) {
                    // 作成者の攻撃としてダメージを与える
                    // 作成者が自分の場合は強制的にダメージを与える
                    Damage.damageNaturally(p, e.getDamage(), p == shooter ? null : shooter);

                    // 近くの重複したTNTを削除
                    double range = tnt.getYield();
                    p.getNearbyEntities(range, range, range).stream()
                            .filter(entity -> entity instanceof TNTPrimed)
                            .map(entity -> (TNTPrimed) entity)
                            .filter(duplicate -> duplicate.getFuseTicks() < 10)
                            .forEach(TNTPrimed::remove);
                }
            }
        }
    }
}

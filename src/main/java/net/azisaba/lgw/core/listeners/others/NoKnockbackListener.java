package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerVelocityEvent;

import com.shampaggon.crackshot.CSDirector;

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
        e.setCancelled(true);
    }

    /**
     * プレイヤーが爆発でノックバックしたときにキャンセルするリスナー
     */
    @EventHandler(ignoreCancelled = true)
    public void onExplosionKnockback(EntityDamageByEntityEvent e) {
        if ( e.getEntity() instanceof Player ) {
            Player p = (Player) e.getEntity();

            if ( e.getDamager() instanceof TNTPrimed ) {
                // TNTを爆発させない
                e.setCancelled(true);

                TNTPrimed tnt = (TNTPrimed) e.getDamager();
                double damage = e.getDamage();
                Entity shooter = null;
                CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

                // 攻撃者を設定
                if ( tnt.hasMetadata("CS_pName") ) {
                    // CrackShotからTNTの作成者を取得
                    String shooterName = tnt.getMetadata("CS_pName").get(0).asString();
                    shooter = Bukkit.getPlayerExact(shooterName);

                    // 自分にダメージが当たらないバグを直す
                    if ( shooter == p ) {
                        shooter = null;
                    }
                }

                // ダメージを計算
                if ( tnt.hasMetadata("CS_potex") ) {
                    // 銃の名前を取得
                    String weaponTitle = tnt.getMetadata("CS_potex").get(0).asString();
                    String multiString = cs.getString(weaponTitle + ".Explosions.Damage_Multiplier");

                    // 銃の設定からパーセント計算
                    if ( multiString != null ) {
                        double multiplier = Double.valueOf(multiString) * 0.01;
                        damage *= multiplier;
                    }
                }

                // 作成者の攻撃としてダメージを与える
                // 作成者が自分の場合や、作成者がいない場合は強制的にダメージを与える
                Damage.damageNaturally(p, damage, shooter);
            }
        }
    }
}

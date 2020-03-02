package net.azisaba.lgw.core.listeners.others;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerVelocityEvent;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

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
    public void onExplosionKnockback(EntityExplodeEvent e) {
        if ( e.getEntity() instanceof Explosive ) {
            // TNTを爆発させない
            e.setCancelled(true);

            Explosive explosive = (Explosive) e.getEntity();
            float power = explosive.getYield();
            double radius = 2 * power;

            // パーティクルを表示
            Particle explode = power >= 1 ? power >= 2 ? Particle.EXPLOSION_HUGE : Particle.EXPLOSION_LARGE : Particle.EXPLOSION_NORMAL;
            e.getLocation().getWorld().spawnParticle(explode, e.getLocation(), 1);

            List<Damageable> targets = explosive.getNearbyEntities(radius, radius, radius).stream()
                    .filter(target -> target instanceof Damageable)
                    .map(target -> (Damageable) target)
                    .collect(Collectors.toList());

            for ( Damageable target : targets ) {
                double damage = 20;
                double distance = explosive.getLocation().toVector().distance(target.getLocation().toVector());

                damage *= (radius - distance) / (2 * 4);

                // 障害物が間にある場合は軽減
                Vector period = target.getLocation().toVector().subtract(explosive.getLocation().toVector()).normalize().multiply(0.1);
                Location next = explosive.getLocation().clone();
                int obstacle = 0;
                for ( int i = 0; i < distance * 10; i++ ) {
                    if ( next.getBlock().getType().isSolid() ) {
                        obstacle++;
                    }
                    next.add(period);
                }
                damage = Math.max(1, damage * (1 - obstacle * 0.061));

                Player shooter = null;
                CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

                // 攻撃者を設定
                if ( explosive.hasMetadata("CS_pName") ) {
                    // CrackShotからTNTの作成者を取得
                    String shooterName = explosive.getMetadata("CS_pName").get(0).asString();
                    shooter = Bukkit.getPlayerExact(shooterName);

                    // 自分にダメージが当たらないバグを直す
                    if ( shooter == target ) {
                        shooter = null;
                    }
                }

                // ダメージを計算
                if ( explosive.hasMetadata("CS_potex") ) {
                    // 銃の名前を取得
                    String weaponTitle = explosive.getMetadata("CS_potex").get(0).asString();
                    String multiString = cs.getString(weaponTitle + ".Explosions.Damage_Multiplier");

                    // 銃の設定からパーセント計算
                    if ( multiString != null ) {
                        double multiplier = Double.parseDouble(multiString) * 0.01;
                        damage *= multiplier;
                    }
                }

                // 防具のダメージを計算する
                if ( target instanceof LivingEntity ) {
                    LivingEntity entity = (LivingEntity) target;
                    double toughness = entity.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getValue();
                    double defensePoints = entity.getAttribute(Attribute.GENERIC_ARMOR).getValue();
                    damage *= 1 - Math.min(20, Math.max(defensePoints / 5, defensePoints - damage / (2 + toughness / 4))) / 25;
                }

                // ターゲットが無敵の場合はダメージを無くす
                if ( target.isInvulnerable() ) {
                    damage = 0;
                }

                // 攻撃者とターゲットが同じチームかつ、フレンドリーファイヤーが許可されていない場合はダメージを無くす
                if ( shooter != null ) {

                    // どこにも参加していない場合はダメージを無効化する
                    if ( target instanceof Player && getJoiningTeam((Player) target).size() <= 0 ) {
                        damage = 0;
                    } else {
                        for ( Team team : shooter.getScoreboard().getTeams() ) {
                            if ( team.allowFriendlyFire() ) {
                                continue;
                            }

                            if ( team.hasEntry(shooter.getName()) && team.hasEntry(target.getName()) ) {
                                damage = 0;
                            }
                        }
                    }
                }

                // 作成者の攻撃としてダメージを与える
                // 作成者が自分の場合や、作成者がいない場合は強制的にダメージを与える
                target.damage(damage);

                @SuppressWarnings("deprecation")
                EntityDamageByEntityEvent cause = new EntityDamageByEntityEvent(shooter, target, DamageCause.ENTITY_EXPLOSION, damage);
                target.setLastDamageCause(cause);
            }
        }
    }

    @EventHandler
    public void onExplosionDamage(EntityDamageByEntityEvent e) {
        if ( e.getDamager() instanceof Explosive ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeaponArmorStandKnockback(WeaponDamageEntityEvent e) {
        Entity victim = e.getVictim();
        if ( victim instanceof ArmorStand ) {
            victim.setVelocity(new Vector());
        }
    }

    private List<Team> getJoiningTeam(Player p) {
        return p.getScoreboard().getTeams().stream()
                .filter(team -> team.hasEntry(p.getName()))
                .collect(Collectors.toList());
    }
}

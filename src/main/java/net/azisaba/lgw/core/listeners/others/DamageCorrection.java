package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.util.PlayerStats;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.WeaponData;

public class DamageCorrection implements Listener {

    HashMap<UUID,Long> rendame = new HashMap<>();
    HashMap<UUID,Long> hitgomuteki = new HashMap<>();

    @EventHandler(ignoreCancelled = true,priority = EventPriority.NORMAL)
    public void onDamage(WeaponDamageEntityEvent e){

        //e.getPlayer().sendMessage("ダメージ: " + e.getDamage());

        if(hitgomuteki.containsKey(e.getVictim().getUniqueId())){

            if(e.getDamager() == null) {
                if ( hitgomuteki.get(e.getVictim().getUniqueId()) > System.currentTimeMillis() ) {

                    //e.getPlayer().sendMessage("ヒット後無敵: " + e.getDamage() + " → 0");

                    e.setDamage(0);
                    e.setCancelled(true);
                    return;

                }
            }

        }

        if(rendame.containsKey(e.getVictim().getUniqueId())){

            if(rendame.get(e.getVictim().getUniqueId()) > System.currentTimeMillis()){

                double before = e.getDamage();
                double after;

                WeaponData data = LeonGunWar.getPlugin().getWeaponConfig().getWeaponData(e.getWeaponTitle());

                if(data != null){

                    after = sisyagonyuu(e.getDamage() * data.getRenzokuDamageRatio());

                    //e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",before,after));
                    e.setDamage(after);

                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + data.getRenzokuDamageLong());

                    if(data.getHitgoMuteki() > 0) {
                        hitgomuteki.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + data.getHitgoMuteki());
                    }

                    return;

                }else if(e.getDamager() == null){

                    after = sisyagonyuu(e.getDamage() * 0.5);

                    /*
                    long sa = rendame.get(e.getVictim().getUniqueId()) - System.currentTimeMillis();

                    if(sa < 400){
                        after = sisyagonyuu(before * 0.7);
                    }else if(sa < 600){
                        after = sisyagonyuu(before * 0.5);
                    }else if(sa < 800){
                        after = sisyagonyuu(before / 3);
                    }else {
                        after = before;
                    }

                     */

                    //e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",before,after));
                    e.setDamage(after);

                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

                    //hitgomuteki.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + getHitGoMutekiTime(after));

                }else if(e.getDamager() instanceof TNTPrimed ){

                    after = sisyagonyuu(e.getDamage() * 0.6);

                    //e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",before,after));
                    e.setDamage(after);
                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

                }else if(e.getDamager() instanceof Projectile ){

                    after = sisyagonyuu(e.getDamage() * 0.8);

                    //e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",before,after));
                    e.setDamage(after);
                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 100);

                }

                return;

            }

        }

        WeaponData data = LeonGunWar.getPlugin().getWeaponConfig().getWeaponData(e.getWeaponTitle());

        if(data != null){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + data.getRenzokuDamageLong());

            if(data.getHitgoMuteki() > 0) {
                hitgomuteki.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + data.getHitgoMuteki());
            }

        }else if(e.getDamager() == null){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

            //hitgomuteki.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + getHitGoMutekiTime(e.getDamage()));

        }else if(e.getDamager() instanceof TNTPrimed ){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

        }else if(e.getDamager() instanceof Projectile ){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 100);

        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamageCanceled(EntityDamageByEntityEvent e){

        if(e.isCancelled() && e.getDamager() instanceof Player ){

            e.getDamager().getWorld().playSound(e.getEntity().getLocation(), Sound.UI_BUTTON_CLICK, 1F, 1.63F);
            Location loc = e.getEntity().getLocation();
            loc.setY(loc.getY() + 1);
            e.getDamager().getWorld().spawnParticle(Particle.CRIT,loc,4);

        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){

        if(rendame.containsKey(e.getPlayer().getUniqueId())){
            rendame.remove(e.getPlayer().getUniqueId());
        }

    }

    @EventHandler
    public void onGameEnd(MatchFinishedEvent e){

        rendame.clear();
        hitgomuteki.clear();

    }

    private double sisyagonyuu(double d){

        d = d * 10;

        long l = Math.round(d);

        return (double) l / 10.0D;

    }

    private long getHitGoMutekiTime(double damage){

        if(damage >= 10.0){
            return 400L;
        }else if(damage >= 6.0){
            return 300L;
        }else if(damage >= 3.0){
            return 200L;
        }else {
            return 0L;
        }

    }

}

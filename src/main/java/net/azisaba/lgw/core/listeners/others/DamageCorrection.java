package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

import net.azisaba.lgw.core.utils.Chat;

public class DamageCorrection implements Listener {

    HashMap<UUID,Long> rendame = new HashMap<>();

    @EventHandler
    public void onDamage(WeaponDamageEntityEvent e){

        if(rendame.containsKey(e.getVictim().getUniqueId())){

            if(rendame.get(e.getVictim().getUniqueId()) < System.currentTimeMillis()){

                if(e.getDamager() == null){

                    e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",e.getDamage(),sisyagonyuu(e.getDamage() * 0.6)));
                    e.setDamage(sisyagonyuu(e.getDamage() * 0.6));
                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

                }else if(e.getDamager() instanceof TNTPrimed ){

                    e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",e.getDamage(),sisyagonyuu(e.getDamage() * 0.6)));
                    e.setDamage(sisyagonyuu(e.getDamage() * 0.6));
                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

                }else if(e.getDamager() instanceof Projectile ){

                    e.getPlayer().sendMessage(Chat.f("連ダメ補正: {0} → {1}",e.getDamage(),sisyagonyuu(e.getDamage() * 0.8)));
                    e.setDamage(sisyagonyuu(e.getDamage() * 0.8));
                    rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 100);

                }

                return;

            }

        }

        if(e.getDamager() == null){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

        }else if(e.getDamager() instanceof TNTPrimed ){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 400);

        }else if(e.getDamager() instanceof Projectile ){

            rendame.put(e.getVictim().getUniqueId(),System.currentTimeMillis() + 100);

        }

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){

        if(rendame.containsKey(e.getPlayer().getUniqueId())){
            rendame.remove(e.getPlayer().getUniqueId());
        }

    }

    private double sisyagonyuu(double d){

        d = d * 10;

        long l = Math.round(d);

        return (double) l / 10.0D;

    }

}

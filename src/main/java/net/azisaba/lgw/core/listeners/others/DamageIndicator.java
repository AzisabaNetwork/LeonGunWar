package net.azisaba.lgw.core.listeners.others;

import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

public class DamageIndicator implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onDamage(WeaponDamageEntityEvent e){

        if(e.getVictim().getType().equals(EntityType.SNOWMAN)){

            ArmorStand stand = (ArmorStand) e.getVictim().getWorld().spawnEntity(((LivingEntity) e.getVictim()).getLocation().subtract(0,100,0), EntityType.ARMOR_STAND);
            stand.setGravity(false);
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setCustomNameVisible(true);
            stand.setCustomName(Chat.f("&a" + (int)(e.getDamage() * 100 + new Random().nextInt(10) - 5)));
            stand.setInvulnerable(true);
            stand.teleport(getRandomLoc(((LivingEntity) e.getVictim()).getEyeLocation().subtract(0, 0.5, 0)));

            BukkitRunnable task = new BukkitRunnable() {
                int count = 14;
                @Override
                public void run() {
                    if(count == 0){
                        stand.remove();
                        cancel();
                    }else if(count == 5){
                        stand.setCustomNameVisible(false);
                    }else if(count == 4){
                        stand.setCustomNameVisible(true);
                    }

                    Location loc = stand.getLocation();
                    loc.setY(loc.getY() + 0.05);
                    stand.teleport(loc);

                    count--;
                }
            };task.runTaskTimer(LeonGunWar.getPlugin(),0L,1L);

        }

    }

    private Location getRandomLoc(Location eye){

        Random random = new Random();
        double range = random.nextDouble() / 2 + 0.3;

        double angle = Math.toRadians(random.nextInt(360));
        double x = Math.cos(angle) * range;
        double z = Math.sin(angle) * range;

        Random random2 = new Random();
        double y = random2.nextDouble() / 2;

        eye.setX(eye.getX() + x);
        eye.setZ(eye.getZ() + z);

        eye.setY(eye.getY() + y);

        return eye;

    }

}

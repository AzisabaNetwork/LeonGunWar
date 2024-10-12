package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Wither;
import org.bukkit.scheduler.BukkitRunnable;

public class BossSpawn {
    public void spawnWither(BattleTeam team,GameMap map){
        Location location = map.getBossSpawnPoint(team);
        Wither wither = location.getWorld().spawn(location, Wither.class);
        wither.setHealth(3000);
        wither.setAI(false);
        wither.setGravity(false);
        wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.addPassenger(wither); // ウィザーを防具立てに乗せる
    }
}

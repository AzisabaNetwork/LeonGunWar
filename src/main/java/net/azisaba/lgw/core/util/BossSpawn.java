package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Wither;

public class BossSpawn {
    public void spawnWither(BattleTeam team,GameMap map){
        if(team == BattleTeam.RED){
            if(!LeonGunWar.getPlugin().getManager().isRedBossDeath()){
                return;
            }
        }else{
            if(!LeonGunWar.getPlugin().getManager().isBlueBossDeath()){
                return;
            }
        }
        Location location = map.getBossSpawnPoint(team);
        Wither wither = location.getWorld().spawn(location, Wither.class);
        wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(3000.0);
        wither.setHealth(3000);
        wither.setAI(false);
        wither.setGravity(false);
        wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
        //ウィザーの名前をEngTeamNameに設定 これでウィザーがどっちのチームかを判定
        wither.setCustomName(team.getEngTeamName());
        wither.setCustomNameVisible(false);
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.addPassenger(wither); // ウィザーを防具立てに乗せる

        //ウィザーが死んでいるか死んでいないかのフラグ更新
        LeonGunWar.getPlugin().getManager().setBossDeath(team,false);
    }

    public void reviveWither(BattleTeam team,GameMap map){
        if(team == BattleTeam.RED){
            if(!LeonGunWar.getPlugin().getManager().isRedBossDeath()){
                return;
            }
        }else{
            if(!LeonGunWar.getPlugin().getManager().isBlueBossDeath()){
                return;
            }
        }
        Location location = map.getBossSpawnPoint(team);
        Wither wither = location.getWorld().spawn(location, Wither.class);
        wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).setBaseValue(3000.0);
        wither.setHealth(500);
        wither.setAI(false);
        wither.setGravity(false);
        wither.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0.0);
        //ウィザーの名前をEngTeamNameに設定 これでウィザーがどっちのチームかを判定
        wither.setCustomName(team.getEngTeamName());
        wither.setCustomNameVisible(false);
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setGravity(false);
        stand.addPassenger(wither); // ウィザーを防具立てに乗せる
        //ウィザーが死んでいるか死んでいないかのフラグ更新
        LeonGunWar.getPlugin().getManager().setBossDeath(team,false);
        BroadcastUtils.broadcast(team.getTeamName() + "&6の目標が復活しました!!");
    }
}

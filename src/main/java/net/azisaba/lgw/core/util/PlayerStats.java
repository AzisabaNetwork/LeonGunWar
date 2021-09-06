package net.azisaba.lgw.core.util;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.azisaba.lgw.core.LeonGunWar;

public class PlayerStats {

    private final UUID uuid;
    private String name;
    private int level;
    private int xps;
    private int coins;
    private int wins;
    private int loses;
    private int angleOfDeathLevel;

    private static HashMap<UUID,PlayerStats> cached = new HashMap<>();

    public PlayerStats(UUID uuid, String name, int level, int xps, int coins,int wins, int loses, int angleOfDeathLevel){

        this.uuid = uuid;
        this.name = name;
        this.level = level;
        this.xps = xps;
        this.coins = coins;
        this.wins = wins;
        this.loses = loses;

        this.angleOfDeathLevel = angleOfDeathLevel;
    }

    public static PlayerStats getStats(UUID uuid){

        if(cached.containsKey(uuid)){
            return cached.get(uuid);
        }

        PlayerStats stats = LeonGunWar.getPlugin().getSQLPlayerStats().getStats(uuid);

        if(stats != null){
            cached.put(uuid,stats);
        }

        return stats;

    }

    public static PlayerStats getStats(Player player){

        if(cached.containsKey(player.getUniqueId())){
            return cached.get(player.getUniqueId());
        }

        PlayerStats stats = LeonGunWar.getPlugin().getSQLPlayerStats().getStats(player.getUniqueId());

        if(stats != null){
            cached.put(player.getUniqueId(),stats);
        }

        return stats;

    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    // TODO set〇〇にSQLの処理を追加する。

    public int getLevel() {
        return level;
    }

    public void setLevel(int level){
        this.level = this.level + level;
    }

    public int getXps() {
        return xps;
    }

    public void addXps(int xps){
        this.xps = this.xps + xps;
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int coins){
        this.coins = this.coins + coins;
    }

    public int getAngleOfDeathLevel() {
        return angleOfDeathLevel;
    }

    public void addAngleOfDeathLevel(int angleOfDeathLevel){
        this.angleOfDeathLevel = this.angleOfDeathLevel + angleOfDeathLevel;
    }

    public int getWins() {
        return wins;
    }

    public void addWins(int wins){
        this.wins = this.wins + wins;
    }

    public int getLoses() {
        return loses;
    }

    public void addLoses(int loses){
        this.loses = this.loses + loses;
    }
}

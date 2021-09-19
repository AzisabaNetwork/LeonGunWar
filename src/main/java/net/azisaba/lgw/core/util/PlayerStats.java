package net.azisaba.lgw.core.util;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.LevelingUtils;

import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import jp.azisaba.lgw.kdstatus.utils.TimeUnit;

public class PlayerStats {

    private final UUID uuid;
    private String name;
    private int level;
    private int xps;
    private int coins;
    private int yobi1;
    private int yobi2;
    private int wins;
    private int loses;
    private int angelOfDeathLevel;
    private int yobi3;

    private static HashMap<UUID,PlayerStats> cached = new HashMap<>();

    private final static String line = ChatColor.GREEN + "" + ChatColor.STRIKETHROUGH + "                                                           ";

    public PlayerStats(UUID uuid, String name, int level, int xps, int coins,int yobi1,int yobi2,int wins, int loses, int angelOfDeathLevel,int yobi3){

        this.uuid = uuid;
        this.name = name;
        this.level = level;
        this.xps = xps;
        this.coins = coins;
        this.yobi1 = yobi1;
        this.yobi2 = yobi2;
        this.wins = wins;
        this.loses = loses;
        this.angelOfDeathLevel = angelOfDeathLevel;
        this.yobi3 = yobi3;
    }

    public static void loadStats(Player player){

        PlayerStats stats = LeonGunWar.getPlugin().getSQLPlayerStats().getStats(player.getUniqueId());

        if(stats != null){
            stats.name = player.getName();
            cached.put(player.getUniqueId(),stats);
        }else if(!LeonGunWar.getPlugin().getSQLPlayerStats().exist(player.getUniqueId())){

            stats = new PlayerStats(player.getUniqueId(),player.getName(),1,0,0,0,0,0,0,0,0);

            //キル数に応じてXPを加算
            stats.setXps(KDStatusReloaded.getPlugin().getKdDataContainer().getPlayerData(player,true).getKills(TimeUnit.LIFETIME));

            LeonGunWar.getPlugin().getSQLPlayerStats().create(stats);
            cached.put(player.getUniqueId(),stats);

        }

    }

    public static void unloadStats(UUID uuid,boolean save){

        if(cached.containsKey(uuid)){
            if(save){
                LeonGunWar.getPlugin().getSQLPlayerStats().update(cached.get(uuid));
            }
            cached.remove(uuid);
        }

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

    public void update(){
        LeonGunWar.getPlugin().getSQLPlayerStats().update(this);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    // TODO set〇〇にSQLの処理を追加する。→変更。試合終了時にまとめて更新

    public int getLevel() {
        return level;
    }

    public void addLevel(int level){
        this.level = this.level + level;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getXps() {
        return xps;
    }

    public void addXps(int xps){
        this.xps = this.xps + xps;
        int l = LevelingUtils.getLevelFromXp(this.xps);
        if( l != this.level){

            Player p = Bukkit.getPlayer(uuid);

            if(p != null){
                p.sendMessage(line);
                p.sendMessage(" ");
                p.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "LeonGunWar Level UP!!! " + LevelingUtils.coloring(this.level,"[" + this.level + "]"));
                p.sendMessage(" ");
                p.sendMessage(line);
            }

            setLevel(l);
        }
    }

    public void setXps(int xps){
        this.xps = xps;
        int l = LevelingUtils.getLevelFromXp(this.xps);
        if( l != this.level){
            setLevel(l);
        }
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int coins){
        this.coins = this.coins + coins;
    }

    public int getAngelOfDeathLevel() {
        return angelOfDeathLevel;
    }

    public void addAngelOfDeathLevel(int angelOfDeathLevel){
        this.angelOfDeathLevel = this.angelOfDeathLevel + angelOfDeathLevel;
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

    public int getYobi1() {
        return yobi1;
    }

    public void setYobi1(int yobi1) {
        this.yobi1 = yobi1;
    }

    public int getYobi2() {
        return yobi2;
    }

    public void setYobi2(int yobi2) {
        this.yobi2 = yobi2;
    }

    public int getYobi3() {
        return yobi3;
    }

    public void setYobi3(int yobi3) {
        this.yobi3 = yobi3;
    }
}

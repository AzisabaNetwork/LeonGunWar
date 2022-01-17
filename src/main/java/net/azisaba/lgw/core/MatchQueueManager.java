package net.azisaba.lgw.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MapLoader;
import net.azisaba.lgw.core.util.MatchMode;

public class MatchQueueManager {

    private boolean hasQueue = false;
    private boolean isLoaded = false;
    private String mapName;
    private World queueWorld;
    private MatchMode matchMode;
    private List<Player> inQueue = new ArrayList<>();

    public void createQueue(String mapName,MatchMode matchMode){

        if(hasQueue)
            return;

        hasQueue = true;

        if(Bukkit.getWorld(mapName) != null){
            while ( Bukkit.getWorld(mapName) != null ){
                mapName = LeonGunWar.getPlugin().getMapsConfig().getRandomMap();
            }
        }

        this.mapName = mapName;
        this.matchMode = matchMode;

        MapLoader.loadMap(mapName);

    }

    public void onDisable(){

        if(hasQueue){
            inQueue.clear();

            queueWorld.getPlayers().forEach(p -> p.teleport(Bukkit.getWorlds().get(0).getSpawnLocation()));

            Bukkit.unloadWorld(mapName,false);
        }

    }

    public void addQueuePlayer(Player player){

        if(hasQueue){
            inQueue.add(player);

            LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard(LeonGunWar.getPlugin().getScoreboardDisplayer().queueBordLines(0));

            if(!LeonGunWar.getPlugin().getManager().isMatching() && inQueue.size() >= 2 && !LeonGunWar.getPlugin().getCountdown().isRunning()){

                LeonGunWar.getPlugin().getCountdown().startCountdown();

            }

        }

    }

    public void removeQueuePlayer(Player player){

        if(inQueue.contains(player))
            inQueue.remove(player);

        LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard(LeonGunWar.getPlugin().getScoreboardDisplayer().queueBordLines(0));

        if(!LeonGunWar.getPlugin().getManager().isMatching()){
            if(inQueue.size() < 2){
                LeonGunWar.getPlugin().getCountdown().stopCountdown();
                queueWorld.getPlayers().forEach(p -> p.sendTitle(ChatColor.RED + "CANCELED",ChatColor.GRAY + "人数が足りないため開始できませんでした",0,60,20));
            }
        }

    }

    public void release(){

        if(!hasQueue)
            return;

        queueWorld.setPVP(true);
        queueWorld.setDifficulty(Difficulty.EASY);

        LeonGunWar.getPlugin().getManager().addEntryPlayers(inQueue);

        LeonGunWar.getPlugin().getManager().loadMatchData(matchMode,LeonGunWar.getPlugin().getMapsConfig().getGameMap(mapName));

        LeonGunWar.getPlugin().getManager().startMatch();

        inQueue.clear();
        hasQueue = false;
        isLoaded = false;

    }

    public int getQueueSize(){
        return inQueue.size();
    }

    public boolean isInQueue(Player player){

        return inQueue.contains(player);

    }

    public boolean hasQueue() {
        return hasQueue;
    }

    public void setHasQueue(boolean hasQueue) {
        this.hasQueue = hasQueue;
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    public void setLoaded(boolean loaded) {

        if(queueWorld == null)
            return;

        if(!this.isLoaded && loaded){
            inQueue.forEach(p -> p.teleport(queueWorld.getSpawnLocation()));
        }

        queueWorld.setPVP(false);
        queueWorld.setGameRuleValue("doWeatherCycle","false");
        queueWorld.setGameRuleValue("doDaylightCycle","false");
        queueWorld.setTime(6000L);
        queueWorld.setDifficulty(Difficulty.PEACEFUL);

        isLoaded = loaded;
    }

    public void setWorld(World world){
        this.queueWorld = world;
    }

    public World getQueueWorld(){
        return queueWorld;
    }

    public String getMapName(){
        return mapName;
    }

    public MatchMode getMatchMode() {
        return matchMode;
    }

    public void setMatchMode(MatchMode matchMode) {
        this.matchMode = matchMode;
    }
}

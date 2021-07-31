package net.azisaba.lgw.core;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MapLoader;
import net.azisaba.lgw.core.util.MatchMode;

public class MatchQueueManager {

    private final MatchManager mm;

    private boolean hasQueue = false;
    private boolean isLoaded = false;
    private String mapName;
    private World queueWorld;
    private MatchMode matchMode;
    private List<Player> inQueue = new ArrayList<>();

    public MatchQueueManager(MatchManager matchManager){

        this.mm = matchManager;

    }

    public void createQueue(String mapName,MatchMode matchMode){

        if(hasQueue)
            return;

        hasQueue = true;

        if(LeonGunWar.getPlugin().getManager().getCurrentGameMap().getMapName().equals(mapName)){
            while ( LeonGunWar.getPlugin().getManager().getCurrentGameMap().getMapName().equals(mapName) ){
                mapName = LeonGunWar.getPlugin().getMapsConfig().getRandomMap();
            }
        }

        this.mapName = mapName;
        this.matchMode = matchMode;

        MapLoader.loadMap(mapName);

    }

    public void addQueuePlayer(Player player){

        if(hasQueue){
            inQueue.add(player);

            LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard(player,LeonGunWar.getPlugin().getScoreboardDisplayer().queueBordLines(0));

            if(!LeonGunWar.getPlugin().getManager().isMatching() && inQueue.size() >= 2 && !LeonGunWar.getPlugin().getCountdown().isRunning()){

                LeonGunWar.getPlugin().getCountdown().startCountdown();

            }

        }

    }

    public void removeQueuePlayer(Player player){

        if(inQueue.contains(player))
            inQueue.remove(player);

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

        LeonGunWar.getPlugin().getManager().addEntryPlayers(inQueue);

        LeonGunWar.getPlugin().getManager().loadMatchData(matchMode,LeonGunWar.getPlugin().getMapsConfig().getGameMap(mapName));

        LeonGunWar.getPlugin().getManager().startMatch();

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

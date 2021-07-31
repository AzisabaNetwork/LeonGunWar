package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.WorldLoadEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchQueueManager;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.util.MatchMode;

public class QueueListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){

        Bukkit.getScheduler().scheduleSyncDelayedTask(LeonGunWar.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if(LeonGunWar.getPlugin().getManager().isMatching() && LeonGunWar.getPlugin().getManager().getTimeLeft().get() >= 45){
                    LeonGunWar.getPlugin().getManager().addPlayerIntoBattle(e.getPlayer());
                }else {
                    MatchQueueManager mqm = LeonGunWar.getPlugin().getMatchQueueManager();
                    if(mqm.hasQueue()){
                        if(mqm.isLoaded()){
                            mqm.addQueuePlayer(e.getPlayer());
                            e.getPlayer().teleport(mqm.getQueueWorld().getSpawnLocation());
                        }else {
                            mqm.addQueuePlayer(e.getPlayer());
                            e.getPlayer().sendMessage(ChatColor.RED + "エラーが発生しました。これがバグである場合は、管理者に報告して下さい。(ERROR: Queue is not loaded)");
                        }
                    }else {
                        mqm.addQueuePlayer(e.getPlayer());
                        e.getPlayer().sendMessage(ChatColor.RED + "エラーが発生しました。これがバグである場合は、管理者に報告して下さい。(ERROR: Queue not found)");
                        //mqm.createQueue(LeonGunWar.getPlugin().getMapsConfig().getRandomMap(), MatchMode.LEADER_DEATH_MATCH_POINT);
                    }
                }
            }
        },1L);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){

        MatchQueueManager mqm = LeonGunWar.getPlugin().getMatchQueueManager();

        if(mqm.hasQueue() && mqm.isInQueue(e.getPlayer())){
            mqm.removeQueuePlayer(e.getPlayer());
        }

    }

    /*
    @EventHandler
    public void onLoadWorld(WorldLoadEvent e){

        if(e.getWorld().getName().equals(LeonGunWar.getPlugin().getMatchQueueManager().getMapName())){
            LeonGunWar.getPlugin().getMatchQueueManager().setWorld(e.getWorld());
            LeonGunWar.getPlugin().getMatchQueueManager().setLoaded(true);
        }

    }

     */

    @EventHandler
    public void onRemain60Sec(MatchTimeChangedEvent e){

        if(e.getTimeLeft() <= 60){
            MatchQueueManager mqm = LeonGunWar.getPlugin().getMatchQueueManager();
            if(!mqm.hasQueue()){
                mqm.createQueue(LeonGunWar.getPlugin().getMapsConfig().getRandomMap(), MatchMode.LEADER_DEATH_MATCH_POINT);
            }
        }

    }

}

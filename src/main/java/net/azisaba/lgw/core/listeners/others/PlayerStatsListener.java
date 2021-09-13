package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.PlayerStats;

public class PlayerStatsListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){

        Bukkit.getScheduler().runTask(LeonGunWar.getPlugin(), () -> {
            PlayerStats.loadStats(e.getPlayer());
        });

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){

        Bukkit.getScheduler().runTask(LeonGunWar.getPlugin(), () -> {
            PlayerStats.unloadStats(e.getPlayer().getUniqueId()); // FIXME 多分ここエラーになるかも...（第二引数がない）
        });

    }

}

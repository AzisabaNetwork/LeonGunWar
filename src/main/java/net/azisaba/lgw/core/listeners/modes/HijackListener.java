package net.azisaba.lgw.core.listeners.modes;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;

public class HijackListener implements Listener {
    @EventHandler
    public void onTime(MatchTimeChangedEvent e) {
        MatchManager manager = LeonGunWar.getPlugin().getManager();
        BattleTeam team = null;
        rLoop:
        for (Player player : manager.getAllTeamPlayers()) {
            for (Area3D area : manager.getHijackAreas()) {
                if (area.isInArea(player.getLocation().toVector())) {
                    if (team != null && team != manager.getBattleTeam(player)) {
                        team = null;
                        break rLoop;
                    } else {
                        team = manager.getBattleTeam(player);
                        continue rLoop;
                    }
                }
            }
        }

        // if there is a single team occupying the area
        if (team != null) {
            manager.addTeamPoint(team);
            if (manager.getCurrentTeamPoint(team) >= 100) {
                BattleTeam finalTeam = team;
                Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> {
                    MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), Arrays.asList(finalTeam),
                            manager.getTeamPlayers());
                    Bukkit.getPluginManager().callEvent(event);
                }, 0L);
            }
        }
    }
}

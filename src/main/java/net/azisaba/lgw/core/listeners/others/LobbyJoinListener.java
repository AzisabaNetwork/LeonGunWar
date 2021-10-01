package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.ScoreboardDisplayer;

public class LobbyJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){

        if( LeonGunWar.getPlugin().isLobby() ){

            e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            ScoreboardDisplayer sd = LeonGunWar.getPlugin().getScoreboardDisplayer();

            sd.updateScoreboard(e.getPlayer(),sd.lobbyBordLines(e.getPlayer()));

        }

    }

}

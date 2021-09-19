package net.azisaba.lgw.core.listeners.others;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.ScoreboardDisplayer;

public class LobbyJoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e){

        if( LeonGunWar.getPlugin().isLobby() ){

            ScoreboardDisplayer sd = LeonGunWar.getPlugin().getScoreboardDisplayer();

            sd.updateScoreboard(e.getPlayer(),sd.lobbyBordLines(e.getPlayer()));

        }

    }

}

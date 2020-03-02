package net.azisaba.lgw.core.listeners.modes;

import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.TeamPointIncreasedEvent;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;

/**
 *
 * TDMの処理をするリスナー
 *
 * @author siloneco
 *
 */
public class TeamDeathMatchListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamPointAdded(TeamPointIncreasedEvent e) {

        // TDMではない場合return
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != MatchMode.TEAM_DEATH_MATCH ) {
            return;
        }

        // 40か45ならメッセージを表示
        if ( e.getCurrentPoint() == 40 || e.getCurrentPoint() == 45 ) {
            Bukkit.broadcastMessage(Chat.f("{0}&7残り &e{1}キル &7で &r{2} &7が勝利！", LeonGunWar.GAME_PREFIX,
                    50 - e.getCurrentPoint(), e.getTeam().getTeamName()));
        } else if ( e.getCurrentPoint() == 50 ) {
            MatchManager manager = LeonGunWar.getPlugin().getManager();

            // 試合終了
            MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), Collections.singletonList(e.getTeam()),
                    manager.getTeamPlayers());
            Bukkit.getPluginManager().callEvent(event);
        }
    }
}

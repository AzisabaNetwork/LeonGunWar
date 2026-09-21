package net.azisaba.lgw.core.listeners.modes;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.BroadcastUtils;
import net.azisaba.lgw.core.util.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Map;

/**
 * LDMの処理を行うListener
 *
 * @author siloneco
 */
public class LeaderDeathMatchListener implements Listener {

    @EventHandler
    public void onLeaderKilledDetector(PlayerDeathEvent e) {
        MatchManager manager = LeonGunWar.getPlugin().getManager();

        // LDMではなければreturn
        if (manager.getMatchMode() == null || !manager.getMatchMode().isLeaderDeathMatch()) {
            return;
        }

        // 死んだプレイヤー
        Player death = e.getEntity();

        // 死んだプレイヤーと殺したプレイヤーが同じ (またはnull) ならreturn
        if (death.getKiller() == null || death == death.getKiller()) {
            return;
        }

        // キルをしたプレイヤー
        Player killer = death.getKiller();
        // キルをしたプレイヤーのチーム
        BattleTeam killerTeam = manager.getBattleTeam(killer);
        BattleTeam victimTeam = manager.getBattleTeam(death);
        if (killerTeam == null || victimTeam == null || killerTeam == victimTeam) {
            return;
        }

        // 各チームのリーダーを取得
        Map<BattleTeam, Player> leaders = manager.getLDMLeaderMap();

        // 死んだプレイヤーがリーダーだった場合、10ボーナスポイントを加えてリーダーを再抽選する
        for (BattleTeam team : leaders.keySet()) {

            // リーダーではない場合continue
            if (leaders.get(team) != death) {
                continue;
            }

            BroadcastUtils.broadcast(Chat.f("{0}{1} &7が {2} &7のリーダーの {3} &7をキル！",
                    LeonGunWar.GAME_PREFIX,
                    killer.getPlayerListName(),
                    team.getTeamName(),
                    death.getPlayerListName()));

            BroadcastUtils.broadcast(Chat.f("{0}{1} &7がリーダー撃破ボーナス &e10ポイント &7を獲得！",
                    LeonGunWar.GAME_PREFIX,
                    killerTeam.getTeamName()));
            manager.addTeamPoint(killerTeam, 10);
            manager.setLeaderAtRandom(team);
            break;
        }
    }
}

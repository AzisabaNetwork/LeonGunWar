package net.azisaba.lgw.core.listeners.modes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.MatchMode;

/**
 *
 * LDMの処理を行うListener
 *
 * @author siloneco
 *
 */
public class LeaderDeathMatchListener implements Listener {

	@EventHandler
	public void onLeaderKilledDetector(PlayerDeathEvent e) {
		MatchManager manager = LeonGunWar.getPlugin().getManager();

		// LDMではなければreturn
		if (manager.getMatchMode() != MatchMode.LEADER_DEATH_MATCH) {
			return;
		}

		// 死んだプレイヤー
		Player death = e.getEntity();

		// 死んだプレイヤーと殺したプレイヤーが同じ (またはnull) ならreturn
		if (death.getKiller() == null || death == death.getKiller()) {
			return;
		}

		// 各チームのリーダーを取得
		Map<BattleTeam, Player> leaders = manager.getLDMLeaderMap();

		// 死んだプレイヤーがリーダーだった場合、試合を終了する
		for (BattleTeam team : leaders.keySet()) {

			// リーダーではない場合continue
			if (leaders.get(team) != death) {
				continue;
			}

			// その他のチームを取得
			List<BattleTeam> teams = new ArrayList<>(leaders.keySet());
			// 殺されたリーダーのチームを削除
			teams.remove(team);

			// このイベントの後にイベント作成、呼び出し
			// 遅らせる理由は最後のキルが表示されないため
			Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), new Runnable() {
				@Override
				public void run() {
					MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), teams,
							manager.getTeamPlayers());
					Bukkit.getPluginManager().callEvent(event);
				}
			}, 0L);
			break;
		}
	}
}

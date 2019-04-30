package net.azisaba.lgw.core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.teams.BattleTeam;

public class MatchControlListener implements Listener {

	private LeonGunWar plugin;

	public MatchControlListener(LeonGunWar plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onMatchFinished(MatchTimeChangedEvent e) {
		// 時間を取得して0じゃなかったらreturn
		if (e.getTimeLeft() > 0) {
			return;
		}

		// チーム作成
		BattleTeam team;

		// 各チームのポイントを取得
		int redPoint = MatchManager.getCurrentTeamPoint(BattleTeam.RED);
		int bluePoint = MatchManager.getCurrentTeamPoint(BattleTeam.BLUE);

		if (redPoint > bluePoint) { // 赤が多い場合
			team = BattleTeam.RED;
		} else if (bluePoint > redPoint) { // 青が多い場合
			team = BattleTeam.BLUE;
		} else { // 同じ場合
			team = BattleTeam.BOTH;
		}

		// イベントを呼び出す
		MatchFinishedEvent event = new MatchFinishedEvent(MatchManager.getCurrentGameMap(), team,
				MatchManager.getTeamPlayers(BattleTeam.RED), MatchManager.getTeamPlayers(BattleTeam.BLUE));
		plugin.getServer().getPluginManager().callEvent(event);
	}
}

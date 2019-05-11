package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.MatchManager.MatchMode;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.PlayerKickMatchEvent;
import net.azisaba.lgw.core.teams.BattleTeam;

public class PlayerControlListener implements Listener {

	/**
	 * 試合中のプレイヤーがサーバーから退出した場合に試合から退出させるリスナー
	 */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		MatchManager manager = LeonGunWar.getPlugin().getManager();
		// プレイヤーが試合中でなければreturn
		if (!manager.isMatching() || manager.getBattleTeam(p) == null) {
			return;
		}

		// 試合から退出
		manager.kickPlayer(p);
	}

	/**
	 * LDMでリーダーが退出した際にゲームを終了させるリスナー
	 */
	@EventHandler
	public void onPlayerKicked(PlayerKickMatchEvent e) {
		Player p = e.getPlayer();
		MatchManager manager = LeonGunWar.getPlugin().getManager();

		// LDMではなかった場合return
		if (manager.getMatchMode() != MatchMode.LEADER_DEATH_MATCH) {
			return;
		}

		// 試合中のプレイヤー取得
		Map<BattleTeam, List<Player>> playerMap = manager.getTeamPlayers();

		// プレイヤーがリーダーだった場合、勝者は無しで試合を終了させる
		for (List<Player> plist : playerMap.values()) {
			if (plist.contains(p)) {
				// イベント作成、呼び出し
				MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(),
						new ArrayList<BattleTeam>(), manager.getTeamPlayers());
				Bukkit.getPluginManager().callEvent(event);
				break;
			}
		}
	}
}

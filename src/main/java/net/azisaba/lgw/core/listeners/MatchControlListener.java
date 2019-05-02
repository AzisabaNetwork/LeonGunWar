package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Strings;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.KillDeathCounter.KDPlayerData;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.ScoreboardDisplayer;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.utils.CustomItem;

public class MatchControlListener implements Listener {

	private LeonGunWar plugin;

	public MatchControlListener(LeonGunWar plugin) {
		this.plugin = plugin;
	}

	/**
	 * 試合が終わったときにMatchFinishedEventを呼び出すリスナー
	 */
	@EventHandler
	public void matchFinishDetector(MatchTimeChangedEvent e) {
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
			team = null;
		}

		// イベントを呼び出す
		MatchFinishedEvent event = new MatchFinishedEvent(MatchManager.getCurrentGameMap(), team,
				MatchManager.getTeamPlayers());
		plugin.getServer().getPluginManager().callEvent(event);
	}

	/**
	 * 試合が終わった時の処理を担当するリスナー
	 */
	@EventHandler
	public void onMatchFinished(MatchFinishedEvent e) {
		// 勝ったチームのプレイヤーリストを取得
		List<Player> winnerPlayers = new ArrayList<>();
		if (e.getWinner() == BattleTeam.RED) {
			winnerPlayers = e.getTeamPlayers(BattleTeam.RED);
		} else if (e.getWinner() == BattleTeam.BLUE) {
			winnerPlayers = e.getTeamPlayers(BattleTeam.BLUE);
		}

		for (Player p : winnerPlayers) {
			// 勝者の証を付与
			p.getInventory().addItem(CustomItem.getWonItem());
		}

		// 試合に参加した全プレイヤーを取得
		List<Player> allPlayers = e.getAllTeamPlayers();

		// MVPのプレイヤーを取得
		List<KDPlayerData> mvpPlayers = MatchManager.getKillDeathCounter().getMVPPlayer();
		// MVPプレイヤーのメッセージ
		List<String> mvpMessages = new ArrayList<String>(Arrays.asList(ChatColor.RED + "MVP:"));
		for (KDPlayerData data : mvpPlayers) {
			mvpMessages.add(
					Strings.repeat(" ", 2) + ChatColor.RED + "- " + ChatColor.AQUA + data.getPlayerName()
							+ ChatColor.RED + ": " + data.getKills() + "k " + data.getDeaths() + "d");
		}

		for (Player p : allPlayers) {
			// スポーンにTP
			p.teleport(MatchManager.getLobbySpawnLocation());

			// アーマー削除
			p.getInventory().setChestplate(null);

			// MVPのプレイヤーを表示
			for (String msg : mvpMessages) {
				p.sendMessage(msg);
			}
		}
	}

	/**
	 * 最後に finalizeMatch メソッドを実行するためのリスナー
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void matchFinalizer(MatchFinishedEvent e) {
		MatchManager.finalizeMatch();
	}

	/**
	 * プレイヤーのスコアボードを更新するためのリスナー
	 */
	@EventHandler
	public void scoreboardUpdater(MatchTimeChangedEvent e) {
		// 試合中のプレイヤーを取得
		List<Player> players = MatchManager.getAllTeamPlayers();

		// スコアボードをアップデート
		ScoreboardDisplayer.updateScoreboard(players);
	}
}

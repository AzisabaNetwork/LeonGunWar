package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.base.Strings;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.KillDeathCounter.KDPlayerData;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.ScoreboardDisplayer;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.utils.CustomItem;

public class MatchControlListener implements Listener {

	private final LeonGunWar plugin;

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
		List<Player> winnerPlayers = e.getTeamPlayers(e.getWinner());

		for (Player p : winnerPlayers) {
			// 勝者の証を付与
			p.getInventory().addItem(CustomItem.getWonItem());
		}

		// 試合に参加した全プレイヤーを取得
		List<Player> allPlayers = e.getAllTeamPlayers();

		// MVPのプレイヤーを取得
		List<KDPlayerData> mvpPlayers = MatchManager.getKillDeathCounter().getMVPPlayer();
		// MVPプレイヤーのメッセージ
		List<String> mvpMessages = new ArrayList<>(Arrays.asList(ChatColor.RED + "MVP:"));

		// MVPが居ない場合は「なし」と表示
		if (mvpPlayers.isEmpty()) {
			mvpMessages.add(Strings.repeat(" ", 2) + ChatColor.RED + "- なし");
		} else { // MVPが居る場合は表示
			for (KDPlayerData data : mvpPlayers) {
				mvpMessages.add(
						Strings.repeat(" ", 2) + ChatColor.RED + "- " + ChatColor.AQUA + data.getPlayerName()
								+ ChatColor.RED + ": " + data.getKills() + "キル " + data.getDeaths() + "デス");
			}
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

	/**
	 * プレイヤーのアクションバーを更新するリスナー
	 */
	@EventHandler
	public void actionbarUpdater(MatchTimeChangedEvent e) {
		// 試合中のプレイヤーを取得
		List<Player> players = MatchManager.getAllTeamPlayers();

		// アクションバーをアップデート
		for (Player p : players) {
			// キル数取得
			int kills = MatchManager.getKillDeathCounter().getKills(p);
			// デス数取得
			int deaths = MatchManager.getKillDeathCounter().getDeaths(p);

			String msg = "";

			// 両方を足して0の場合は白く表示
			if (kills + deaths == 0) {
				msg = "0 kill " + Strings.repeat("┃", 50) + " 0 death";
			} else { // それ以外の場合はメーター作成
				// キルのパーセンテージ
				double killsPercentage = (double) kills / (double) (kills + deaths) * 100d;
				// デスのパーセンテージ
				double deathsPercentage = (double) deaths / (double) (kills + deaths) * 100d;

				msg += ChatColor.LIGHT_PURPLE + Strings.repeat("┃", (int) killsPercentage / 2);
				msg += ChatColor.DARK_PURPLE + Strings.repeat("┃", (int) deathsPercentage / 2);

				// キル数とデス数を数字で表示
				msg = ChatColor.YELLOW + "" + kills + " kill(s) " + msg;
				msg += " " + ChatColor.YELLOW + "" + deaths + " death(s)";
			}

			// アクションバーに表示
			JSONMessage.create(msg).actionbar(p);
		}
	}
}

package net.azisaba.lgw.core.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.google.common.base.Strings;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.KillDeathCounter.KDPlayerData;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.utils.CustomItem;

public class MatchControlListener implements Listener {

	/**
	 * 試合が終わったときにMatchFinishedEventを呼び出すリスナー
	 */
	@EventHandler
	public void matchFinishDetector(MatchTimeChangedEvent e) {
		// 時間を取得して0じゃなかったらreturn
		if (e.getTimeLeft() > 0) {
			return;
		}

		// 各チームのポイントを取得して比較し、一番ポイントが多いチームを取得
		BattleTeam winner = Stream.of(BattleTeam.values())
				.max(Comparator.comparing(LeonGunWar.getPlugin().getManager()::getCurrentTeamPoint))
				.orElse(null);

		// イベントを呼び出す
		MatchFinishedEvent event = new MatchFinishedEvent(LeonGunWar.getPlugin().getManager().getCurrentGameMap(),
				winner,
				LeonGunWar.getPlugin().getManager().getTeamPlayers());
		Bukkit.getPluginManager().callEvent(event);
	}

	/**
	 * 試合が終わった時の処理を担当するリスナー
	 */
	@EventHandler
	public void onMatchFinished(MatchFinishedEvent e) {
		// 勝ったチームがあれば勝者の証を付与
		if (e.getWinner() != null) {
			// チームメンバーを取得
			List<Player> winnerPlayers = e.getTeamPlayers(e.getWinner());

			for (Player p : winnerPlayers) {
				// 勝者の証を付与
				p.getInventory().addItem(CustomItem.getWonItem());
			}
		}

		// 試合に参加した全プレイヤーを取得
		List<Player> allPlayers = e.getAllTeamPlayers();

		// MVPのプレイヤーを取得
		List<KDPlayerData> mvpPlayers = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getMVPPlayer();
		// MVPプレイヤーのメッセージ
		List<String> resultMessages = new ArrayList<>(
				Arrays.asList(ChatColor.LIGHT_PURPLE + "=== Team Point Information ==="));

		// 各チームのポイントを表示
		for (BattleTeam team : BattleTeam.values()) {
			int point = LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(team);
			resultMessages.add(team.getDisplayTeamName() + ChatColor.RED + " " + point + " Point(s)");
		}

		// MVPを表示 (ない場合は何も表示しない)
		if (!mvpPlayers.isEmpty()) {
			for (KDPlayerData data : mvpPlayers) {
				resultMessages
						.add(ChatColor.RED + "[MVP] " + data.getPlayerName() + " " + data.getKills() + " Kill(s), "
								+ data.getDeaths() + " Death(s), " + data.getAssists() + " Assist(s)");
			}
		}

		for (Player p : allPlayers) {
			// スポーンにTP
			p.teleport(LeonGunWar.getPlugin().getManager().getLobbySpawnLocation());

			// アーマー削除
			p.getInventory().setChestplate(null);

			// 結果を表示
			for (String msg : resultMessages) {
				p.sendMessage(msg);
			}

			// 各記録を取得
			int kills = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getKills(p);
			int deaths = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getDeaths(p);
			int assists = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getAssists(p);

			// プレイヤーの戦績を表示
			p.sendMessage(
					ChatColor.GRAY + "[Your Score] " + p.getName() + " " + kills + " Kill(s), " + deaths + " Death(s), "
							+ assists + " Assist(s)");
		}
	}

	/**
	 * 最後に finalizeMatch メソッドを実行するためのリスナー
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void matchFinalizer(MatchFinishedEvent e) {
		LeonGunWar.getPlugin().getManager().finalizeMatch();
	}

	/**
	 * プレイヤーのスコアボードを更新するためのリスナー
	 */
	@EventHandler
	public void scoreboardUpdater(MatchTimeChangedEvent e) {
		// 試合中のプレイヤーを取得
		List<Player> players = LeonGunWar.getPlugin().getManager().getAllTeamPlayers();

		// スコアボードをアップデート
		LeonGunWar.getPlugin().getScoreboardDisplayer().updateScoreboard(players);
	}

	/**
	 * プレイヤーのアクションバーを更新するリスナー
	 */
	@EventHandler
	public void actionbarUpdater(MatchTimeChangedEvent e) {
		// 試合中のプレイヤーを取得
		List<Player> players = LeonGunWar.getPlugin().getManager().getAllTeamPlayers();

		// アクションバーをアップデート
		for (Player p : players) {
			// キル数取得
			int kills = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getKills(p);
			// デス数取得
			int deaths = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getDeaths(p);
			// アシスト数取得
			int assists = LeonGunWar.getPlugin().getManager().getKillDeathCounter().getAssists(p);

			String msg = "";

			// 両方を足して0の場合は白く表示
			if (kills + deaths == 0) {
				msg = "0 Kill " + Strings.repeat("┃", 50) + " 0 Death";
			} else { // それ以外の場合はメーター作成
				// キルのパーセンテージ
				double killsPercentage = (double) kills / (double) (kills + deaths) * 100d;
				// デスのパーセンテージ
				double deathsPercentage = (double) deaths / (double) (kills + deaths) * 100d;

				msg += ChatColor.LIGHT_PURPLE + Strings.repeat("┃", (int) killsPercentage / 2);
				msg += ChatColor.DARK_PURPLE + Strings.repeat("┃", (int) deathsPercentage / 2);

				// キル数とデス数を数字で表示
				msg = ChatColor.YELLOW + "" + kills + " Kill(s) " + msg;
				msg += " " + ChatColor.YELLOW + "" + deaths + " Death(s)";
			}

			// アシスト数を表示
			msg += " " + ChatColor.DARK_GRAY + "" + assists + " Assist(s)";

			// アクションバーに表示
			JSONMessage.create(msg).actionbar(p);
		}
	}
}

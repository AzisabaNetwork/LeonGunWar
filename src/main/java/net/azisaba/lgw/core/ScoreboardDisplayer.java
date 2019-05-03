package net.azisaba.lgw.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.azisaba.lgw.core.teams.BattleTeam;

public class ScoreboardDisplayer {

	/**
	 * プレイヤーに表示するスコアボードのタイトルを取得します
	 * @return スコアボードのタイトル
	 */
	private static String scoreBoardTitle() {
		return ChatColor.GOLD + "LeonGunWar";
	}

	/**
	 * スコアボードに表示したい文章をListで指定する (上から)
	 * @return The lines that you want to display in your scoreboard. (from above)
	 */
	private static List<String> boardLines() {
		// 試合中の場合
		if (MatchManager.isMatching()) {

			/**
			 *
			 * 残り時間: ?秒
			 *
			 * 赤チーム: ? point
			 * 青チーム: ? point
			 *
			 * 現在のマップ: {マップ名}
			 *
			 * azisaba.net
			 */

			// マップ名を取得
			String mapName = MatchManager.getCurrentGameMap().getMapName();

			// 赤、青チームの現在のポイントを取得
			int redPoint = MatchManager.getCurrentTeamPoint(BattleTeam.RED);
			int bluePoint = MatchManager.getCurrentTeamPoint(BattleTeam.BLUE);

			// 残り時間
			int timeLeft = MatchManager.getTimeLeft();

			// 文字を作成
			String line1 = "";
			String line2 = ChatColor.AQUA + "残り時間" + ChatColor.GREEN + ": " + ChatColor.RED + timeLeft + "秒";
			String line3 = "";
			String line4 = BattleTeam.RED.getTeamName() + ChatColor.GREEN + ": " + ChatColor.YELLOW + redPoint
					+ " point";
			String line5 = BattleTeam.BLUE.getTeamName() + ChatColor.GREEN + ": " + ChatColor.YELLOW + bluePoint
					+ " point";
			String line6 = "";
			String line7 = ChatColor.GRAY + "現在のマップ" + ChatColor.GREEN + ": " + ChatColor.RED + mapName;
			String line8 = "";
			String line9 = ChatColor.GOLD + "play azisaba.net";

			// リストにして返す
			return Arrays.asList(line1, line2, line3, line4, line5, line6, line7, line8, line9);
		}

		// 試合をしていない場合
		return null;
	}

	// Objectiveを作成したいスコアボード
	private static Scoreboard board;

	static {
		// 初期設定でボードはMain
		board = Bukkit.getScoreboardManager().getMainScoreboard();
	}

	/**
	 * 使用したいScoreboardを指定
	 * @param board 使用したいScoreboard
	 */
	public static void setScoreBoard(Scoreboard board) {
		ScoreboardDisplayer.board = board;
	}

	/**
	 * プレイヤーにスコアボードを表示します
	 * @param plist スコアボードを表示させたいプレイヤーのリスト
	 */
	public static void updateScoreboard(List<Player> plist) {
		if (Bukkit.getOnlinePlayers().size() <= 0) {
			return;
		}

		// 現在指定されているEntryを全て解除
		clearEntries();

		// Objectiveを取得
		Objective obj = board.getObjective("side");

		// Objectiveが存在しなかった場合は作成
		if (obj == null) {
			obj = board.registerNewObjective("side", "dummy");
		}

		// Slotを設定
		obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		obj.setDisplayName(scoreBoardTitle());

		// 行を取得
		List<String> lines = boardLines();

		// nullが返ってきた場合は非表示にしてreturn
		if (lines == null) {
			board.clearSlot(DisplaySlot.SIDEBAR);
			return;
		}

		// reverseして0から設定していく
		Collections.reverse(lines);

		int currentValue = 0;
		for (String msg : lines) {

			// 行が0の場合は空白にする
			if (msg == null)
				msg = "";

			// すでに値が設定されている場合は最後に空白を足していく
			while (obj.getScore(msg).isScoreSet()) {
				msg = msg + " ";
			}

			// 値を設定
			obj.getScore(msg).setScore(currentValue);
			currentValue++;
		}

		for (Player p : Bukkit.getOnlinePlayers()) {
			// スコアボードを設定する
			p.setScoreboard(board);
		}
	}

	/**
	 * 現在設定されているEntryを全てリセットする
	 */
	private static void clearEntries() {
		if (board.getEntries() != null)
			for (String str : board.getEntries()) {
				board.resetScores(str);
			}
	}
}
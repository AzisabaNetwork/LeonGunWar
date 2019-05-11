package net.azisaba.lgw.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.azisaba.lgw.core.teams.BattleTeam;
import net.azisaba.lgw.core.utils.Chat;

public class ScoreboardDisplayer {

	/**
	 * プレイヤーに表示するスコアボードのタイトルを取得します
	 * @return スコアボードのタイトル
	 */
	private String scoreBoardTitle() {
		return Chat.f("&6LeonGunWar&a v{0}", LeonGunWar.getPlugin().getDescription().getVersion());
	}

	/**
	 * スコアボードに表示したい文章をListで指定する (上から)
	 * @return The lines that you want to display in your scoreboard. (from above)
	 */
	private List<String> boardLines() {
		// 試合中の場合
		if (LeonGunWar.getPlugin().getManager().isMatching()) {

			/**
			 *
			 * 残り時間: ?秒
			 *
			 * 赤チーム: ? Point(s)
			 * 青チーム: ? Point(s)
			 *
			 * 現在のマップ: {マップ名}
			 *
			 * azisaba.net で今すぐ遊べ！
			 */

			// マップ名を取得
			String mapName = LeonGunWar.getPlugin().getManager().getCurrentGameMap().getMapName();

			// 赤、青チームの現在のポイントを取得
			int redPoint = LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(BattleTeam.RED);
			int bluePoint = LeonGunWar.getPlugin().getManager().getCurrentTeamPoint(BattleTeam.BLUE);

			// 残り時間
			int timeLeft = LeonGunWar.getPlugin().getManager().getTimeLeft().get();

			// 文字を作成
			String line1 = "";
			String line2 = Chat.f("&b残り時間&a: &c{0}秒", timeLeft);
			String line3 = "";
			String line4 = Chat.f("{0}&a: &e{1} Point(s)", BattleTeam.RED.getDisplayTeamName(), redPoint);
			String line5 = Chat.f("{0}&a: &e{1} Point(s)", BattleTeam.BLUE.getDisplayTeamName(), bluePoint);
			String line6 = "";
			String line7 = Chat.f("&7現在のマップ&a: &c{0}", mapName);
			String line8 = "";
			String line9 = Chat.f("&6azisaba.net &7で今すぐ遊べ！");

			// リストにして返す
			return Arrays.asList(line1, line2, line3, line4, line5, line6, line7, line8, line9);
		}

		// 試合をしていない場合
		return null;
	}

	// Objectiveを作成したいスコアボード
	private Scoreboard board;

	public ScoreboardDisplayer() {
		// 初期設定でボードはMain
		board = Bukkit.getScoreboardManager().getMainScoreboard();
	}

	/**
	 * 使用したいScoreboardを指定
	 * @param board 使用したいScoreboard
	 */
	public void setScoreBoard(Scoreboard board) {
		this.board = board;
	}

	/**
	 * プレイヤーにスコアボードを表示します
	 * @param plist スコアボードを表示させたいプレイヤーのリスト
	 */
	public void updateScoreboard(List<Player> plist) {
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
			if (msg == null) {
				msg = "";
			}

			// すでに値が設定されている場合は最後に空白を足していく
			while (obj.getScore(msg).isScoreSet()) {
				msg = msg + " ";
			}

			// 値を設定
			obj.getScore(msg).setScore(currentValue);
			currentValue++;
		}

		// スコアボードを設定する
		Bukkit.getOnlinePlayers().forEach(p -> p.setScoreboard(board));
	}

	/**
	 * 現在設定されているEntryを全てリセットする
	 */
	private void clearEntries() {
		board.getEntries().forEach(board::resetScores);
	}

	public void clearSideBar() {
		// boardがnullでなければSIDEBARを削除
		if (board != null) {
			board.clearSlot(DisplaySlot.SIDEBAR);
		}
	}
}

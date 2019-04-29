package net.azisaba.lgw.core;

import net.azisaba.lgw.core.teams.DefaultTeamDistributor;
import net.azisaba.lgw.core.teams.TeamDistributor;

/**
 *
 * ゲームを司るコアクラス
 * @author siloneco
 *
 */
public class MatchManager {

	// plugin
	private static LeonGunWar plugin;
	private static boolean initialized = false;

	// チーム分けを行うクラス
	private static TeamDistributor teamDistributor;

	// ゲーム中かどうかの判定
	private static boolean isMatching = false;

	/**
	 * 初期化メゾッド
	 * Pluginが有効化されたときのみ呼び出されることを前提としています
	 * @param plugin LeonGunWar plugin
	 */
	protected static void init(LeonGunWar plugin) {
		// すでに初期化されている場合はreturn
		if (initialized) {
			return;
		}

		MatchManager.plugin = plugin;

		// デフォルトのTeamDistributorを指定
		MatchManager.teamDistributor = new DefaultTeamDistributor();

		initialized = true;
	}

	/**
	 * チーム分けを行うクラスを変更します
	 * @param distributor 変更するTeamDistributorを実装したクラスのコンストラクタ
	 */
	public static void setTeamDistributor(TeamDistributor distributor) {
		MatchManager.teamDistributor = distributor;
	}
}

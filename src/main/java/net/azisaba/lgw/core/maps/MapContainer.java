package net.azisaba.lgw.core.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.azisaba.lgw.core.LeonGunWar;

public class MapContainer {

	private static List<GameMap> mapList = new ArrayList<>();

	private static boolean initialized = false;

	// ランダムなマップを呼び出すときに何度もコンストラクタを呼び出すためRandomを設置
	private static Random random = null;

	/**
	 * ファイルに保存してあるマップデータをロードします
	 * このメゾッドはPluginのロード時にのみ呼び出されることを想定しています
	 * @param plugin
	 */
	public static void init(LeonGunWar plugin) {
		// すでにメゾッドが呼び出されている場合はreturn
		if (initialized) {
			return;
		}

		// GameSettingsLoaderの初期化
		MatchSettingsLoader.init(plugin);
		// 保存されているマップデータの収集
		mapList = MatchSettingsLoader.loadMapData();

		plugin.getLogger().info(mapList.size() + "個のマップをロードしました。");

		// 初期ロード完了
		initialized = true;
	}

	/**
	 * 現在ロードされているすべてのゲームマップを返します。
	 * @return ロードされているゲームマップ
	 *
	 * @exception IllegalStateException MapContainerが初期化される前に呼び出された場合
	 */
	public static List<GameMap> getAllGameMap() {
		if (!initialized) {
			throw new IllegalStateException("\"" + MapContainer.class.getName() + "\" is not initialized yet.");
		}

		return mapList;
	}

	/**
	 * ロードされているすべてのマップから1つだけランダムで抽選します
	 * @return ランダムなマップ
	 *
	 * @exception IllegalStateException MapContainerが初期化される前に呼び出された場合
	 */
	public static GameMap getRandomMap() {
		if (!initialized) {
			throw new IllegalStateException("\"" + MapContainer.class.getName() + "\" is not initialized yet.");
		}

		// 登録されているマップが0この場合nullをreturn
		if (mapList.size() <= 0) {
			return null;
		}

		// randomがnullの場合作成
		if (random == null)
			random = new Random();

		// 0からmapListのサイズ -1 までの値でランダムな数字を生成
		int randomNumber = random.nextInt(mapList.size());

		// リストから取得してreturn
		return mapList.get(randomNumber);
	}
}

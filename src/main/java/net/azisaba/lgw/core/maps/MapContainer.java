package net.azisaba.lgw.core.maps;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Preconditions;

import net.azisaba.lgw.core.LeonGunWar;

public class MapContainer {

	private static final List<GameMap> MAP_LIST = new ArrayList<>();

	private static boolean loaded = false;

	/**
	 * ファイルに保存してあるマップデータをロードします
	 * このメソッドはPluginのロード時にのみ呼び出されることを想定しています
	 * @param plugin
	 */
	public static void init(LeonGunWar plugin) {
		// すでにメソッドが呼び出されている場合はreturn
		if (loaded) {
			return;
		}

		// GameSettingsLoaderの初期化
		MapLoader.init(plugin);
		// 保存されているマップデータの収集
		MAP_LIST.addAll(MapLoader.loadMapData());

		plugin.getLogger().info(MAP_LIST.size() + "個のマップをロードしました。");

		// ロード完了
		loaded = true;
	}

	/**
	 * 現在ロードされているすべてのゲームマップを返します。
	 * @return ロードされているゲームマップ
	 *
	 * @exception IllegalStateException MapContainerが初期化される前に呼び出された場合
	 */
	public static List<GameMap> getAllGameMap() {
		Preconditions.checkState(loaded, "\"" + MapContainer.class.getName() + "\" is not initialized yet.");

		return MAP_LIST;
	}

	/**
	 * ロードされているすべてのマップから1つだけランダムで抽選します
	 * @return ランダムなマップ
	 *
	 * @exception IllegalStateException MapContainerが初期化される前に呼び出された場合
	 */
	public static GameMap getRandomMap() {
		Preconditions.checkState(loaded, "\"" + MapContainer.class.getName() + "\" is not initialized yet.");

		// 登録されているマップが0この場合nullをreturn
		if (MAP_LIST.size() <= 0) {
			return null;
		}

		// 0からmapListのサイズ -1 までの値でランダムな数字を生成
		int randomNumber = ThreadLocalRandom.current().nextInt(MAP_LIST.size());

		// リストから取得してreturn
		return MAP_LIST.get(randomNumber);
	}
}

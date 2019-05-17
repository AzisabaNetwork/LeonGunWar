package net.azisaba.lgw.core.map;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import com.google.common.base.Preconditions;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.GameMap;

public class MapContainer {

	private final List<GameMap> mapList = new ArrayList<>();

	private boolean loaded = false;

	/**
	 * ファイルに保存してあるマップデータをロードします
	 * このメソッドはPluginのロード時にのみ呼び出されることを想定しています
	 * @param plugin
	 */
	public void loadMaps() {
		// すでにメソッドが呼び出されている場合はreturn
		if (loaded) {
			return;
		}

		// 保存されているマップデータの収集
		mapList.addAll(LeonGunWar.getPlugin().getMapLoader().loadMapData());

		LeonGunWar.getPlugin().getLogger().info(mapList.size() + "個のマップをロードしました。");

		// ロード完了
		loaded = true;
	}

	/**
	 * 現在ロードされているすべてのゲームマップを返します。
	 * @return ロードされているゲームマップ
	 *
	 * @exception IllegalStateException MapContainerが初期化される前に呼び出された場合
	 */
	public List<GameMap> getAllGameMap() {
		Preconditions.checkState(loaded, "\"" + MapContainer.class.getName() + "\" is not initialized yet.");

		return mapList;
	}

	/**
	 * ロードされているすべてのマップから1つだけランダムで抽選します
	 * @return ランダムなマップ
	 *
	 * @exception IllegalStateException MapContainerが初期化される前に呼び出された場合
	 */
	public GameMap getRandomMap() {
		Preconditions.checkState(loaded, "\"" + MapContainer.class.getName() + "\" is not initialized yet.");

		// 登録されているマップが0この場合nullをreturn
		if (mapList.size() <= 0) {
			return null;
		}

		// 0からmapListのサイズ -1 までの値でランダムな数字を生成
		int randomNumber = ThreadLocalRandom.current().nextInt(mapList.size());

		// リストから取得してreturn
		return mapList.get(randomNumber);
	}
}

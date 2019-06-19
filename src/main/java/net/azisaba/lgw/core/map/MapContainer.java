package net.azisaba.lgw.core.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.GameMap;

import lombok.Data;

@Data
public class MapContainer {

	private final List<GameMap> allGameMap = new ArrayList<>();

	private boolean loaded = false;

	/**
	 * ファイルに保存してあるマップデータをロードします
	 * このメソッドはPluginのロード時にのみ呼び出されることを想定しています
	 * @param plugin
	 * @exception IllegalStateException MapContainerが既に初期化されている場合
	 */
	public void loadMaps() {
		// すでにメソッドが呼び出されている場合はエラー
		Preconditions.checkState(!loaded, "\"" + MapContainer.class.getName() + "\" is already initialized.");

		// 保存されているマップデータの収集
		allGameMap.addAll(LeonGunWar.getPlugin().getMapLoader().loadMapData());
		LeonGunWar.getPlugin().getLogger().info(allGameMap.size() + " 個のマップをロードしました。");

		// ロード完了
		loaded = true;
	}

	/**
	 * ロードされているすべてのマップから1つだけランダムで抽選します
	 * @return ランダムなマップ
	 */
	public GameMap getRandomMap() {
		// 登録されているマップが0この場合nullをreturn
		// 0からmapListのサイズ -1 までの値でランダムな数字を生成
		// リストから取得してreturn
		return allGameMap.isEmpty() ? null : allGameMap.get(new Random().nextInt(allGameMap.size()));
	}

	/**
	 * マップをファイルからリロードします
	 */
	public void reloadMapsFromFile() {
		// allGameMapの初期化
		allGameMap.clear();

		// 保存されているマップデータを収集
		allGameMap.addAll(LeonGunWar.getPlugin().getMapLoader().loadMapData());
		LeonGunWar.getPlugin().getLogger().info(allGameMap.size() + " 個のマップをロードしました。");
	}
}

package net.azisaba.lgw.core.util;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author siloneco
 * ゲームを行うマップの情報を格納するクラス
 *
 */
public class GameMap {

	// プレイヤーに表示するマップ名
	private String mapName;
	// 各チームのスポーン地点
	private final Map<BattleTeam, Location> spawnMap;
	// マップのワールド
	private World world;

	public GameMap(String mapName, World world, Map<BattleTeam, Location> spawnMap) {
		this.mapName = mapName;
		this.world = world;

		// ワールドを指定
		for (BattleTeam team : spawnMap.keySet()) {
			Location loc = spawnMap.get(team).clone();
			loc.setWorld(world);
			spawnMap.put(team, loc);
		}

		this.spawnMap = spawnMap;
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public Location getSpawnPoint(BattleTeam team) {
		// 指定されていない場合はreturn null
		return spawnMap.getOrDefault(team, null);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}

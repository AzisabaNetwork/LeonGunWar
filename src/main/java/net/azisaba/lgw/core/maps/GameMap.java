package net.azisaba.lgw.core.maps;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.World;

import net.azisaba.lgw.core.teams.BattleTeam;

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
	private HashMap<BattleTeam, Location> spawnMap;
	// マップのワールド
	private World world;

	public GameMap(String mapName, World world, HashMap<BattleTeam, Location> spawnMap) {
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
		if (!spawnMap.containsKey(team)) {
			return null;
		}

		return spawnMap.get(team);
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}

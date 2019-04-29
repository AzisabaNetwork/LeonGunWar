package net.azisaba.lgw.core.maps;

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
	// 赤と青のスポーン地点
	private Location redSpawn, blueSpawn;
	// マップのワールド
	private World world;

	public GameMap(String mapName, World world, Location redSpawn, Location blueSpawn) {
		this.mapName = mapName;
		this.world = world;
		this.redSpawn = redSpawn.clone();
		this.blueSpawn = blueSpawn.clone();

		this.redSpawn.setWorld(this.world);
		this.blueSpawn.setWorld(this.world);
	}

	public String getMapName() {
		return mapName;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public Location getRedSpawn() {
		return redSpawn;
	}

	public void setRedSpawn(Location redSpawn) {
		this.redSpawn = redSpawn;
	}

	public Location getBlueSpawn() {
		return blueSpawn;
	}

	public void setBlueSpawn(Location blueSpawn) {
		this.blueSpawn = blueSpawn;
	}

	public World getWorld() {
		return world;
	}

	public void setWorld(World world) {
		this.world = world;
	}
}

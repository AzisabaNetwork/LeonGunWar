package net.azisaba.lgw.core.util;

import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author siloneco ゲームを行うマップの情報を格納するクラス
 *
 */
@Data
public class GameMap {

    // プレイヤーに表示するマップ名
    private final String mapName;
    // マップのワールド
    private final World world;
    // 各チームのスポーン地点
    private final Map<BattleTeam, Location> spawnMap;
    private final List<Area3D> hijackAreas;

    public GameMap(String mapName, World world, Map<BattleTeam, Location> spawnMap, List<Area3D> hijackAreas) {
        this.mapName = mapName;
        this.world = world;
        this.spawnMap = spawnMap;
        this.hijackAreas = hijackAreas != null ? hijackAreas : new ArrayList<>();
    }

    public Location getSpawnPoint(BattleTeam team) {
        // 指定されていない場合はreturn null
        return spawnMap.getOrDefault(team, null);
    }

    public String getMapName() {
        return mapName;
    }
}

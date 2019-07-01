package net.azisaba.lgw.core.util;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;

import lombok.Data;

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

    public Location getSpawnPoint(BattleTeam team) {
        // 指定されていない場合はreturn null
        return spawnMap.getOrDefault(team, null);
    }
}

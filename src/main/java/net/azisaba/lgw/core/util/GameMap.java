package net.azisaba.lgw.core.util;

import java.util.List;
import java.util.Map;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.Match;
import org.bukkit.GameMode;
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
    //マップの対応ゲームモード
    //MatchModeのsuggestが入っている
    private final List<String> allowMatchMode;
    // 各チームの目標スポーン地点
    private final Map<BattleTeam, Location> bossSpawnMap;

    public GameMap(String mapName, World world, Map<BattleTeam, Location> spawnMap, List<String> allowMatchMode, Map<BattleTeam, Location> bossspawnMap) {
        this.mapName = mapName;
        this.world = world;
        this.spawnMap = spawnMap;
        this.allowMatchMode = allowMatchMode;
        this.bossSpawnMap = bossspawnMap;
    }

    public Location getSpawnPoint(BattleTeam team) {
        // 指定されていない場合はreturn null
        return spawnMap.getOrDefault(team, null);
    }

    public Location getBossSpawnPoint(BattleTeam team) {
        // 指定されていない場合はreturn null
        return bossSpawnMap.getOrDefault(team, null);
    }

    public String getMapName() { return mapName; }
}

package net.azisaba.lgw.core.util;

import java.util.Map;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * @author siloneco ゲームを行うマップの情報を格納するクラス
 */
@Data
public class GameMap {

  // プレイヤーに表示するマップ名
  private final String mapName;
  // マップのワールド
  private final World world;
  // 各チームのスポーン地点
  private final Map<BattleTeam, Location> spawnMap;

  public GameMap(String mapName, World world, Map<BattleTeam, Location> spawnMap) {
    this.mapName = mapName;
    this.world = world;
    this.spawnMap = spawnMap;
  }

  public Location getSpawnPoint(BattleTeam team) {
    // 指定されていない場合はreturn null
    return spawnMap.getOrDefault(team, null);
  }

  public String getMapName() {
    return mapName;
  }
}

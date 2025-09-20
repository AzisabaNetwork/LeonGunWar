package net.azisaba.lgw.core.util;

import net.azisaba.lgw.core.battlesystem.BattleTeam;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * @param mapName  プレイヤーに表示するマップ名
 * @param world    マップのワールド
 * @param spawnMap 各チームのスポーン地点
 * @author siloneco ゲームを行うマップの情報を格納するクラス
 */
public record GameMap(String mapName, World world, Map<BattleTeam, Location> spawnMap) {
    @Nullable
    public Location getSpawnPoint(BattleTeam team) {
        // 指定されていない場合はreturn null
        return spawnMap.getOrDefault(team, null);
    }
}

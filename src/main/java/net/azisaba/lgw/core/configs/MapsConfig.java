package net.azisaba.lgw.core.configs;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.base.Enums;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class MapsConfig extends Config {

    private List<GameMap> allGameMap;

    public MapsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, Paths.get("configs/maps.yml"), Paths.get("maps.yml"));
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() {
        super.loadConfig();

        this.allGameMap = new ArrayList<>();
        config.getValues(false).keySet().stream()
                .map(mapName -> {
                    World world = plugin.getServer().getWorld(config.getString(mapName + ".world"));
                    Map<BattleTeam, Location> spawnMap = config.getConfigurationSection(mapName + ".spawns").getValues(false).keySet().stream()
                            .map(lowerTeamName -> Enums.getIfPresent(BattleTeam.class, lowerTeamName.toUpperCase()).orNull())
                            .filter(Objects::nonNull)
                            .collect(Collectors.toMap(Function.identity(), team -> config.getSerializable(team.name().toLowerCase(), Location.class)));
                    GameMap gameMap = new GameMap(mapName, world, spawnMap);
                    plugin.getLogger().fine("マップ " + gameMap.getMapName() + " をロードしました。");
                    return gameMap;
                })
                .forEach(allGameMap::add);
        plugin.getLogger().info(allGameMap.size() + " 個のマップをロードしました。");
    }

    /**
     * ロードされているすべてのマップから1つだけランダムで抽選します
     *
     * @return ランダムなマップ
     */
    public GameMap getRandomMap() {
        // 登録されているマップが0この場合nullをreturn
        // 0からmapListのサイズ -1 までの値でランダムな数字を生成
        // リストから取得してreturn
        return allGameMap.isEmpty() ? null : allGameMap.get(new Random().nextInt(allGameMap.size()));
    }
}

package net.azisaba.lgw.core.configs;

import com.google.common.base.Enums;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.LgwLog;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

import net.azisaba.lgw.core.util.Area3D;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import com.google.common.base.Enums;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.bukkit.util.Vector;

@Getter
public class MapsConfig extends Config {
    private final Logger logger = LgwLog.getLogger(this.getClass());

    private List<GameMap> allGameMap;

    public MapsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/maps.yml", "maps.yml");
    }

    @SneakyThrows(value = {Exception.class})
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        allGameMap = new ArrayList<>();
        for (String mapName : config.getValues(false).keySet()) {
            ConfigurationSection mapSection = config.getConfigurationSection(mapName);
            ConfigurationSection spawnsSection = mapSection.getConfigurationSection("spawns");

            World world = plugin.getServer().getWorld(mapSection.getString("world"));

            Map<BattleTeam, Location> spawnMap = new HashMap<>();
            for (String teamName : spawnsSection.getValues(false).keySet()) {
                Optional<BattleTeam> battleTeam = Enums.getIfPresent(BattleTeam.class, teamName.toUpperCase()).toJavaUtil();

                if (battleTeam.isPresent()) {
                    Location spawn = new Location(
                            plugin.getServer().getWorld(spawnsSection.getString(teamName + ".world")),
                            spawnsSection.getDouble(teamName + ".x"),
                            spawnsSection.getDouble(teamName + ".y"),
                            spawnsSection.getDouble(teamName + ".z"),
                            (float) spawnsSection.getDouble(teamName + ".yaw"),
                            (float) spawnsSection.getDouble(teamName + ".pitch"));
                    spawnMap.put(battleTeam.get(), spawn);
                }
            }

            List<Area3D> areas = new ArrayList<>();
            for (String area : mapSection.getConfigurationSection("areas").getKeys(false)) {
                ConfigurationSection areaSection = mapSection.getConfigurationSection("areas." + area);
                Vector min = getLocation(areaSection.getConfigurationSection("min"));
                Vector max = getLocation(areaSection.getConfigurationSection("max"));
                areas.add(new Area3D(min, max));
            }

            GameMap gameMap = new GameMap(mapName, world, spawnMap, areas);
            allGameMap.add(gameMap);

            logger.info("マップ {} をロードしました。", mapName);
        }
        logger.info("{} 個のマップをロードしました。", allGameMap.size());
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

    /**
     * ロードされているすべてのマップから指定した数だけランダムで抽選します
     *
     * @return 指定した数のランダムなマップのHashSetを返す
     * @throws IllegalArgumentException 0以下またはロードされているマップの数よりも多い数が指定された場合
     */
    public Set<GameMap> getRandomMaps(int count) {
        if (count > allGameMap.size() || count <= 0) {
            throw new IllegalArgumentException("Mapを" + count + "個抽選することはできません！ (ロードされているMapの数: " + allGameMap.size() + " )");
        }
        List<GameMap> shuffleList = new ArrayList<>(allGameMap);
        Collections.shuffle(shuffleList);

        return new HashSet<>(shuffleList.subList(0, count));
    }

    public Set<GameMap> getRandomHijackMaps(int count) {
        List<GameMap> hijackMaps = new ArrayList<>();
        for (GameMap map : allGameMap) {
            if (!map.getHijackAreas().isEmpty()) {
                hijackMaps.add(map);
            }
        }
        if ( count > hijackMaps.size() || count <= 0 ) {
            throw new IllegalArgumentException("Hijack Mapを" + count + "個抽選することはできません！ (ロードされているHijack Mapの数: " + hijackMaps.size() + " )");
        }
        List<GameMap> shuffleList = new ArrayList<>(hijackMaps);
        Collections.shuffle(shuffleList);

        return new HashSet<>(shuffleList.subList(0, count));
    }

    public List<GameMap> getAllGameMap() {
        return allGameMap;
    }

    private Vector getLocation(ConfigurationSection section) {
        double x = section.getDouble("x");
        double y = section.getDouble("y");
        double z = section.getDouble("z");
        return new Vector(x, y, z);
    }
}

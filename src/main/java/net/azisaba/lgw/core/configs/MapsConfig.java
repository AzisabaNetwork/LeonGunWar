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

            String worldName = mapSection.getString("world");
            World world = plugin.getServer().getWorld(worldName);
            if (world == null) {
                logger.warn("マップ {} のワールド {} が読み込まれていません。", mapName, worldName);
            }

            Map<BattleTeam, Location> spawnMap = new HashMap<>();
            for (String teamName : spawnsSection == null ? Set.<String>of() : spawnsSection.getKeys(false)) {
                Optional<BattleTeam> battleTeam = Enums.getIfPresent(BattleTeam.class, teamName.toUpperCase()).toJavaUtil();

                if (battleTeam.isPresent()) {
                    String spawnWorldName = spawnsSection.getString(teamName + ".world");
                    World spawnWorld = plugin.getServer().getWorld(spawnWorldName);
                    if (spawnWorld == null) {
                        logger.warn("マップ {} の {} スポーン用ワールド {} が読み込まれていません。",
                                mapName, teamName, spawnWorldName);
                    }
                    Location spawn = new Location(
                            spawnWorld,
                            spawnsSection.getDouble(teamName + ".x"),
                            spawnsSection.getDouble(teamName + ".y"),
                            spawnsSection.getDouble(teamName + ".z"),
                            (float) spawnsSection.getDouble(teamName + ".yaw"),
                            (float) spawnsSection.getDouble(teamName + ".pitch"));
                    spawnMap.put(battleTeam.get(), spawn);
                }
            }

            List<Area3D> areas = new ArrayList<>();
            if (mapSection.getConfigurationSection("areas") != null) {
                for (String area : mapSection.getConfigurationSection("areas").getKeys(false)) {
                    ConfigurationSection areaSection = mapSection.getConfigurationSection("areas." + area);
                    Vector min = getLocation(areaSection.getConfigurationSection(".min"));
                    Vector max = getLocation(areaSection.getConfigurationSection(".max"));
                    areas.add(new Area3D(min, max));
                }
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
        List<GameMap> available = getReadyMaps();
        return available.isEmpty() ? null : available.get(new Random().nextInt(available.size()));
    }

    /**
     * ロードされているすべてのマップから指定した数だけランダムで抽選します
     *
     * @return 指定した数のランダムなマップのHashSetを返す
     * @throws IllegalArgumentException 0以下またはロードされているマップの数よりも多い数が指定された場合
     */
    public Set<GameMap> getRandomMaps(int count) {
        List<GameMap> shuffleList = new ArrayList<>(getReadyMaps());
        shuffleList.removeIf(map -> !map.getHijackAreas().isEmpty());
        if (count > shuffleList.size() || count <= 0) {
            throw new IllegalArgumentException("Mapを" + count + "個抽選することはできません！ (使用可能な通常Mapの数: " + shuffleList.size() + " )");
        }
        Collections.shuffle(shuffleList);

        return new HashSet<>(shuffleList.subList(0, count));
    }

    public Set<GameMap> getRandomHijackMaps(int count) {
        List<GameMap> hijackMaps = new ArrayList<>();
        for (GameMap map : getReadyMaps()) {
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

    public List<GameMap> getReadyMaps() {
        return allGameMap.stream().filter(GameMap::isReady).toList();
    }

    /** Registers a map in the player's current world after its first spawn is saved successfully. */
    public GameMap createMapWithSpawn(String mapName, BattleTeam team, Location location)
            throws IOException, InvalidConfigurationException {
        location.checkFinite();
        if (location.getWorld() == null) {
            throw new IllegalArgumentException("現在のワールドを取得できませんでした。");
        }
        var updated = MapSpawnStorage.save(getPath(), config.saveToString(), mapName, team,
                location.getWorld().getName(), location, true);
        config.loadFromString(updated.saveToString());
        Map<BattleTeam, Location> spawns = new HashMap<>();
        spawns.put(team, location.clone());
        GameMap map = new GameMap(mapName, location.getWorld(), spawns, new ArrayList<>());
        allGameMap.add(map);
        return map;
    }

    /** Saves only this team's spawn, then updates the existing map used by a running match. */
    public void setSpawnPoint(GameMap map, BattleTeam team, Location location)
            throws IOException, InvalidConfigurationException {
        location.checkFinite();
        if (!allGameMap.contains(map) || location.getWorld() == null || location.getWorld() != map.getWorld()) {
            throw new IllegalArgumentException("設定するマップのワールド内で実行してください。");
        }
        var updated = MapSpawnStorage.save(getPath(), config.saveToString(), map.getMapName(), team,
                location.getWorld().getName(), location);
        config.loadFromString(updated.saveToString());
        map.getSpawnMap().put(team, location.clone());
    }

    private Vector getLocation(ConfigurationSection section) {
        double x = section.getDouble(".x");
        double y = section.getDouble(".y");
        double z = section.getDouble(".z");
        return new Vector(x, y, z);
    }
}

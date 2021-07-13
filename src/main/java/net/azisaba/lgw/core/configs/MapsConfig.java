package net.azisaba.lgw.core.configs;

import java.io.IOException;
import java.sql.BatchUpdateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import org.bukkit.Bukkit;
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

@Getter
public class MapsConfig extends Config {

    private List<GameMap> allGameMap;

    public MapsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/maps.yml", "maps.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        allGameMap = new ArrayList<>();
        for ( String mapName : config.getValues(false).keySet() ) {
            ConfigurationSection mapSection = config.getConfigurationSection(mapName);
            ConfigurationSection spawnsSection = mapSection.getConfigurationSection("spawns");

            World world = plugin.getServer().getWorld(mapSection.getString("world"));

            Map<BattleTeam, Location> spawnMap = new HashMap<>();
            for ( String teamName : spawnsSection.getValues(false).keySet() ) {
                Optional<BattleTeam> battleTeam = Enums.getIfPresent(BattleTeam.class, teamName.toUpperCase()).toJavaUtil();

                if ( battleTeam.isPresent() ) {
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

            GameMap gameMap = new GameMap(mapName, world, spawnMap);
            allGameMap.add(gameMap);

            plugin.getLogger().info("マップ " + mapName + " をロードしました。");
        }
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

    public List<GameMap> getAllGameMap() {
        return allGameMap;
    }
}

package net.azisaba.lgw.core.configs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;

import net.azisaba.lgw.core.LeonGunWar;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class SpawnsConfig extends Config {

    private Map<String, Location> spawns;
    private Location lobby;
    private Location onsen;

    public SpawnsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/spawns.yml", "spawns.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() {
        super.loadConfig();

        spawns = new HashMap<>();
        for ( String spawnName : config.getValues(false).keySet() ) {
            Location spawn = new Location(
                    plugin.getServer().getWorld(config.getString(spawnName + ".world")),
                    config.getDouble(spawnName + ".x"),
                    config.getDouble(spawnName + ".y"),
                    config.getDouble(spawnName + ".z"),
                    (float) config.getDouble(spawnName + ".yaw"),
                    (float) config.getDouble(spawnName + ".pitch"));
            spawns.put(spawnName, spawn);
        }
        spawns = Collections.unmodifiableMap(spawns);

        lobby = spawns.get("lobby");
        onsen = spawns.get("onsen");
    }
}

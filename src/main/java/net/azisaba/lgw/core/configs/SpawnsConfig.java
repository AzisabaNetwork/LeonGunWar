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

    public SpawnsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/spawns.yml", "spawns.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() {
        super.loadConfig();

        spawns = new HashMap<>();
        for ( String spawnName : config.getValues(false).keySet() ) {
            Location spawn = config.getSerializable(spawnName, Location.class);
            spawns.put(spawnName, spawn);
        }
        spawns = Collections.unmodifiableMap(spawns);

        lobby = spawns.get("lobby");
    }
}

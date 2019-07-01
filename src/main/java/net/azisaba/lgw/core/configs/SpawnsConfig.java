package net.azisaba.lgw.core.configs;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        super(plugin, Paths.get("configs/spawns.yml"), Paths.get("spawns.yml"));
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() {
        super.loadConfig();

        this.spawns = new HashMap<>();
        config.getValues(false).keySet().stream()
                .collect(Collectors.toMap(Function.identity(), name -> config.getSerializable(name, Location.class)))
                .forEach(spawns::put);
        spawns = Collections.unmodifiableMap(spawns);

        lobby = spawns.get("lobby");
    }
}

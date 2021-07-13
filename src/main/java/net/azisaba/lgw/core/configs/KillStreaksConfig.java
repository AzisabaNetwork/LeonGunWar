package net.azisaba.lgw.core.configs;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.configuration.InvalidConfigurationException;

import net.azisaba.lgw.core.LeonGunWar;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;

@Getter
public class KillStreaksConfig extends Config {

    private Map<Integer, Map.Entry<List<String>, List<String>>> streaks;

    private Map<Integer, Map.Entry<List<String>, List<String>>> levels;

    private String removed;

    public KillStreaksConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/killStreaks.yml", "killStreaks.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        streaks = new HashMap<>();
        config.getConfigurationSection("streaks").getValues(false).keySet().stream()
                .map(Integer::valueOf)
                .collect(Collectors.toMap(Function.identity(), count -> {
                    List<String> messages = config.getStringList("streaks." + count + ".messages");
                    List<String> commands = config.getStringList("streaks." + count + ".commands");
                    return new AbstractMap.SimpleEntry<>(messages, commands);
                }))
                .forEach(streaks::put);
        streaks = Collections.unmodifiableMap(streaks);

        levels = new HashMap<>();
        config.getConfigurationSection("levels").getValues(false).keySet().stream()
                .map(Integer::valueOf)
                .collect(Collectors.toMap(Function.identity(), count -> {
                    List<String> messages = config.getStringList("levels." + count + ".messages");
                    List<String> commands = config.getStringList("levels." + count + ".commands");
                    return new AbstractMap.SimpleEntry<>(messages, commands);
                }))
                .forEach(levels::put);
        levels = Collections.unmodifiableMap(levels);

        removed = config.getString("removed");
    }
}

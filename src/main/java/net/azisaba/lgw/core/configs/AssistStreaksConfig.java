package net.azisaba.lgw.core.configs;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Deprecated(forRemoval = true, since = "4.1.0")
public class AssistStreaksConfig extends Config {

    private Map<Integer, Map.Entry<List<String>, List<String>>> levels;

    public AssistStreaksConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/assistStreaks.yml", "assistStreaks.yml");
    }

    @SneakyThrows(value = {Exception.class})
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

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
    }

    public Map<Integer, Map.Entry<List<String>, List<String>>> getLevels() {
        return levels;
    }
}

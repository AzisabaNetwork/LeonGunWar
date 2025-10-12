package net.azisaba.lgw.core.configs;

import net.azisaba.lgw.core.api.config.LGWConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigMigrator {
    // "configs/items.yml"
    public static LGWConfig.@NonNull ItemsConfig itemsConfig(File file) throws RuntimeException {
        final YamlConfiguration config = new YamlConfiguration();

        // load config from file
        if (Files.isRegularFile(file.toPath())) {
            try(var lines = Files.lines(file.toPath())) {
                config.loadFromString(lines.collect(Collectors.joining(System.lineSeparator())));
            } catch (InvalidConfigurationException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Failed to load config");
        }

        // load old config data
        List<String> victoryItemCrackShotIds;
        if (!config.isSet("victoryItems")) {
            victoryItemCrackShotIds = null;
        } else {
            victoryItemCrackShotIds = Collections.unmodifiableList(
                    config.getStringList("victoryItems"));
        }

        // move data to new instance
        LGWConfig.ItemsConfig newConfig = new LGWConfig.ItemsConfig();
        newConfig.victoryItemCrackShotIds = victoryItemCrackShotIds;

        return newConfig;
    }
}

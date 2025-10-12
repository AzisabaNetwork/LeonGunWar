package net.azisaba.lgw.core.configs;

import net.azisaba.lgw.core.api.config.LGWConfig;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConfigMigrator {
    private static YamlConfiguration loadConfig(File file) {
        final YamlConfiguration config = new YamlConfiguration();

        // load config from file
        if (Files.isRegularFile(file.toPath())) {
            try(Stream<String> lines = Files.lines(file.toPath())) {
                config.loadFromString(lines.collect(Collectors.joining(System.lineSeparator())));
            } catch (InvalidConfigurationException | IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new RuntimeException("Failed to load config");
        }
        return config;
    }

    // "configs/main.yml"
    public static LGWConfig.@NonNull MainConfig mainConfig(File file) throws RuntimeException {
        final YamlConfiguration config = loadConfig(file);

        boolean isLobby = config.getBoolean("isLobby", true);
        String serverName = config.getString("servername", "lobby");

        LGWConfig.MainConfig newConfig = new LGWConfig.MainConfig();
        newConfig.isLobby = isLobby;
        newConfig.serverName = serverName;

        return newConfig;
    }

    // "configs/items.yml"
    public static LGWConfig.@NonNull ItemsConfig itemsConfig(File file) throws RuntimeException {
        final YamlConfiguration config = loadConfig(file);

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

    // "configs/database.yml"
    public static LGWConfig.@NonNull DatabaseConfig databaseConfig(File file) throws RuntimeException {
        final YamlConfiguration config = loadConfig(file);

        // load old
        boolean enabled = config.getBoolean("enable", false);
        String host = config.getString("host", "HOST");
        int port = config.getInt("port", 3306);
        String database = config.getString("database", "conflict");
        String user = config.getString("user", "conflict");
        String password = config.getString("password", "password");

        // write new
        LGWConfig.DatabaseConfig newConfig = new LGWConfig.DatabaseConfig();
        newConfig.enabled = enabled;
        newConfig.host = host;
        newConfig.port = port;
        newConfig.database = database;
        newConfig.username = user;
        newConfig.password = password;

        return newConfig;
    }

    // "configs/syogo.yml"
    public static LGWConfig.@NonNull SyogoConfig syogoConfig(File file) throws RuntimeException {
        final YamlConfiguration config = loadConfig(file);

        HashMap<String, String> syogos = new HashMap<>();
        for (String syogo : config.getValues(false).keySet()) {
            syogos.put(syogo, config.getString(syogo));
        }

        LGWConfig.SyogoConfig newConfig = new LGWConfig.SyogoConfig();
        newConfig.syogos = syogos;

        return newConfig;
    }
}

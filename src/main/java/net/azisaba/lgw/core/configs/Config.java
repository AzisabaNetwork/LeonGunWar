package net.azisaba.lgw.core.configs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class Config {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @NonNull
    protected final Plugin plugin;

    protected final YamlConfiguration config = new YamlConfiguration();

    @NonNull
    private final String resourcePath;

    @NonNull
    private final String relativePath;

    public InputStream getResource() {
        return plugin.getClass().getClassLoader().getResourceAsStream(resourcePath);
    }

    public Path getPath() {
        return plugin.getDataFolder().toPath().resolve(relativePath);
    }

    public boolean exists() {
        return Files.isRegularFile(getPath());
    }

    public boolean existsResource() {
        return getResource() != null;
    }

    @SneakyThrows(value = { IOException.class })
    public String loadAsString() {
        return Files.lines(getPath()).collect(Collectors.joining(System.lineSeparator()));
    }

    public String loadResourceAsString() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(getResource(), StandardCharsets.UTF_8));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    @SneakyThrows(value = { InvalidConfigurationException.class })
    public void loadConfig() {
        if ( exists() ) {
            config.loadFromString(loadAsString());
        } else if ( existsResource() ) {
            config.loadFromString(loadResourceAsString());
            saveResource();
        }
    }

    public void saveResource() {
        saveResource(true);
    }

    @SneakyThrows(value = { IOException.class })
    public void saveResource(boolean async) {
        if ( async ) {
            executor.execute(() -> saveResource(false));
        } else {
            Files.createDirectories(getPath().getParent());
            Files.copy(getResource(), getPath());
        }
    }

    public void saveConfig() {
        saveConfig(true);
    }

    @SneakyThrows(value = { IOException.class })
    public void saveConfig(boolean async) {
        if ( async ) {
            executor.execute(() -> saveConfig(false));
        } else {
            Files.createDirectories(getPath().getParent());
            Files.write(getPath(), config.saveToString().getBytes(StandardCharsets.UTF_8));
        }
    }
}

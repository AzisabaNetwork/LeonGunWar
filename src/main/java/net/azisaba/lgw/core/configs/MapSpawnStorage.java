package net.azisaba.lgw.core.configs;

import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFileAttributeView;

final class MapSpawnStorage {
    private MapSpawnStorage() {
    }

    static YamlConfiguration save(Path path, String fallback, String mapName, BattleTeam team,
                                  String worldName, Location spawn) throws IOException, InvalidConfigurationException {
        return save(path, fallback, mapName, team, worldName, spawn, false);
    }

    static YamlConfiguration save(Path path, String fallback, String mapName, BattleTeam team,
                                  String worldName, Location spawn, boolean createMap)
            throws IOException, InvalidConfigurationException {
        if (mapName.isBlank() || mapName.contains(".") || mapName.contains("\n") || mapName.contains("\r")) {
            throw new IllegalArgumentException("マップ名は空欄にせず、ドットや改行を含めないでください。");
        }
        // Resolve links so saving a shared maps.yml does not replace its symlink.
        Path target = Files.exists(path) ? path.toRealPath() : path.toAbsolutePath();
        YamlConfiguration updated = new YamlConfiguration();
        updated.loadFromString(Files.exists(target) ? Files.readString(target, StandardCharsets.UTF_8) : fallback);
        ConfigurationSection map = updated.getConfigurationSection(mapName);
        if (createMap) {
            boolean exists = updated.getKeys(false).stream().anyMatch(name -> name.replace('_', ' ')
                    .equalsIgnoreCase(mapName.replace('_', ' ')));
            if (exists) {
                throw new IllegalArgumentException("maps.yml に同名のマップがあります。/lgwadmin reload 後に再実行してください。");
            }
            map = updated.createSection(mapName);
            map.set("world", worldName);
        } else if (map == null) {
            throw new IOException("maps.yml にマップがありません: " + mapName);
        }
        ConfigurationSection spawns = map.getConfigurationSection("spawns");
        if (spawns == null) {
            spawns = map.createSection("spawns");
        }
        String key = spawns.getKeys(false).stream().filter(name -> name.equalsIgnoreCase(team.name()))
                .findFirst().orElse(team.getEngTeamName());
        ConfigurationSection point = spawns.getConfigurationSection(key);
        if (point == null) {
            point = spawns.createSection(key);
        }
        point.set("world", worldName);
        point.set("x", spawn.getX());
        point.set("y", spawn.getY());
        point.set("z", spawn.getZ());
        point.set("yaw", spawn.getYaw());
        point.set("pitch", spawn.getPitch());

        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "maps-spawn-", ".tmp");
        try {
            Files.writeString(temporary, updated.saveToString(), StandardCharsets.UTF_8);
            if (Files.exists(target) && Files.getFileAttributeView(target, PosixFileAttributeView.class) != null) {
                Files.setPosixFilePermissions(temporary, Files.getPosixFilePermissions(target));
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
        return updated;
    }
}

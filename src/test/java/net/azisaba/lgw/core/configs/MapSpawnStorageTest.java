package net.azisaba.lgw.core.configs;

import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class MapSpawnStorageTest {
    @TempDir
    Path directory;

    @Test
    void savesPreciseFeetPositionAndFacingPreservingOtherTeamAndLatestDiskEdits() throws Exception {
        Path file = directory.resolve("maps.yml");
        Files.writeString(file, """
                vertigo:
                  world: maps
                  spawns:
                    RED: {world: maps, x: 1, y: 2, z: 3, custom: keep}
                    blue: {world: maps, x: -1080.5, y: 160, z: -1008.5}
                  areas:
                    objective: {custom: unchanged}
                Other: {world: edited-after-reload}
                """);
        Location location = new Location(null, -911.512345, 161.0625, -999.56789, 92.75f, -13.25f);
        MapSpawnStorage.save(file, "ignored: stale", "vertigo", BattleTeam.RED, "maps", location);
        YamlConfiguration saved = YamlConfiguration.loadConfiguration(file.toFile());
        assertEquals(location.getX(), saved.getDouble("vertigo.spawns.RED.x"));
        assertEquals(location.getY(), saved.getDouble("vertigo.spawns.RED.y"));
        assertEquals(location.getZ(), saved.getDouble("vertigo.spawns.RED.z"));
        assertEquals(location.getYaw(), saved.getDouble("vertigo.spawns.RED.yaw"));
        assertEquals(location.getPitch(), saved.getDouble("vertigo.spawns.RED.pitch"));
        assertEquals("maps", saved.getString("vertigo.spawns.RED.world"));
        assertEquals("keep", saved.getString("vertigo.spawns.RED.custom"));
        assertFalse(saved.contains("vertigo.spawns.red"));
        assertEquals(-1080.5, saved.getDouble("vertigo.spawns.blue.x"));
        assertEquals("unchanged", saved.getString("vertigo.areas.objective.custom"));
        assertEquals("edited-after-reload", saved.getString("Other.world"));
    }

    @Test
    void missingMapFailsWithoutChangingExistingFile() throws Exception {
        Path file = directory.resolve("maps.yml");
        String original = "Other: {world: maps}\n";
        Files.writeString(file, original);
        assertThrows(IOException.class, () -> MapSpawnStorage.save(file, "", "missing", BattleTeam.BLUE,
                "maps", new Location(null, 1, 2, 3)));
        assertEquals(original, Files.readString(file));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count());
        }
    }

    @Test
    void initializesMissingSpawnsFromExistingMap() throws Exception {
        Path file = directory.resolve("maps.yml");
        MapSpawnStorage.save(file, "vertigo: {world: maps}\n", "vertigo", BattleTeam.BLUE,
                "maps", new Location(null, 0.5, 64, -0.5));
        YamlConfiguration saved = YamlConfiguration.loadConfiguration(file.toFile());
        assertEquals(-0.5, saved.getDouble("vertigo.spawns.blue.z"));
        assertEquals("maps", saved.getString("vertigo.world"));
    }

    @Test
    void createsMapWithCurrentWorldThenAddsOtherTeamWithoutLosingFirstSpawn() throws Exception {
        Path file = directory.resolve("maps.yml");
        String existing = "Existing: {world: other}\n";
        MapSpawnStorage.save(file, existing, "New Arena", BattleTeam.RED,
                "current_world", new Location(null, -10.25, 64.5, 20.75, 90, -15), true);
        MapSpawnStorage.save(file, "", "New Arena", BattleTeam.BLUE,
                "current_world", new Location(null, 30.5, 65, -40.5));
        YamlConfiguration saved = YamlConfiguration.loadConfiguration(file.toFile());
        assertEquals("current_world", saved.getString("New Arena.world"));
        assertEquals("current_world", saved.getString("New Arena.spawns.red.world"));
        assertEquals("current_world", saved.getString("New Arena.spawns.blue.world"));
        assertEquals(-10.25, saved.getDouble("New Arena.spawns.red.x"));
        assertEquals(-15, saved.getDouble("New Arena.spawns.red.pitch"));
        assertEquals(30.5, saved.getDouble("New Arena.spawns.blue.x"));
        assertEquals("other", saved.getString("Existing.world"));
    }

    @Test
    void refusesToReplaceMapAddedOnDiskWithoutReload() throws Exception {
        Path file = directory.resolve("maps.yml");
        String original = "New_Arena: {world: existing_world}\n";
        Files.writeString(file, original);
        assertThrows(IllegalArgumentException.class, () -> MapSpawnStorage.save(file, "", "new arena",
                BattleTeam.RED, "current_world", new Location(null, 1, 2, 3), true));
        assertEquals(original, Files.readString(file));
    }

    @Test
    void rejectsPathSeparatorInNewMapNameWithoutCreatingFile() {
        Path file = directory.resolve("maps.yml");
        assertThrows(IllegalArgumentException.class, () -> MapSpawnStorage.save(file, "", "Parent.Child",
                BattleTeam.RED, "current_world", new Location(null, 1, 2, 3), true));
        assertFalse(Files.exists(file));
    }
}

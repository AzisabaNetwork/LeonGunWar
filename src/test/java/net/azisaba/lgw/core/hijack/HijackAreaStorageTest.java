package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.util.Area3D;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HijackAreaStorageTest {
    @TempDir Path directory;
    private Path fixture() throws Exception {
        Path path = directory.resolve("maps.yml");
        Files.writeString(path, """
                New Arena:
                  world: maps
                  spawns:
                    red: {world: maps, x: 1.5, y: 48, z: 3}
                  areas:
                    existing:
                      min: {x: -9, y: 48, z: -1741}
                      max: {x: -14, y: 52, z: -1736}
                      custom: keep
                Other: {world: latest-disk-edit}
                """);
        return path;
    }
    @Test void savesPreciseNormalizedGeometryAndPreservesOtherAreasSpawnsAndDiskEdits() throws Exception {
        Path path = fixture();
        Area3D area = new Area3D(new Vector(-1.125, 52, 8), new Vector(-11.25, 48, -2));
        var yaml = HijackAreaStorage.save(path, "New Arena", "maps", "2", area);
        assertEquals("latest-disk-edit", yaml.getString("Other.world"));
        assertEquals(1.5, yaml.getDouble("New Arena.spawns.red.x"));
        assertEquals("keep", yaml.getString("New Arena.areas.existing.custom"));
        var saved = HijackAreaStorage.readAreas(yaml.getConfigurationSection("New Arena"));
        assertEquals(2, saved.size());
        assertEquals(area.getMin(), saved.get("2").getMin());
        assertEquals(area.getMax(), saved.get("2").getMax());
        assertEquals(-14, saved.get("existing").getMin().getX());
        assertEquals(yaml.saveToString(), YamlConfiguration.loadConfiguration(path.toFile()).saveToString());
    }
    @Test void updatesSameIdAndDeletesOnlySpecifiedArea() throws Exception {
        Path path = fixture();
        var area = new Area3D(new Vector(0, 10, 0), new Vector(10, 14, 10));
        var yaml = HijackAreaStorage.save(path, "New Arena", "maps", "existing", area);
        assertEquals(1, yaml.getConfigurationSection("New Arena.areas").getKeys(false).size());
        assertEquals("keep", yaml.getString("New Arena.areas.existing.custom"));
        HijackAreaStorage.save(path, "New Arena", "maps", "2", area);
        yaml = HijackAreaStorage.save(path, "New Arena", "maps", "existing", null);
        assertFalse(yaml.contains("New Arena.areas.existing"));
        assertTrue(yaml.contains("New Arena.areas.2"));
        assertEquals(1.5, yaml.getDouble("New Arena.spawns.red.x"));
    }
    @Test void rejectsWrongMapWorldIdOrMissingDeletionWithoutChangingFile() throws Exception {
        Path path = fixture();
        String before = Files.readString(path);
        var area = new Area3D(new Vector(), new Vector(10, 4, 10));
        assertThrows(IllegalArgumentException.class, () -> HijackAreaStorage.save(path, "missing", "maps", "1", area));
        assertThrows(IllegalArgumentException.class, () -> HijackAreaStorage.save(path, "New Arena", "other", "1", area));
        assertThrows(IllegalArgumentException.class, () -> HijackAreaStorage.save(path, "New Arena", "maps", "../../bad", area));
        assertThrows(IllegalArgumentException.class, () -> HijackAreaStorage.save(path, "New Arena", "maps", "missing", null));
        assertEquals(before, Files.readString(path));
        try (var files = Files.list(directory)) { assertEquals(1, files.count()); }
    }
    @Test void malformedExistingAreaAndNonFiniteCoordinatesNeverReplaceFile() throws Exception {
        Path path = fixture();
        String invalid = Files.readString(path).replace("min: {x: -9, y: 48, z: -1741}", "min: {x: broken, y: 48, z: -1741}");
        Files.writeString(path, invalid);
        var area = new Area3D(new Vector(), new Vector(10, 4, 10));
        assertThrows(IllegalArgumentException.class, () -> HijackAreaStorage.save(path, "New Arena", "maps", "2", area));
        assertEquals(invalid, Files.readString(path));
        var nonFinite = new Area3D(new Vector(Double.NaN, 0, 0), new Vector(10, 4, 10));
        assertThrows(IllegalArgumentException.class, () -> HijackAreaStorage.save(path, "New Arena", "maps", "2", nonFinite));
        assertEquals(invalid, Files.readString(path));
    }
}

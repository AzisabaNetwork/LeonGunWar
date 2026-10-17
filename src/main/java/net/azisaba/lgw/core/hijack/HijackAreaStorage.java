package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.AtomicConfigWriter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.Vector;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/** Changes only one objective in the latest maps.yml; publish in memory after saving. */
public final class HijackAreaStorage {
    private HijackAreaStorage() {}

    public static YamlConfiguration save(Path file, String mapName, String worldName, String id, Area3D area)
            throws IOException, InvalidConfigurationException {
        if (!id.matches("[A-Za-z0-9_-]{1,32}")) throw new IllegalArgumentException("エリアIDは英数字・_・-の1～32文字です。");
        var yaml = new YamlConfiguration();
        yaml.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
        Object value = yaml.getValues(false).get(mapName);
        if (!(value instanceof ConfigurationSection map) || !worldName.equals(map.getString("world"))) {
            throw new IllegalArgumentException("maps.yml のマップ/ワールドが変更されています。reloadしてからやり直してください。");
        }
        ConfigurationSection areas = map.getConfigurationSection("areas");
        if (area == null) {
            if (areas == null || !areas.contains(id)) throw new IllegalArgumentException("そのエリアIDは登録されていません。");
            areas.set(id, null);
        } else {
            area.getMin().checkFinite();
            area.getMax().checkFinite();
            if (areas == null) areas = map.createSection("areas");
            ConfigurationSection entry = areas.getConfigurationSection(id);
            if (entry == null) entry = areas.createSection(id);
            writePoint(entry, "min", area.getMin());
            writePoint(entry, "max", area.getMax());
        }
        // Validate every resulting area before changing disk or the live map.
        readAreas(map);
        AtomicConfigWriter.write(file, "hijack-area-", yaml.saveToString());
        return yaml;
    }

    public static Map<String, Area3D> readAreas(ConfigurationSection map) {
        Map<String, Area3D> result = new LinkedHashMap<>();
        var areas = map == null ? null : map.getConfigurationSection("areas");
        if (areas == null) return result;
        for (String id : areas.getKeys(false)) {
            var entry = areas.getConfigurationSection(id);
            if (entry == null) throw new IllegalArgumentException("不正な占領範囲: " + id);
            result.put(id, new Area3D(readPoint(entry, "min"), readPoint(entry, "max")));
        }
        return result;
    }

    private static Vector readPoint(ConfigurationSection section, String key) {
        for (String axis : new String[]{"x", "y", "z"}) {
            if (!(section.get(key + "." + axis) instanceof Number)) throw new IllegalArgumentException("範囲座標が不正です: " + key + "." + axis);
        }
        Vector point = new Vector(section.getDouble(key + ".x"), section.getDouble(key + ".y"), section.getDouble(key + ".z"));
        point.checkFinite();
        return point;
    }

    private static void writePoint(ConfigurationSection entry, String key, Vector point) {
        entry.set(key + ".x", point.getX());
        entry.set(key + ".y", point.getY());
        entry.set(key + ".z", point.getZ());
    }
}

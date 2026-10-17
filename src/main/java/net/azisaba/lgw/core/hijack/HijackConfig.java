package net.azisaba.lgw.core.hijack;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

/** Read/validate/save before publishing. Failed reloads and writes preserve the previous rules. */
public final class HijackConfig {
    private final Path file;
    private HijackRules pending = HijackRules.defaults();

    public HijackConfig(Path file) { this.file = file; }
    public Path getPath() { return file; }
    public HijackRules getPending() { return pending; }

    private YamlConfiguration read() throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = new YamlConfiguration();
        if (Files.exists(file)) yaml.loadFromString(Files.readString(file, StandardCharsets.UTF_8));
        else {
            try (var resource = HijackConfig.class.getResourceAsStream("/configs/hijack.yml")) {
                if (resource != null) yaml.loadFromString(new String(resource.readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return yaml;
    }

    public void reload() throws IOException, InvalidConfigurationException {
        YamlConfiguration yaml = read();
        boolean migrated = removeRetiredCaptureSettings(yaml);
        HijackRules next = HijackRules.read(yaml);
        if (!Files.exists(file) || migrated) save(yaml, next);
        pending = next;
    }

    public void set(String key, String text) throws IOException, InvalidConfigurationException {
        Object type = HijackRules.defaultsMap().get(key);
        if (type == null) throw new IllegalArgumentException("不明な設定: " + key);
        Object value;
        if (type instanceof Boolean) {
            if (!text.equalsIgnoreCase("true") && !text.equalsIgnoreCase("false")) throw new IllegalArgumentException("true または false を指定してください。");
            value = Boolean.parseBoolean(text);
        } else if (type instanceof Number) {
            try { value = Double.parseDouble(text); }
            catch (NumberFormatException exception) { throw new IllegalArgumentException("数値を指定してください。", exception); }
        } else if (type instanceof List<?>) {
            value = text.equals("[]") ? List.of() : Arrays.stream(text.split(",", -1)).map(String::trim).toList();
        } else value = text;
        YamlConfiguration yaml = read();
        removeRetiredCaptureSettings(yaml);
        yaml.set(key, value);
        HijackRules next = HijackRules.read(yaml);
        save(yaml, next);
        pending = next;
    }

    private static boolean removeRetiredCaptureSettings(YamlConfiguration yaml) {
        boolean changed = false;
        for (String key : HijackRules.RETIRED_CAPTURE_KEYS) {
            if (yaml.contains(key)) { yaml.set(key, null); changed = true; }
        }
        var capture = yaml.getConfigurationSection("capture");
        if (capture != null && capture.getKeys(false).isEmpty()) { yaml.set("capture", null); changed = true; }
        return changed;
    }

    private void save(YamlConfiguration yaml, HijackRules rules) throws IOException {
        rules.values().forEach(yaml::set);
        Path target = Files.exists(file) ? file.toRealPath() : file.toAbsolutePath();
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "hijack-", ".tmp");
        try {
            Files.writeString(temporary, yaml.saveToString(), StandardCharsets.UTF_8);
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException exception) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } finally { Files.deleteIfExists(temporary); }
    }
}

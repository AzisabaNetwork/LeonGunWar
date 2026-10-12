package net.azisaba.lgw.core.configs;

import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.LgwLog;
import net.azisaba.lgw.core.util.LegacyColors;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.Material;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.function.Consumer;

public class SyogoConfig extends Config {
    private final Logger logger = LgwLog.getLogger(this.getClass());

    public final HashMap<String, String> syogos = new HashMap<>();
    private volatile Map<String, Entry> entries = Map.of();

    public record Entry(String id, String display, Material icon, List<String> lore) {
        public Entry { lore = List.copyOf(lore); }
    }

    public Map<String, Entry> entries() { return entries; }

    public SyogoConfig(LeonGunWar plugin) {
        super(plugin, "configs/syogo.yml", "syogo.yml");
    }

    @SneakyThrows(value = {Exception.class})
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();
        entries = parse(config, message -> logger.warn(message));
        syogos.clear();
        entries.forEach((id, entry) -> syogos.put(id, entry.display()));
        logger.info("称号を " + syogos.size() + " 個読み込みました");
    }

    public static Map<String, Entry> parse(ConfigurationSection config, Consumer<String> warning) {
        Map<String, Entry> result = new HashMap<>();
        for (String id : config.getKeys(false)) {
            if (id.isBlank() || id.length() > 64 || id.indexOf('.') >= 0 || id.chars().anyMatch(Character::isWhitespace)) {
                warning.accept("syogo.yml: invalid ID: " + id);
                continue;
            }
            Object value = config.get(id);
            if (value instanceof String display && !display.isBlank()) {
                result.put(id, new Entry(id, LegacyColors.normalize(display), Material.NAME_TAG, List.of()));
            } else if (value instanceof ConfigurationSection section) {
                String display = section.getString("Displayname", "");
                Material icon = Material.matchMaterial(section.getString("GUIicon", "NAME_TAG"));
                if (display.isBlank() || icon == null || !icon.isItem() || icon.isAir()) {
                    warning.accept("syogo.yml: invalid Displayname/GUIicon: " + id);
                    continue;
                }
                List<String> lore = section.isList("lore") ? section.getStringList("lore")
                        : List.of(section.getString("lore", ""));
                result.put(id, new Entry(id, LegacyColors.normalize(display), icon, lore.stream()
                        .flatMap(line -> Arrays.stream(line.split("\\|", -1))).map(LegacyColors::normalize).toList()));
            } else warning.accept("syogo.yml: invalid title: " + id);
        }
        return Map.copyOf(result);
    }

    public void add(String syogo, String display) {
        config.set(syogo, display);
        refreshEntries();
    }

    public void delete(String syogo) {
        config.set(syogo, null);
        refreshEntries();
    }

    private void refreshEntries() {
        entries = parse(config, message -> logger.warn(message));
        syogos.clear();
        entries.forEach((id, entry) -> syogos.put(id, entry.display()));
    }

    public boolean exist(String syogo) {
        return syogos.containsKey(syogo);
    }

}

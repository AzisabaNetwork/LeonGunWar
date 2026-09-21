package net.azisaba.lgw.core.configs;

import lombok.NonNull;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class KillLogsConfig extends Config {

    private static final String DEFAULT_FORMAT =
            "{prefix}{syogo}{killer}&7━━━ [&r{weapon}&7] ━━━>&r{victim}";

    private String defaultFormat = DEFAULT_FORMAT;
    private List<KillLogStyle> styles = List.of();

    public KillLogsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/killLogs.yml", "killLogs.yml");
    }

    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        defaultFormat = config.getString("default-format", DEFAULT_FORMAT);
        List<KillLogStyle> loadedStyles = new ArrayList<>();
        ConfigurationSection stylesSection = config.getConfigurationSection("styles");
        if (stylesSection != null) {
            for (String key : stylesSection.getKeys(false)) {
                String permission = stylesSection.getString(key + ".permission");
                String format = stylesSection.getString(key + ".format");
                if (permission == null || permission.isBlank() || format == null || format.isBlank()) {
                    continue;
                }

                int priority = stylesSection.getInt(key + ".priority", 0);
                loadedStyles.add(new KillLogStyle(permission, format, priority));
            }
        }

        loadedStyles.sort(Comparator.comparingInt(KillLogStyle::priority).reversed());
        styles = List.copyOf(loadedStyles);
    }

    public String getFormat(Player player) {
        return styles.stream()
                .filter(style -> player.hasPermission(style.permission()))
                .map(KillLogStyle::format)
                .findFirst()
                .orElse(defaultFormat);
    }

    private record KillLogStyle(String permission, String format, int priority) {
    }
}

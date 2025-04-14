package net.azisaba.lgw.core.configs;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.InvalidConfigurationException;

@Getter
public class ItemsConfig extends Config {

    private List<String> victoryItemCrackShotIds;

    public ItemsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/items.yml", "items.yml");
    }

    @SneakyThrows(value = {Exception.class})
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        if (!config.isSet("victoryItems")) {
            victoryItemCrackShotIds = null;
        } else {
            victoryItemCrackShotIds = Collections.unmodifiableList(
                config.getStringList("victoryItems"));
        }
    }
}

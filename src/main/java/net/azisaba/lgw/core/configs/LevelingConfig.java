package net.azisaba.lgw.core.configs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.InvalidConfigurationException;

import net.azisaba.lgw.core.LeonGunWar;

import lombok.NonNull;
import lombok.SneakyThrows;

public class LevelingConfig extends Config{
    public Map<String, Integer> configmap = new HashMap<String, Integer>();

    public LevelingConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/leveling.yml", "leveling.yml");
        config.addDefault("killXP", 1);
        config.addDefault("winXP", 15);
        config.addDefault("feverBaseXP", 5);
        config.addDefault("mvpXP", 30); //TODO MVPってキルたくさんしてるわけだからいらないと思う
    }
    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();
        configmap = config.getValues(false);
    }
}

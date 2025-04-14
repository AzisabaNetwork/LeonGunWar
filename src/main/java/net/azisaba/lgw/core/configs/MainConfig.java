package net.azisaba.lgw.core.configs;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.HashMap;

public class MainConfig extends Config{

    public boolean isLobby = true;
    public String serverName;

    public MainConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/main.yml", "main.yml");
    }

    @SneakyThrows(value = {Exception.class})
    @Override
    public void loadConfig() {
        super.loadConfig();

        isLobby = config.getBoolean("isLobby", true);
        //sv1かsv2かlobby
        serverName = config.getString("servername","lobby");
    }

}

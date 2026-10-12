package net.azisaba.lgw.core.configs;

import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;

public class MainConfig extends Config {

    public boolean isLobby = true;
    public String serverName;
    public String lobbyServer = "lgw2";
    public long cosmeticsSyncIntervalTicks = 100;

    public MainConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/main.yml", "main.yml");
    }

    @SneakyThrows(value = {Exception.class})
    @Override
    public void loadConfig() {
        super.loadConfig();

        isLobby = config.getBoolean("isLobby", true);
        cosmeticsSyncIntervalTicks = Math.max(20L, config.getLong("cosmetics-sync-interval-ticks", 100L));
        //sv1かsv2かlobby
        serverName = config.getString("servername", "lobby");
        lobbyServer = config.getString("lobby-server", "lgw2").trim();
        if (lobbyServer.isEmpty()) {
            plugin.getLogger().warning("main.yml: lobby-server is blank; using lgw2.");
            lobbyServer = "lgw2";
        }
    }

}

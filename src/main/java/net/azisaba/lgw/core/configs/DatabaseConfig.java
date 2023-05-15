package net.azisaba.lgw.core.configs;

import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.IOException;

public class DatabaseConfig extends Config{

    private boolean enabled;
    private String host;
    private int port;
    private String database;
    private String user;
    private String password;

    public DatabaseConfig(LeonGunWar plugin) {
        super(plugin, "configs/database.yml", "database.yml");
    }

    @SneakyThrows(value = { Exception.class })
    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        this.enabled = config.getBoolean("enable",false);
        this.host = config.getString("host","HOST");
        this.port = config.getInt("port",3306);
        this.database = config.getString("database","conflict");
        this.user = config.getString("user","conflict");
        this.password = config.getString("password","password");
    }

    public boolean isEnabled(){
        return this.enabled;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getDatabase() {
        return database;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

}

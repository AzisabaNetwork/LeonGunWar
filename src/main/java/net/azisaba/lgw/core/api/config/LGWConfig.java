package net.azisaba.lgw.core.api.config;

import de.exlll.configlib.Configuration;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class LGWConfig {
    public MainConfig mainConfig = new MainConfig();
    public DatabaseConfig database = new DatabaseConfig();
    public ItemsConfig itemsConfig = new ItemsConfig();
    public SpawnsConfig spawnsConfig = new SpawnsConfig();
    public SyogoConfig syogoConfig = new SyogoConfig();
    public WeaponControlConfig weaponControlConfig = new WeaponControlConfig();

    @Configuration
    public static class MainConfig {
        public boolean isLobby = true;
        public String serverName = "lobby";
    }

    @Configuration
    public static class DatabaseConfig {
        public String host = "127.0.0.1";
        public int port = 3306;
        public String database = "leongunwar";
        public String username = "lgwuser";
        public String password = "lgwpass";
    }

    @Configuration
    public static class ItemsConfig {
        public List<String> victoryItemCrackShotIds = new ArrayList<>();
    }

    @Configuration
    public static class SpawnsConfig {
        public Map<String, Location> spawns = new HashMap<>();
        public Location lobby = new Location(Bukkit.getWorld("world"), 0, 0, 0);
        public Location onsen = new Location(Bukkit.getWorld("world"), 0, 0, 0);
    }

    @Configuration
    public static class SyogoConfig {
        public Map<String, String> syogos = new HashMap<>();
    }

    @Configuration
    public static class WeaponControlConfig {
        public Map<String, Integer> rateLimitedWeapons = new HashMap<>();
    }
}

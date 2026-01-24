package net.azisaba.lgw.core.configs;

import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Getter
public class SpawnsConfig extends Config {

    // 位置データは“文字列の world 名＋座標”だけ持つ
    private static class Pos {
        final String worldName;
        final double x, y, z;
        final float yaw, pitch;
        Pos(String worldName, double x, double y, double z, float yaw, float pitch) {
            this.worldName = worldName; this.x=x; this.y=y; this.z=z; this.yaw=yaw; this.pitch=pitch;
        }
    }

    private Map<String, Pos> spawns;  // ← Location ではなく Pos を保存

    public SpawnsConfig(@NonNull LeonGunWar plugin) {
        super(plugin, "configs/spawns.yml", "spawns.yml");
    }

    @Override
    public void loadConfig() throws IOException, InvalidConfigurationException {
        super.loadConfig();

        Map<String, Pos> map = new HashMap<>();
        for (String spawnName : config.getValues(false).keySet()) {
            String w = config.getString(spawnName + ".world");
            Pos p = new Pos(
                    w,
                    config.getDouble(spawnName + ".x"),
                    config.getDouble(spawnName + ".y"),
                    config.getDouble(spawnName + ".z"),
                    (float) config.getDouble(spawnName + ".yaw"),
                    (float) config.getDouble(spawnName + ".pitch")
            );
            map.put(spawnName, p);
        }
        spawns = Collections.unmodifiableMap(map);
    }

    /** 使う直前に World を解決。null のときは null を返す（呼び出し側でフォールバック） */
    public @Nullable Location get(String name) {
        Pos p = spawns.get(name);
        if (p == null) {
            Bukkit.getLogger().warning("[LGW] spawn '" + name + "' not found in spawns.yml");
            return null;
        }
        World w = plugin.getServer().getWorld(p.worldName);
        if (w == null) {
            Bukkit.getLogger().warning("[LGW] spawn '" + name + "' world not loaded or not found: " + p.worldName);
            return null;
        }
        return new Location(w, p.x, p.y, p.z, p.yaw, p.pitch);
    }

    public @Nullable Location getLobby() { return get("lobby"); }
    public @Nullable Location getOnsen() { return get("onsen"); }
}
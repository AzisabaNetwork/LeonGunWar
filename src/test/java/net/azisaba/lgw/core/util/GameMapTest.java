package net.azisaba.lgw.core.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameMapTest {
    @Test
    void mapBecomesReadyOnlyAfterBothSpawnsHaveLoadedWorlds() {
        World world = (World) Proxy.newProxyInstance(World.class.getClassLoader(), new Class<?>[]{World.class},
                (proxy, method, args) -> { throw new UnsupportedOperationException(method.getName()); });
        var spawns = new HashMap<BattleTeam, Location>();
        GameMap map = new GameMap("New Arena", world, spawns, List.of());
        assertFalse(map.isReady());
        spawns.put(BattleTeam.RED, new Location(world, 0.5, 64, 0.5));
        assertFalse(map.isReady());
        spawns.put(BattleTeam.BLUE, new Location(world, 20.5, 64, 20.5));
        assertTrue(map.isReady());
        spawns.put(BattleTeam.BLUE, new Location(null, 20.5, 64, 20.5));
        assertFalse(map.isReady());
        assertFalse(new GameMap("Unloaded", null, spawns, List.of()).isReady());
    }
}

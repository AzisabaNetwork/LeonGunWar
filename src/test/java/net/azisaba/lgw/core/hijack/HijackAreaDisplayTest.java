package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.configs.MapsConfig;
import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HijackAreaDisplayTest {
    record Draw(Player viewer, Location location, Particle.DustOptions dust) { }
    LeonGunWar plugin;
    HijackController controller;
    HijackAreaDisplay display;
    Server server;
    World world;
    Player admin, observer;
    final List<Draw> draws = new ArrayList<>();
    HijackController.AreaView view;

    @BeforeEach void setup() {
        plugin = mock(LeonGunWar.class); controller = mock(HijackController.class);
        server = mock(Server.class); world = mock(World.class);
        when(plugin.getServer()).thenReturn(server);
        when(world.getUID()).thenReturn(UUID.randomUUID());
        when(server.getWorld(world.getUID())).thenReturn(world);
        var area = new Area3D(new Vector(-5, 64, -5), new Vector(5, 66, 5));
        view = new HijackController.AreaView(world.getUID(), "Shagger", List.of(area));
        admin = player(); observer = player();
        when(world.getPlayers()).thenReturn(List.of(admin, observer));
        var maps = mock(MapsConfig.class); when(plugin.getMapsConfig()).thenReturn(maps);
        var map = new GameMap("Shagger", world, Map.of(), List.of(area));
        when(maps.getAllGameMap()).thenReturn(List.of(map));
        when(controller.nextAreaView(map)).thenReturn(view);
        var manager = mock(MatchManager.class); when(plugin.getManager()).thenReturn(manager);
        when(manager.getCurrentGameMap()).thenReturn(map);
        display = new HijackAreaDisplay(plugin, controller);
    }
    private Player player() {
        Player player = mock(Player.class);
        UUID id = UUID.randomUUID();
        when(player.getUniqueId()).thenReturn(id);
        when(server.getPlayer(id)).thenReturn(player);
        when(player.isOnline()).thenReturn(true);
        when(player.hasPermission(anyString())).thenReturn(true);
        when(player.getWorld()).thenReturn(world);
        when(player.getLocation()).thenReturn(new Location(world, 0, 64, 0));
        doAnswer(call -> {
            draws.add(new Draw(player, call.getArgument(1), call.getArgument(7)));
            return null;
        }).when(player).spawnParticle(eq(Particle.REDSTONE), any(Location.class), eq(1), eq(0d), eq(0d), eq(0d), eq(0d), any(Particle.DustOptions.class));
        return player;
    }
    @Test void matchShowsOnlyFloorEdgesInTheCurrentTeamColorAndWhiteWhenUnoccupiedOrContested() {
        when(controller.activeAreaView()).thenReturn(view);
        for (BattleTeam team : new BattleTeam[]{BattleTeam.RED, BattleTeam.BLUE, null}) {
            when(controller.getCapturingTeam()).thenReturn(team);
            draws.clear(); display.pulse(); display.pulse();
            assertFalse(draws.isEmpty());
            Color expected = team == null ? Color.WHITE : team.getColor();
            assertTrue(draws.stream().allMatch(draw -> draw.location.getY() == 64 && draw.dust.getColor().equals(expected)));
            assertTrue(draws.stream().allMatch(draw -> draw.dust.getSize() == .6f));
        }
        when(controller.activeAreaView()).thenReturn(null);
        draws.clear(); display.pulse(); assertTrue(draws.isEmpty());
    }
    @Test void previewIsPrivateDrawsTheFinalBoxAndExpiresWithoutChangingActiveRules() {
        display.preview(admin, true, "Shagger");
        display.pulse();
        assertFalse(draws.isEmpty());
        assertTrue(draws.stream().allMatch(draw -> draw.viewer == admin));
        assertTrue(draws.stream().anyMatch(draw -> draw.location.getY() == 66));
        verify(admin).sendMessage(contains("Y 64.00～66.00"));
        verify(admin).sendMessage(contains("現在の足元"));
        for (int i = 1; i < 60; i++) display.pulse();
        draws.clear(); display.pulse(); assertTrue(draws.isEmpty());
        verify(controller, never()).begin(any());
        assertThrows(IllegalArgumentException.class, () -> display.preview(admin, true, "Missing"));
        assertThrows(IllegalArgumentException.class, () -> display.preview(admin, false, ""));
    }
    @Test void previewStopsOnExplicitStopDisconnectWorldChangeAndPermissionLossAndRespectsDistance() {
        for (int reason = 0; reason < 4; reason++) {
            when(admin.isOnline()).thenReturn(true);
            when(admin.hasPermission(anyString())).thenReturn(true);
            when(admin.getWorld()).thenReturn(world);
            display.preview(admin, true, "Shagger");
            if (reason == 0) display.stopPreview(admin);
            if (reason == 1) when(admin.isOnline()).thenReturn(false);
            if (reason == 2) {
                var other = mock(World.class); when(other.getUID()).thenReturn(UUID.randomUUID());
                when(admin.getWorld()).thenReturn(other);
            }
            if (reason == 3) when(admin.hasPermission(anyString())).thenReturn(false);
            draws.clear(); display.pulse(); assertTrue(draws.isEmpty());
        }
        when(admin.hasPermission(anyString())).thenReturn(true);
        when(admin.getLocation()).thenReturn(new Location(world, 1000, 64, 1000));
        display.preview(admin, true, "Shagger");
        draws.clear(); display.pulse(); assertTrue(draws.isEmpty());
    }
    @Test void taskStartsOnceAndShutdownCancelsItAndClearsPreviews() {
        var scheduler = mock(BukkitScheduler.class); var task = mock(BukkitTask.class);
        when(server.getScheduler()).thenReturn(scheduler);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), eq(10L), eq(10L))).thenReturn(task);
        display.start(); display.start();
        verify(scheduler).runTaskTimer(eq(plugin), any(Runnable.class), eq(10L), eq(10L));
        display.preview(admin, true, "Shagger");
        display.close(); verify(task).cancel();
        draws.clear(); display.pulse(); assertTrue(draws.isEmpty());
    }
}

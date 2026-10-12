package net.azisaba.lgw.core.util;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MatchPlayerReturnTest {
    private Plugin plugin;
    private Player player;
    private Location lobby;
    private World world;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        player = mock(Player.class);
        when(player.getName()).thenReturn("TestPlayer");
        when(player.isOnline()).thenReturn(true);
        world = mock(World.class);
        lobby = new Location(world, -911.5, 161, -999.5, 90, 0);
    }

    @Test
    void missingLobbyDoesNotPreventDeadPlayersArmorAndTagsFromBeingCleared() {
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(player.isDead()).thenReturn(true);

        assertDoesNotThrow(() -> {
            MatchPlayerReturn.reset(player);
            MatchPlayerReturn.returnToLobby(plugin, player, null, () -> true);
        });

        verify(inventory).setChestplate(null);
        verify(player).removeScoreboardTag("red");
        verify(player).removeScoreboardTag("blue");
        verify(player).setFoodLevel(20);
        verify(player, never()).setHealth(anyDouble());
        verify(player, never()).teleport(any(Location.class));
        verify(player).sendMessage(anyString());
    }

    @Test
    void missingWorldIsReportedWithoutThrowing() {
        assertDoesNotThrow(() -> MatchPlayerReturn.returnToLobby(plugin, player,
                new Location(null, 1, 2, 3), () -> true));
        verify(player, never()).teleport(any(Location.class));
        verify(player).sendMessage(anyString());
    }

    @Test
    void healthyPlayerReturnsToExactConfiguredLocation() {
        when(player.teleport(any(Location.class))).thenReturn(true);
        MatchPlayerReturn.returnToLobby(plugin, player, lobby, () -> true);
        var destination = ArgumentCaptor.forClass(Location.class);
        verify(player).teleport(destination.capture());
        assertEquals(lobby, destination.getValue());
        assertNotSame(lobby, destination.getValue());
        verify(player, never()).sendMessage(anyString());
    }

    @Test
    void cancelledTeleportIsReported() {
        when(player.teleport(any(Location.class))).thenReturn(false);
        assertDoesNotThrow(() -> MatchPlayerReturn.returnToLobby(plugin, player, lobby, () -> true));
        verify(player).sendMessage(anyString());
    }

    @Test
    void teleportExceptionDoesNotAbortNextPlayersReturn() {
        when(player.teleport(any(Location.class))).thenThrow(new IllegalArgumentException("world unavailable"));
        Player next = mock(Player.class);
        when(next.isOnline()).thenReturn(true);
        when(next.teleport(any(Location.class))).thenReturn(true);
        assertDoesNotThrow(() -> {
            MatchPlayerReturn.returnToLobby(plugin, player, lobby, () -> true);
            MatchPlayerReturn.returnToLobby(plugin, next, lobby, () -> true);
        });
        verify(next).teleport(lobby);
    }

    @Test
    void pendingDeathReturnDoesNotPullPlayerOutOfNextMatch() {
        when(player.isDead()).thenReturn(true);
        Player.Spigot spigot = mock(Player.Spigot.class);
        when(player.spigot()).thenReturn(spigot);
        Server server = mock(Server.class);
        BukkitScheduler scheduler = mock(BukkitScheduler.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        AtomicBoolean mayReturn = new AtomicBoolean(true);

        MatchPlayerReturn.returnToLobby(plugin, player, lobby, mayReturn::get);
        verify(spigot).respawn();
        var callback = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(eq(plugin), callback.capture());
        when(player.isDead()).thenReturn(false);
        mayReturn.set(false);
        callback.getValue().run();
        verify(player, never()).teleport(any(Location.class));
        verify(player, never()).getInventory();
    }

    @Test
    void offlinePlayerIsNotTeleported() {
        when(player.isOnline()).thenReturn(false);
        MatchPlayerReturn.returnToLobby(plugin, player, lobby, () -> true);
        verify(player, never()).teleport(any(Location.class));
    }
}

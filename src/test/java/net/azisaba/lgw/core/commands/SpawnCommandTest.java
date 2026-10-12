package net.azisaba.lgw.core.commands;

import com.google.common.io.ByteStreams;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.MainConfig;
import net.azisaba.lgw.core.configs.SpawnsConfig;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SpawnCommandTest {
    @Test
    void spawnIsPublicEvenWhenPermissionDefaultsAreDisabled() throws Exception {
        try (var stream = getClass().getResourceAsStream("/plugin.yml")) {
            assertNotNull(stream);
            var description = new PluginDescriptionFile(stream);
            assertNull(description.getCommands().get("spawn").get("permission"));
            assertEquals("leongunwar.command.leongunwaradmin",
                    description.getCommands().get("leongunwaradmin").get("permission"));
            assertTrue(description.getPermissions().stream().anyMatch(p ->
                    p.getName().equals("leongunwar.command.leongunwaradmin")
                            && p.getDefault() == PermissionDefault.OP));
            assertTrue(description.getSoftDepend().contains("EssentialsSpawn"));
        }
    }

    @Test
    void registrationUsesOwnedSpawnAndReplacesOnlyTheBareCommand() {
        LeonGunWar plugin = mock(LeonGunWar.class);
        PluginCommand ownSpawn = mock(PluginCommand.class);
        Command essentialsSpawn = mock(Command.class);
        CommandExecutor executor = mock(CommandExecutor.class);
        Server server = mock(Server.class);
        CommandMap commandMap = mock(CommandMap.class);
        var known = new HashMap<String, Command>();
        known.put("spawn", essentialsSpawn);
        known.put("essentials:spawn", essentialsSpawn);
        known.put("leongunwar:spawn", ownSpawn);
        when(plugin.getCommand("spawn")).thenReturn(ownSpawn);
        when(plugin.getServer()).thenReturn(server);
        when(server.getCommandMap()).thenReturn(commandMap);
        when(commandMap.getKnownCommands()).thenReturn(known);
        when(plugin.registerCommand("spawn", executor)).thenCallRealMethod();

        assertSame(ownSpawn, plugin.registerCommand("spawn", executor));
        assertSame(ownSpawn, known.get("spawn"));
        assertSame(ownSpawn, known.get("leongunwar:spawn"));
        assertSame(essentialsSpawn, known.get("essentials:spawn"));
        verify(ownSpawn).setExecutor(executor);
        verifyNoInteractions(essentialsSpawn);
    }

    @Test
    void otherCommandsDoNotReplaceBareCommands() {
        LeonGunWar plugin = mock(LeonGunWar.class);
        PluginCommand ownCommand = mock(PluginCommand.class);
        CommandExecutor executor = mock(CommandExecutor.class);
        when(plugin.getCommand("mapvote")).thenReturn(ownCommand);
        when(plugin.registerCommand("mapvote", executor)).thenCallRealMethod();

        assertSame(ownCommand, plugin.registerCommand("mapvote", executor));
        verify(ownCommand).setExecutor(executor);
        verify(plugin, never()).getServer();
    }

    @Test
    void ordinaryLobbyPlayerReturnsToConfiguredLocalSpawn() {
        var plugin = mock(LeonGunWar.class);
        var config = mock(MainConfig.class);
        config.isLobby = true;
        var spawns = mock(SpawnsConfig.class);
        var lobby = new Location(mock(World.class), 12.5, 64, -35.5, 90, 15);
        var player = mock(Player.class);
        when(plugin.getMainConfig()).thenReturn(config);
        when(plugin.getSpawnsConfig()).thenReturn(spawns);
        when(spawns.getLobby()).thenReturn(lobby);
        when(player.teleport(lobby)).thenReturn(true);

        try (var lgw = mockStatic(LeonGunWar.class)) {
            lgw.when(LeonGunWar::getPlugin).thenReturn(plugin);
            assertTrue(new SpawnCommand().onCommand(player, mock(Command.class), "spawn", new String[0]));
        }
        verify(player).teleport(lobby);
        verify(player, never()).hasPermission(anyString());
        verify(player, never()).sendPluginMessage(any(), anyString(), any());
    }

    @Test
    void matchPlayerIsSentToConfiguredLobbyServerWithoutOperatorPermission() {
        var plugin = mock(LeonGunWar.class);
        var config = mock(MainConfig.class);
        config.isLobby = false;
        config.lobbyServer = "lgw2test";
        var player = mock(Player.class);
        when(plugin.getMainConfig()).thenReturn(config);

        try (var lgw = mockStatic(LeonGunWar.class)) {
            lgw.when(LeonGunWar::getPlugin).thenReturn(plugin);
            assertTrue(new SpawnCommand().onCommand(player, mock(Command.class), "spawn", new String[0]));
        }
        var message = ArgumentCaptor.forClass(byte[].class);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), message.capture());
        var input = ByteStreams.newDataInput(message.getValue());
        assertEquals("Connect", input.readUTF());
        assertEquals("lgw2test", input.readUTF());
        verify(player, never()).hasPermission(anyString());
        verify(player, never()).teleport(any(Location.class));
    }

    @Test
    void cancelledLobbyTeleportIsReportedToPlayer() {
        var plugin = mock(LeonGunWar.class);
        var config = mock(MainConfig.class);
        config.isLobby = true;
        var spawns = mock(SpawnsConfig.class);
        var lobby = new Location(mock(World.class), 1, 2, 3);
        var player = mock(Player.class);
        when(plugin.getMainConfig()).thenReturn(config);
        when(plugin.getSpawnsConfig()).thenReturn(spawns);
        when(spawns.getLobby()).thenReturn(lobby);
        when(player.teleport(lobby)).thenReturn(false);

        try (var lgw = mockStatic(LeonGunWar.class)) {
            lgw.when(LeonGunWar::getPlugin).thenReturn(plugin);
            assertTrue(new SpawnCommand().onCommand(player, mock(Command.class), "spawn", new String[0]));
        }
        verify(player).sendMessage(contains("移動がキャンセル"));
    }
}

package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.commands.LgwAdminCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HijackAdminReloadTest {
    @TempDir Path directory;
    @Test void generalReloadAndAliasReadHijackFile() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml")); config.reload();
        var plugin = mock(LeonGunWar.class, RETURNS_DEEP_STUBS);
        var world = mock(org.bukkit.World.class); when(world.getUID()).thenReturn(java.util.UUID.randomUUID());
        var map = new net.azisaba.lgw.core.util.GameMap("Shagger", world, java.util.Map.of(), java.util.List.of());
        when(plugin.getManager().isMatching()).thenReturn(true);
        when(plugin.getManager().getMatchMode()).thenReturn(net.azisaba.lgw.core.util.MatchMode.HIJACK);
        when(plugin.getManager().getTimeLeft()).thenReturn(new java.util.concurrent.atomic.AtomicInteger(600));
        when(plugin.getMapsConfig().getAllGameMap()).thenReturn(java.util.List.of(map));
        var controller = new HijackController(plugin, config); when(plugin.getHijackController()).thenReturn(controller);
        controller.begin(map); controller.freezeStartingPlayers(2);
        var sender = mock(CommandSender.class);
        try (var staticPlugin = mockStatic(LeonGunWar.class)) {
            staticPlugin.when(LeonGunWar::getPlugin).thenReturn(plugin);
            Files.writeString(config.getPath(), "victory: {target-points: 321}\n");
            new LgwAdminCommand().onCommand(sender, mock(Command.class), "lgwadmin", new String[]{"reload"});
            assertEquals(321, config.getPending().points());
            assertEquals(321, controller.getActive().points());
            Files.writeString(config.getPath(), "victory: {target-points: 432}\n");
            new LgwAdminCommand().onCommand(sender, mock(Command.class), "lgwadmin", new String[]{"rl"});
            assertEquals(432, config.getPending().points());
            assertEquals(432, controller.getActive().points());
        }
        verify(sender, times(2)).sendMessage(contains("現在の試合"));
    }
    @Test void invalidHijackReloadKeepsPendingRulesAndDoesNotReportSuccess() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml")); config.reload(); config.set("victory.target-points", "321");
        var plugin = mock(LeonGunWar.class, RETURNS_DEEP_STUBS);
        when(plugin.getHijackController().getConfig()).thenReturn(config);
        var sender = mock(CommandSender.class);
        Files.writeString(config.getPath(), "resistance: {percent: 200}\n");
        try (var staticPlugin = mockStatic(LeonGunWar.class)) {
            staticPlugin.when(LeonGunWar::getPlugin).thenReturn(plugin);
            new LgwAdminCommand().onCommand(sender, mock(Command.class), "lgwadmin", new String[]{"reload"});
        }
        assertEquals(321, config.getPending().points());
        verify(sender).sendMessage(contains("失敗"));
        verify(sender, never()).sendMessage(contains("完了"));
    }
}

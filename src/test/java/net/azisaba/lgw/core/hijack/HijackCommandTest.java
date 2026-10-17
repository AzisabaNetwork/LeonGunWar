package net.azisaba.lgw.core.hijack;

import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HijackCommandTest {
    @TempDir Path directory;
    @Test void permissionFailureDoesNotWriteAndPendingRulesAreVisible() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml"));
        var controller = mock(HijackController.class);
        when(controller.getConfig()).thenReturn(config);
        var command = new HijackCommand(controller, mock(HijackAreaDisplay.class), mock(Logger.class));
        var sender = mock(CommandSender.class);
        command.execute(sender, new String[]{"hijack", "set", "victory.target-points", "400"});
        assertFalse(Files.exists(config.getPath()));
        when(sender.hasPermission("leongunwar.command.leongunwaradmin")).thenReturn(true);
        when(controller.isRunning()).thenReturn(true);
        when(controller.getActive()).thenReturn(HijackRules.defaults());
        command.execute(sender, new String[]{"hijack", "set", "victory.target-points", "400"});
        command.execute(sender, new String[]{"hijack", "show"});
        assertEquals(400, config.getPending().points());
        verify(sender).sendMessage(startsWith("§6[適用中]")); verify(sender).sendMessage(startsWith("§6[次の試合]"));
        verify(sender).sendMessage(contains("未実装・無効"));
    }
    @Test void completionAndCommandRejectUnimplementedRewardActivation() {
        var config = new HijackConfig(directory.resolve("hijack.yml"));
        var controller = mock(HijackController.class); when(controller.getConfig()).thenReturn(config);
        var command = new HijackCommand(controller, mock(HijackAreaDisplay.class), mock(Logger.class));
        var sender = mock(CommandSender.class); when(sender.hasPermission(anyString())).thenReturn(true);
        assertTrue(command.complete(sender, new String[]{"hijack", "set", "resistance."}).contains("resistance.percent"));
        assertEquals(java.util.List.of("false"), command.complete(sender, new String[]{"hijack", "set", "rewards.area-kill.enabled", ""}));
        assertTrue(command.complete(sender, new String[]{"hijack", "set", "victory-item.mode", ""}).contains("none"));
        assertTrue(command.complete(sender, new String[]{"hijack", "set", "capture."}).isEmpty());
        assertTrue(command.complete(sender, new String[]{"hijack", "set", "resistance.target", ""}).contains("capturing"));
        assertTrue(command.complete(sender, new String[]{"hijack", "set", "explosion-resistance.target", ""}).contains("capturing"));
        assertEquals(java.util.List.of("1:100,6:150,12:200"), command.complete(sender,
                new String[]{"hijack", "set", "victory.player-count.targets", ""}));
        command.execute(sender, new String[]{"hijack", "set", "rewards.area-kill.enabled", "true"});
        verify(sender).sendMessage(contains("未実装")); assertFalse(Files.exists(config.getPath()));
    }
    @Test void previewCommandsRequirePermissionAndPlayersAndCompleteModesAndMapNames() {
        var controller = mock(HijackController.class);
        var display = mock(HijackAreaDisplay.class);
        var command = new HijackCommand(controller, display, mock(Logger.class));
        var player = mock(org.bukkit.entity.Player.class);
        command.execute(player, new String[]{"hijack", "preview"}); verifyNoInteractions(display);
        when(player.hasPermission(anyString())).thenReturn(true);
        when(controller.isRunning()).thenReturn(true);
        command.execute(player, new String[]{"hijack", "preview"}); verify(display).preview(player, false, "");
        command.execute(player, new String[]{"hijack", "preview", "next", "New", "Arena"});
        verify(display).preview(player, true, "New Arena");
        command.execute(player, new String[]{"hijack", "preview", "stop"}); verify(display).stopPreview(player);
        var console = mock(CommandSender.class); when(console.hasPermission(anyString())).thenReturn(true);
        command.execute(console, new String[]{"hijack", "preview"}); verify(console).sendMessage(contains("プレイヤー専用"));
        when(display.mapNames()).thenReturn(java.util.List.of("Shagger"));
        assertTrue(command.complete(player, new String[]{"hijack", "pre"}).contains("preview"));
        assertEquals(java.util.List.of("active", "next", "stop"), command.complete(player, new String[]{"hijack", "preview", ""}));
        assertEquals(java.util.List.of("Shagger"), command.complete(player, new String[]{"hijack", "preview", "next", "Sh"}));
    }
}

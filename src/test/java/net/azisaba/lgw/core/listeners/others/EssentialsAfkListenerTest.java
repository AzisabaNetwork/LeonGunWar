package net.azisaba.lgw.core.listeners.others;

import com.google.common.io.ByteStreams;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.configs.MainConfig;
import net.azisaba.lgw.core.util.AfkMatchReturn;
import net.ess3.api.IUser;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EssentialsAfkListenerTest {
    private LeonGunWar plugin;
    private MatchManager manager;
    private Player player;
    private MainConfig config;
    private BukkitScheduler scheduler;
    private EssentialsAfkListener listener;

    @BeforeEach
    void setUp() {
        plugin = mock(LeonGunWar.class);
        manager = mock(MatchManager.class);
        player = mock(Player.class);
        config = mock(MainConfig.class);
        config.isLobby = false;
        config.lobbyServer = "lgw2test";
        Server server = mock(Server.class);
        scheduler = mock(BukkitScheduler.class);
        when(plugin.getManager()).thenReturn(manager);
        when(plugin.getMainConfig()).thenReturn(config);
        when(plugin.getServer()).thenReturn(server);
        when(server.getScheduler()).thenReturn(scheduler);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(player.isOnline()).thenReturn(true);
        when(manager.isPlayerMatching(player)).thenReturn(true);
        listener = new EssentialsAfkListener(plugin);
    }

    private AfkStatusChangeEvent event(boolean afk, boolean cancelled) {
        AfkStatusChangeEvent event = mock(AfkStatusChangeEvent.class);
        IUser user = mock(IUser.class);
        when(event.getAffected()).thenReturn(user);
        when(user.getBase()).thenReturn(player);
        when(event.getValue()).thenReturn(afk);
        when(event.isCancelled()).thenReturn(cancelled);
        return event;
    }

    @Test
    void afkStatusSchedulesCleanupAndTransferOnMainThread() {
        listener.onAfk(event(true, false));
        verifyNoInteractions(manager);
        var task = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(eq(plugin), task.capture());
        task.getValue().run();

        var order = inOrder(manager, player);
        order.verify(manager).removeEntryPlayer(player);
        order.verify(manager).kickPlayer(player);
        var packet = ArgumentCaptor.forClass(byte[].class);
        order.verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), packet.capture());
        var input = ByteStreams.newDataInput(packet.getValue());
        assertEquals("Connect", input.readUTF());
        assertEquals("lgw2test", input.readUTF());
    }

    @Test
    void cancelledAfkAndAfkRemovalDoNotTriggerReturn() {
        listener.onAfk(event(true, true));
        listener.onAfk(event(false, false));
        verifyNoInteractions(scheduler, manager);
    }

    @Test
    void lobbyTestMatchUsesLocalReturnOnly() {
        config.isLobby = true;
        AfkMatchReturn.leave(plugin, player);
        verify(manager).kickPlayer(player);
        verify(player, never()).sendPluginMessage(any(), anyString(), any());
    }

    @Test
    void timerAndEssentialsDoNotReturnTheSamePlayerTwice() {
        when(manager.isPlayerMatching(player)).thenReturn(true, false);
        AfkMatchReturn.leave(plugin, player);
        AfkMatchReturn.leave(plugin, player);
        verify(manager).kickPlayer(player);
        verify(player).sendPluginMessage(eq(plugin), eq("BungeeCord"), any());
    }

    @Test
    void playersWhoLeftBeforeCallbackAreNotTeleported() {
        listener.onAfk(event(true, false));
        var task = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTask(eq(plugin), task.capture());
        when(player.isOnline()).thenReturn(false);
        task.getValue().run();
        verify(manager, never()).kickPlayer(any());
        verify(player, never()).sendPluginMessage(any(), anyString(), any());
    }

    @Test
    void nonParticipantsAreNotTransferred() {
        when(manager.isPlayerMatching(player)).thenReturn(false);
        AfkMatchReturn.leave(plugin, player);
        verify(manager, never()).removeEntryPlayer(any());
        verify(manager, never()).kickPlayer(any());
        verify(player, never()).sendPluginMessage(any(), anyString(), any());
    }
}

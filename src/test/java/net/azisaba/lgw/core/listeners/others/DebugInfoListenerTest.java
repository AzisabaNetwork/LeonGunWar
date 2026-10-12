package net.azisaba.lgw.core.listeners.others;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.GameRules;
import org.bukkit.Registry;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

class DebugInfoListenerTest {
    @BeforeAll
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void initializeGameRules() {
        // Paper's registry is supplied by the running server. Mock that boundary only;
        // these tests exercise the world rule's Boolean result, not registry contents.
        try (var staticAccess = mockStatic(RegistryAccess.class)) {
            RegistryAccess access = mock(RegistryAccess.class);
            staticAccess.when(RegistryAccess::registryAccess).thenReturn(access);
            when(access.getRegistry(any(Class.class))).thenAnswer(ignored -> mock(Registry.class));
            when(access.getRegistry(any(RegistryKey.class))).thenAnswer(ignored -> mock(Registry.class));
            var ignored = GameRules.REDUCED_DEBUG_INFO;
        }
    }

    private Plugin plugin;
    private Player player;
    private DebugInfoListener.Sender sender;
    private DebugInfoListener listener;
    private Server server;
    private BukkitScheduler scheduler;

    @BeforeEach
    void setUp() {
        plugin = mock(Plugin.class);
        player = mock(Player.class);
        sender = mock(DebugInfoListener.Sender.class);
        server = mock(Server.class);
        scheduler = mock(BukkitScheduler.class);
        when(plugin.getServer()).thenReturn(server);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        when(server.getScheduler()).thenReturn(scheduler);
        doReturn(List.of(player)).when(server).getOnlinePlayers();
        when(player.isOnline()).thenReturn(true);
        listener = new DebugInfoListener(plugin, sender);
    }

    @Test
    void ordinaryPlayerGetsReducedDebugInfoWithoutNeedingLuckPermsChanges() throws Exception {
        listener.apply(player);
        verify(sender).send(player, true);
    }

    @Test
    void operatorGetsFullDebugInfoEvenWhenPermissionDefaultsAreDisabled() throws Exception {
        when(player.isOp()).thenReturn(true);
        listener.apply(player);
        verify(sender).send(player, false);
    }

    @Test
    void nonOperatorWithLgwAdminPermissionGetsFullDebugInfo() throws Exception {
        when(player.hasPermission("leongunwar.command.leongunwaradmin")).thenReturn(true);
        listener.apply(player);
        verify(sender).send(player, false);
    }

    @Test
    void permissionRevocationIsAppliedOnNextRefresh() throws Exception {
        when(player.hasPermission("leongunwar.command.leongunwaradmin")).thenReturn(true, false);
        listener.apply(player);
        listener.apply(player);
        var order = inOrder(sender);
        order.verify(sender).send(player, false);
        order.verify(sender).send(player, true);
    }

    @Test
    void offlinePlayersDoNotReceivePackets() {
        when(player.isOnline()).thenReturn(false);
        listener.apply(player);
        verifyNoInteractions(sender);
    }

    @Test
    void loginWorldChangeAndRespawnApplyAfterVanillaReset() throws Exception {
        listener.onJoin(new PlayerJoinEvent(player, (net.kyori.adventure.text.Component) null));
        listener.onWorldChange(new PlayerChangedWorldEvent(player, mock(World.class)));
        PlayerRespawnEvent respawn = mock(PlayerRespawnEvent.class);
        when(respawn.getPlayer()).thenReturn(player);
        listener.onRespawn(respawn);

        verifyNoInteractions(sender);
        var callback = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler, times(3)).runTask(eq(plugin), callback.capture());
        for (Runnable task : callback.getAllValues()) task.run();
        verify(sender, times(3)).send(player, true);
    }

    @Test
    void timerRefreshesOnlyOnlinePlayersAndIsCancelledOnDisable() throws Exception {
        BukkitTask task = mock(BukkitTask.class);
        when(scheduler.runTaskTimer(eq(plugin), any(Runnable.class), eq(1L), eq(20L))).thenReturn(task);
        listener.start();
        listener.start();
        var callback = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).runTaskTimer(eq(plugin), callback.capture(), eq(1L), eq(20L));
        callback.getValue().run();
        verify(sender).send(player, true);

        World world = mock(World.class);
        when(player.getWorld()).thenReturn(world);
        when(world.getGameRuleValue(GameRules.REDUCED_DEBUG_INFO)).thenReturn(false);
        listener.close();
        verify(task).cancel();
        verify(sender).send(player, false);
        verify(world, never()).setGameRule(any(), any());
    }

    @Test
    void disableRestoresReducedWorldRuleInsteadOfAlwaysEnablingDebug() throws Exception {
        World world = mock(World.class);
        when(player.getWorld()).thenReturn(world);
        when(world.getGameRuleValue(GameRules.REDUCED_DEBUG_INFO)).thenReturn(true);
        listener.close();
        verify(sender).send(player, true);
    }

    @Test
    void packetFailureIsLoggedOnceAndOtherPlayersStillUpdate() throws Exception {
        doThrow(new ReflectiveOperationException("unsupported packet mapping"))
                .when(sender).send(player, true);
        listener.apply(player);
        listener.apply(player);
        verify(plugin.getLogger()).log(eq(java.util.logging.Level.WARNING), anyString(), any(Throwable.class));

        Player other = mock(Player.class);
        when(other.isOnline()).thenReturn(true);
        listener.apply(other);
        verify(sender).send(other, true);
    }
}

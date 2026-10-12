package net.azisaba.lgw.core.syogo;

import org.bukkit.Server;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RyuzuChatHookTest {
    private Plugin plugin;
    private PluginManager manager;
    private RyuzuChatHook hook;
    private AtomicReference<String> title;
    private RyuzuChatHook.Binding binding;

    @BeforeEach
    void setUp() throws Exception {
        plugin = mock(Plugin.class);
        Server server = mock(Server.class);
        manager = mock(PluginManager.class);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(manager);
        when(plugin.getLogger()).thenReturn(mock(Logger.class));
        title = new AtomicReference<>("&6[古参]&r ");
        hook = new RyuzuChatHook(plugin, ignored -> title.get());
        binding = RyuzuChatHook.Binding.resolve(PublishEvent.class, Message.class);
    }

    @Test
    void titleIsAddedToOutgoingMessageWhileExistingPrefixAndOtherFieldsStayIntact() {
        Message message = new Message();
        hook.decorate(binding, new PublishEvent(message));
        assertEquals("&6[古参]&r &b[既存]", message.prefix);
        assertEquals("hello", message.body);
        assertEquals("&c[運営]", message.luckPermsPrefix);
    }

    @Test
    void selectionRemovalAndNewSelectionAreReadForEachMessage() {
        Message first = new Message();
        hook.decorate(binding, new PublishEvent(first));
        title.set("");
        Message removed = new Message();
        hook.decorate(binding, new PublishEvent(removed));
        title.set("&a[新人]&r ");
        Message changed = new Message();
        hook.decorate(binding, new PublishEvent(changed));
        assertEquals("&6[古参]&r &b[既存]", first.prefix);
        assertEquals("&b[既存]", removed.prefix);
        assertEquals("&a[新人]&r &b[既存]", changed.prefix);
    }

    @Test
    void playerWithoutExistingPrefixStillGetsTitle() {
        Message message = new Message();
        message.prefix = null;
        hook.decorate(binding, new PublishEvent(message));
        assertEquals(title.get(), message.prefix);
    }

    @Test
    void discordAndSystemMessagesAreNotDecorated() {
        Message discord = new Message();
        discord.fromDiscord = true;
        hook.decorate(binding, new PublishEvent(discord));
        Message system = new Message();
        system.uuid = null;
        hook.decorate(binding, new PublishEvent(system));
        assertEquals("&b[既存]", discord.prefix);
        assertEquals("&b[既存]", system.prefix);
    }

    @Test
    void missingOptionalPluginDoesNotRegisterOrWarn() {
        hook.register();
        verify(manager, never()).registerEvent(any(), any(), any(), any(), any(), anyBoolean());
        verifyNoInteractions(plugin.getLogger());
    }

    @Test
    void publishEventsAreRegisteredBeforeRedisTransmissionAndIgnoreCancellation() throws Exception {
        var channel = RyuzuChatHook.Binding.resolve(ChannelPublishEvent.class, Message.class);
        hook.register(List.of(binding, channel));
        var executor = ArgumentCaptor.forClass(EventExecutor.class);
        verify(manager).registerEvent(eq(PublishEvent.class), eq(hook), eq(EventPriority.HIGHEST),
                executor.capture(), eq(plugin), eq(true));
        verify(manager).registerEvent(eq(ChannelPublishEvent.class), eq(hook), eq(EventPriority.HIGHEST),
                any(EventExecutor.class), eq(plugin), eq(true));
        Message message = new Message();
        executor.getValue().execute(hook, new PublishEvent(message));
        assertEquals("&6[古参]&r &b[既存]", message.prefix);
    }

    @Test
    void asynchronousPublishReadsTitleWithoutBukkitPlayerLookup() throws Exception {
        Message message = new Message();
        Thread thread = new Thread(() -> hook.decorate(binding, new PublishEvent(message)));
        thread.start();
        thread.join();
        assertEquals("&6[古参]&r &b[既存]", message.prefix);
        verifyNoInteractions(plugin);
    }

    @Test
    void incompatibleMessageLogsOnceAndDoesNotBreakPublishing() {
        PublishEvent event = new PublishEvent(null);
        assertDoesNotThrow(() -> hook.decorate(binding, event));
        assertDoesNotThrow(() -> hook.decorate(binding, event));
        verify(plugin.getLogger()).log(eq(java.util.logging.Level.WARNING), anyString(), any(Throwable.class));
    }

    @Test
    void incompatibleApiIsRejectedWhenResolvingBindings() {
        assertThrows(NoSuchMethodException.class, () -> RyuzuChatHook.Binding.resolve(PublishEvent.class, String.class));
    }

    // Minimal public API fixtures; the installed plugin supplies these event/data objects.
    public static class PublishEvent extends Event {
        private final Message message;
        public PublishEvent(Message message) { super(true); this.message = message; }
        public Message getMessage() { return message; }
        @Override public HandlerList getHandlers() { return new HandlerList(); }
    }
    public static final class ChannelPublishEvent extends PublishEvent {
        public ChannelPublishEvent(Message message) { super(message); }
    }
    public static final class Message {
        UUID uuid = UUID.randomUUID();
        String prefix = "&b[既存]";
        String luckPermsPrefix = "&c[運営]";
        String body = "hello";
        boolean fromDiscord;
        public UUID getPlayerUuid() { return uuid; }
        public boolean isFromDiscord() { return fromDiscord; }
        public String getRyuzuMapPrefix() { return prefix; }
        public void setRyuzuMapPrefix(String prefix) { this.prefix = prefix; }
    }
}

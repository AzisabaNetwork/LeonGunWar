package net.azisaba.lgw.core.syogo;

import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;

/** Optional RyuZUPluginChat integration. Adds the title to outgoing messages, without changing Redis prefixes. */
public final class RyuzuChatHook implements Listener {
    private static final String ROOT = "net.azisaba.ryuzupluginchat.";
    private final Plugin plugin;
    private final Function<UUID, String> titles;
    private final AtomicBoolean failureReported = new AtomicBoolean();

    public RyuzuChatHook(Plugin plugin, Function<UUID, String> titles) {
        this.plugin = plugin;
        this.titles = titles;
    }

    public void register() {
        Plugin chat = plugin.getServer().getPluginManager().getPlugin("RyuZUPluginChat");
        if (chat == null || !chat.isEnabled()) return;
        try {
            ClassLoader loader = chat.getClass().getClassLoader();
            // Resolve both contracts before registering either event.
            register(List.of(resolve(loader, "Global", "GlobalMessageData"),
                    resolve(loader, "Channel", "ChannelChatMessageData")));
            plugin.getLogger().info("RyuZUPluginChatの全体・チャンネルチャットに称号を表示します。");
        } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
            report(exception);
        }
    }

    private static Binding resolve(ClassLoader loader, String kind, String data) throws ReflectiveOperationException {
        Class<? extends Event> event = Class.forName(ROOT + "event.AsyncPublish" + kind + "MessageEvent", true, loader)
                .asSubclass(Event.class);
        return Binding.resolve(event, Class.forName(ROOT + "message.data." + data, true, loader));
    }

    void register(List<Binding> bindings) {
        for (Binding binding : bindings) {
            plugin.getServer().getPluginManager().registerEvent(binding.type(), this, EventPriority.HIGHEST,
                    (ignored, event) -> decorate(binding, event), plugin, true);
        }
    }

    void decorate(Binding binding, Event event) {
        try {
            Object message = binding.message().invoke(event);
            if ((boolean) binding.fromDiscord().invoke(message)) return;
            UUID uuid = (UUID) binding.uuid().invoke(message);
            if (uuid == null) return;
            String title = titles.apply(uuid);
            if (title == null || title.isEmpty()) return;
            String prefix = (String) binding.prefix().invoke(message);
            binding.setPrefix().invoke(message, title + (prefix == null ? "" : prefix));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            report(exception);
        }
    }

    private void report(Throwable exception) {
        if (failureReported.compareAndSet(false, true)) {
            plugin.getLogger().log(Level.WARNING,
                    "RyuZUPluginChatの称号連携に失敗しました。導入版のAPIを確認してください。", exception);
        }
    }

    record Binding(Class<? extends Event> type, Method message, Method uuid, Method fromDiscord,
                   Method prefix, Method setPrefix) {
        static Binding resolve(Class<? extends Event> event, Class<?> data) throws NoSuchMethodException {
            return new Binding(event, event.getMethod("getMessage"), data.getMethod("getPlayerUuid"),
                    data.getMethod("isFromDiscord"), data.getMethod("getRyuzuMapPrefix"),
                    data.getMethod("setRyuzuMapPrefix", String.class));
        }
    }
}

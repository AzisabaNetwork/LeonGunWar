package net.azisaba.lgw.core.listeners.others;

import org.bukkit.GameRules;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.logging.Level;

/** Standard-client debug visibility, independent of world-wide gamerules. */
public final class DebugInfoListener implements Listener, AutoCloseable {
    private static final String ADMIN_PERMISSION = "leongunwar.command.leongunwaradmin";
    private final Plugin plugin;
    private final Sender sender;
    private BukkitTask task;
    private boolean reportedFailure;

    public DebugInfoListener(Plugin plugin) {
        this(plugin, new PaperSender());
    }

    DebugInfoListener(Plugin plugin, Sender sender) {
        this.plugin = plugin;
        this.sender = sender;
    }

    public void start() {
        if (task != null) return;
        // Refresh permissions and repair flags reset by login, gamerules or another plugin.
        task = plugin.getServer().getScheduler().runTaskTimer(plugin,
                () -> plugin.getServer().getOnlinePlayers().forEach(this::apply), 1L, 20L);
    }

    void apply(Player player) {
        if (!player.isOnline()) return;
        send(player, !player.isOp() && !player.hasPermission(ADMIN_PERMISSION));
    }

    private void send(Player player, boolean reduced) {
        try {
            sender.send(player, reduced);
            reportedFailure = false;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            if (!reportedFailure) {
                plugin.getLogger().log(Level.WARNING,
                        "デバッグ情報の表示制御に失敗しました。Paper 1.21.11向けの送信処理を確認してください。", exception);
                reportedFailure = true;
            }
        }
    }

    private void applyNextTick(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> apply(player));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        applyNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        applyNextTick(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        applyNextTick(event.getPlayer());
    }

    @Override
    public void close() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        // Restore each online player's actual world rule when this plugin is disabled.
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (player.isOnline()) {
                send(player, Boolean.TRUE.equals(player.getWorld().getGameRuleValue(GameRules.REDUCED_DEBUG_INFO)));
            }
        }
    }

    @FunctionalInterface
    interface Sender {
        void send(Player player, boolean reduced) throws ReflectiveOperationException;
    }

    /** Paper's Mojang-mapped 1.21.11 packet, sent only to its own player. */
    private static final class PaperSender implements Sender {
        private Access access;

        @Override
        public void send(Player player, boolean reduced) throws ReflectiveOperationException {
            if (access == null) access = Access.resolve(player);
            Object handle = access.getHandle.invoke(player);
            Object connection = access.connection.get(handle);
            if (connection == null) return;
            Object packet = access.packet.newInstance(handle, reduced ? access.reduced : access.full);
            access.send.invoke(connection, packet);
        }
    }

    private record Access(Method getHandle, Field connection, Constructor<?> packet, Method send,
                          byte reduced, byte full) {
        static Access resolve(Player player) throws ReflectiveOperationException {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object handle = getHandle.invoke(player);
            Field connection = handle.getClass().getField("connection");
            Class<?> entity = Class.forName("net.minecraft.world.entity.Entity");
            Class<?> packetType = Class.forName("net.minecraft.network.protocol.Packet");
            Constructor<?> packet = Class.forName("net.minecraft.network.protocol.game.ClientboundEntityEventPacket")
                    .getConstructor(entity, byte.class);
            Method send = connection.getType().getMethod("send", packetType);
            Class<?> events = Class.forName("net.minecraft.world.entity.EntityEvent");
            return new Access(getHandle, connection, packet, send,
                    events.getField("REDUCED_DEBUG_INFO").getByte(null),
                    events.getField("FULL_DEBUG_INFO").getByte(null));
        }
    }
}

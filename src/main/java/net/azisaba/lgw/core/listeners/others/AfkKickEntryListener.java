package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.AfkKickMonitoringTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AfkKickEntryListener implements Listener {
    private static final long THROTTLE_MS = 400;   // 連続更新の最短間隔
    private static final double POS_EPS = 0.02;    // 微動カット
    private static final float ROT_EPS = 2.0f;     // 回転2度以上で動いた扱い
    // 最後にプレイヤーが動いた時のミリ秒を保存
    private final ConcurrentHashMap<UUID, Long> lastMoved = new ConcurrentHashMap<>();

    public AfkKickEntryListener() {
        // コンストラクタが呼び出されたときにタスクを開始
        new AfkKickMonitoringTask(LeonGunWar.getPlugin(), lastMoved)
                .runTaskTimerAsynchronously(LeonGunWar.getPlugin(), 0L, 20L * 3);

        // 現在オンラインのプレイヤーを設定
        Bukkit.getOnlinePlayers().forEach(p -> lastMoved.put(p.getUniqueId(), System.currentTimeMillis()));
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        Location from = e.getFrom(), to = e.getTo();
        boolean movedPos =
                Math.abs(from.getX() - to.getX()) > POS_EPS ||
                        Math.abs(from.getY() - to.getY()) > POS_EPS ||
                        Math.abs(from.getZ() - to.getZ()) > POS_EPS;

        boolean movedRot =
                Math.abs(norm(from.getYaw() - to.getYaw())) >= ROT_EPS ||
                        Math.abs(norm(from.getPitch() - to.getPitch())) >= ROT_EPS;

        if (movedPos || movedRot) updateActivity(p.getUniqueId());
    }

    private static float norm(float deg) {
        deg %= 360f;
        if (deg > 180f) deg -= 360f;
        if (deg < -180f) deg += 360f;
        return deg;
    }

    /* ===== スロットル付きの更新 ===== */
    private void updateActivity(UUID id) {
        long now = System.currentTimeMillis();
        Long prev = lastMoved.get(id);
        if (prev != null && (now - prev) < THROTTLE_MS) return; // スパム抑制
        lastMoved.put(id, now);
    }

    /**
     * 銃を撃ったときも値を設定
     */
    @EventHandler(ignoreCancelled = true)
    public void onShot(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType().isAir()) {
            return;
        }
        updateActivity(e.getPlayer().getUniqueId());
    }

    /**
     * 参加したときも値を設定
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        lastMoved.put(e.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    /**
     * データがかさばるので退出したときに値を削除
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        lastMoved.remove(e.getPlayer().getUniqueId());
    }

    /**
     * GUI操作中も値を設定
     */
    @EventHandler
    public void onInvClick(InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player p) updateActivity(p.getUniqueId());
    }

    @EventHandler
    public void onInvOpen(InventoryOpenEvent e) {
        if (e.getPlayer() instanceof Player p) updateActivity(p.getUniqueId());
    }
}

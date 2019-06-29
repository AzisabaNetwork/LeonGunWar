package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.AfkKickMonitoringTask;

public class AfkKickEntryListener implements Listener {

    // 最後にプレイヤーが動いた時のミリ秒を保存
    private final Map<Player, Long> lastMoved = new HashMap<>();

    public AfkKickEntryListener() {
        // コンストラクタが呼び出されたときにタスクを開始
        new AfkKickMonitoringTask(lastMoved).runTaskTimer(LeonGunWar.getPlugin(), 0, 20 * 3);

        // 現在オンラインのプレイヤーを設定
        Bukkit.getOnlinePlayers().forEach(p -> {
            lastMoved.put(p, System.currentTimeMillis());
        });
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        lastMoved.put(e.getPlayer(), System.currentTimeMillis());
    }

    /**
     * 参加したときも値を設定
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        lastMoved.put(e.getPlayer(), System.currentTimeMillis());
    }

    /**
     * データがかさばるので退出したときに値を削除
     */
    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        if ( lastMoved.containsKey(e.getPlayer()) ) {
            lastMoved.remove(e.getPlayer());
        }
    }
}

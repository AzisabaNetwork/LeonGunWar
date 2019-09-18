package net.azisaba.lgw.core.listeners.others;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.AllowEditInventoryTask;
import net.azisaba.lgw.core.utils.Chat;

/**
 * プレイヤーリスポーン後、動いてから10秒間のみアイテム切り替えができる機能を追加
 *
 * @author siloneco
 *
 */
public class DisableChangeItemListener implements Listener {

    private final long allowEditSeconds = 10;

    private final Map<Player, Instant> remainTimes = new HashMap<>();
    private final Map<Player, BukkitTask> taskMap = new HashMap<>();

    private final List<Player> countdownQueue = new ArrayList<>();

    private boolean isAllowEdit(Player player) {
        if ( !LeonGunWar.getPlugin().getManager().isPlayerMatching(player) ) {
            return true;
        }

        return countdownQueue.contains(player) || Instant.now().isBefore(remainTimes.getOrDefault(player, Instant.now()));
    }

    private void startCountdown(Player player) {
        // 時間指定
        remainTimes.put(player, Instant.now().plusSeconds(allowEditSeconds));

        taskMap.compute(player, (key, task) -> {
            // タスク終了
            if ( task != null ) {
                task.cancel();
            }

            // タスク開始
            return new AllowEditInventoryTask(player, remainTimes).runTaskTimer(LeonGunWar.getPlugin(), 0, 20);
        });
    }

    @EventHandler
    public void clickInventory(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }
        Player p = (Player) e.getWhoClicked();
        Inventory clicked = e.getClickedInventory();

        if ( clicked == null ) {
            return;
        }
        if ( !clicked.equals(p.getInventory()) ) {
            return;
        }

        if ( !isAllowEdit(p) ) {
            e.setCancelled(true);
            p.closeInventory();
            p.sendMessage(Chat.f("{0}&7リスポーン後以外でアイテムを切り替えることはできません！", LeonGunWar.GAME_PREFIX));
            return;
        }

    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();

        // 試合中ではない場合return
        if ( !LeonGunWar.getPlugin().getManager().isPlayerMatching(p) ) {
            return;
        }

        // カウントダウンのキューに追加
        if ( !countdownQueue.contains(p) ) {
            countdownQueue.add(p);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if ( countdownQueue.contains(p) ) {
            startCountdown(p);
            countdownQueue.remove(p);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if ( countdownQueue.contains(p) ) {
            startCountdown(p);
            countdownQueue.remove(p);
        }
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }
        Player p = (Player) e.getWhoClicked();

        if ( countdownQueue.contains(p) ) {
            startCountdown(p);
            countdownQueue.remove(p);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if ( countdownQueue.contains(p) ) {
            countdownQueue.remove(p);
        }
    }
}

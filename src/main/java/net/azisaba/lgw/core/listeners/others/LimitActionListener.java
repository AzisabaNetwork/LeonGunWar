package net.azisaba.lgw.core.listeners.others;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 運営の不必要な動きを制限するListener
 *
 * @author siloneco
 *
 */
public class LimitActionListener implements Listener {

    private List<UUID> allowDropPlayers = new ArrayList<>();
    private List<UUID> allowBuildPlayers = new ArrayList<>();

    // アイテムドロップ時
    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if ( !p.hasPermission("leongunwar.command.limit") ) {
            return;
        }
        // allowDropPlayersに含まれていない場合はキャンセル
        if ( !allowDropPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    // ブロック設置時
    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if ( !p.hasPermission("leongunwar.command.limit") ) {
            return;
        }
        // allowBuildPlayersに含まれていない場合はキャンセル
        if ( !allowBuildPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    // ブロック破壊時
    @EventHandler
    public void onPlace(BlockBreakEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if ( !p.hasPermission("leongunwar.command.limit") ) {
            return;
        }
        // allowBuildPlayersに含まれていない場合はキャンセル
        if ( !allowBuildPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    // 額縁破壊時
    @EventHandler
    public void preventDestroyItemFrame(EntityDamageByEntityEvent e) {
        // Entityが額縁ではない場合return
        if ( !(e.getEntity() instanceof ItemFrame) ) {
            return;
        }

        Player p = null;

        // 攻撃者がプレイヤーの場合
        if ( e.getDamager() instanceof Player ) {
            p = (Player) e.getDamager();
        }
        // 攻撃者が発射物の場合
        if ( e.getDamager() instanceof Projectile ) {
            // ソースがプレイヤーの場合
            if ( ((Projectile) e.getDamager()).getShooter() instanceof Player ) {
                p = (Player) ((Projectile) e.getDamager()).getShooter();
            }
        }

        // プレイヤーではない場合return
        if ( p == null ) {
            return;
        }

        // 権限がない場合return
        if ( !p.hasPermission("leongunwar.command.limit") ) {
            return;
        }
        // allowBuildPlayersに含まれていない場合はキャンセル
        if ( !allowBuildPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    // 空の額縁破壊時
    @EventHandler
    public void preventDestroyEmptyFrame(HangingBreakByEntityEvent e) {
        // 壊したのがプレイヤーではない場合return
        if ( !(e.getRemover() instanceof Player) ) {
            return;
        }

        // プレイヤーを取得
        Player p = (Player) e.getRemover();

        // 権限がない場合return
        if ( !p.hasPermission("leongunwar.command.limit") ) {
            return;
        }
        // allowBuildPlayersに含まれていない場合はキャンセル
        if ( !allowBuildPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        allowDropPlayers.remove(id);
        allowBuildPlayers.remove(id);
    }

    public boolean toggleAllowDrop(Player player) {
        if ( allowDropPlayers.contains(player.getUniqueId()) ) {
            allowDropPlayers.remove(player.getUniqueId());
            return false;
        } else {
            allowDropPlayers.add(player.getUniqueId());
            return true;
        }
    }

    public boolean toggleAllowBuild(Player player) {
        if ( allowBuildPlayers.contains(player.getUniqueId()) ) {
            allowBuildPlayers.remove(player.getUniqueId());
            return false;
        } else {
            allowBuildPlayers.add(player.getUniqueId());
            return true;
        }
    }
}

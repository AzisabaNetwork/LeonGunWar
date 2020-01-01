package net.azisaba.lgw.core.listeners.others;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
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
        // allowDropPlayersに含まれていない場合はキャンセル
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
        // allowDropPlayersに含まれていない場合はキャンセル
        if ( !allowBuildPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        if ( allowDropPlayers.contains(id) ) {
            allowDropPlayers.remove(id);
        }
        if ( allowBuildPlayers.contains(id) ) {
            allowBuildPlayers.remove(id);
        }
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

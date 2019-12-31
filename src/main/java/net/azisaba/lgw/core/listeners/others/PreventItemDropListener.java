package net.azisaba.lgw.core.listeners.others;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 運営がアイテムを落とさないようにするリスナー
 *
 * @author siloneco
 *
 */
public class PreventItemDropListener implements Listener {

    private List<UUID> allowDropPlayers = new ArrayList<>();

    @EventHandler
    public void onDropItem(PlayerDropItemEvent e) {
        Player p = e.getPlayer();

        // 権限がない場合return
        if ( !p.hasPermission("leongunwar.command.itemdrop") ) {
            return;
        }
        // allowDropPlayersに含まれていない場合はキャンセル
        if ( !allowDropPlayers.contains(p.getUniqueId()) ) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent e) {
        if ( allowDropPlayers.contains(e.getPlayer().getUniqueId()) ) {
            allowDropPlayers.remove(e.getPlayer().getUniqueId());
        }
    }

    public void setAllowDrop(Player player, boolean value) {
        if ( value && !allowDropPlayers.contains(player.getUniqueId()) ) {
            allowDropPlayers.add(player.getUniqueId());
        }
        if ( !value && allowDropPlayers.contains(player.getUniqueId()) ) {
            allowDropPlayers.remove(player.getUniqueId());
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
}

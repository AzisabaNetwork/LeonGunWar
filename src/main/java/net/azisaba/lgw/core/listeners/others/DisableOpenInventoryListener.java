package net.azisaba.lgw.core.listeners.others;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

// 自分のインベントリのCreative特例をブロックの操作へ広げない。
public class DisableOpenInventoryListener implements Listener {

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent e) {
        Player p = (Player) e.getPlayer();
        InventoryType type = e.getInventory().getType();
        boolean ownInventory = type == InventoryType.CRAFTING || type == InventoryType.CREATIVE
                || type == InventoryType.PLAYER;
        boolean blocked = type == InventoryType.ANVIL || type == InventoryType.WORKBENCH
                || type == InventoryType.FURNACE;
        // 仮想GUIは開けるが、Creativeでチェストなどの実コンテナは操作しない。
        boolean creativeContainer = p.getGameMode() == GameMode.CREATIVE && !ownInventory
                && e.getInventory().getLocation() != null;
        if (blocked || creativeContainer) {
            e.setCancelled(true);
            p.playSound(p.getLocation(), "minecraft:entity.villager.no", 1, 1);
        }
    }
}

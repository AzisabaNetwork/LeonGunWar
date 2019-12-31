package net.azisaba.lgw.core.listeners.others;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;

/**
 * ホッパーが外部のアイテムを吸引するのを無効化するListener
 *
 * @author siloneco
 *
 */
public class DisableHopperPickupListener implements Listener {

    @EventHandler
    public void onPickUp(InventoryPickupItemEvent e) {
        if ( e.getInventory().getType() != InventoryType.HOPPER ) {
            return;
        }

        e.setCancelled(true);
    }
}

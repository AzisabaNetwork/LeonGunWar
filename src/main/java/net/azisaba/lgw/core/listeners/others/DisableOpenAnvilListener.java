package net.azisaba.lgw.core.listeners.others;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.AnvilInventory;

// 金床を開けれないようにするリスナー
public class DisableOpenAnvilListener implements Listener {

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getInventory() instanceof AnvilInventory) {
			// 金床だった場合はキャンセル
			e.setCancelled(true);
		}
	}
}

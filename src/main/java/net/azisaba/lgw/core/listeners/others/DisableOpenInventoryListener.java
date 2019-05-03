package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;

// 金床を開けれないようにするリスナー
public class DisableOpenInventoryListener implements Listener {

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		if (e.getInventory() instanceof AnvilInventory) {
			// 金床だった場合はキャンセル
			e.setCancelled(true);
		}

		if (e.getInventory().getType() == InventoryType.FURNACE) {
			// かまどならキャンセル
			e.setCancelled(true);
			((Player) e.getPlayer()).playSound(e.getPlayer().getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		}
	}
}

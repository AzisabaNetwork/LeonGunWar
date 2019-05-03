package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

// 金床とかまどを開けれないようにするリスナー
public class DisableOpenInventoryListener implements Listener {

	@EventHandler
	public void onInventoryOpen(InventoryOpenEvent e) {
		Player p = (Player) e.getPlayer();

		if (e.getInventory().getType() == InventoryType.ANVIL) {
			// 金床だった場合はキャンセル
			e.setCancelled(true);

			// 音を鳴らす
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		}

		if (e.getInventory().getType() == InventoryType.FURNACE) {
			// かまどならキャンセル
			e.setCancelled(true);

			// 音を鳴らす
			p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
		}
	}
}

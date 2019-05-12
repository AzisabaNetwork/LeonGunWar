package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

// レシピを無効化するリスナー
public class DisableRecipeListener implements Listener {

	@EventHandler
	public void onCraftItem(CraftItemEvent e) {
		Player p = (Player) e.getWhoClicked();

		// クラフト結果
		ItemStack result = e.getInventory().getResult();

		// クラフト結果がない場合は無視
		if (result == null) {
			return;
		}

		// クラフト後のアイテムにカスタム名がある場合は無視
		if (result.hasItemMeta() && result.getItemMeta().hasDisplayName()) {
			return;
		}

		// 普通のアイテムはクラフト禁止！！
		e.setCancelled(true);

		// 音を鳴らす
		p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);
	}
}
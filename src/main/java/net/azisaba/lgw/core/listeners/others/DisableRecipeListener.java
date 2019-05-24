package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;

// レシピを無効化するリスナー
public class DisableRecipeListener implements Listener {

	// priority MONITORでresultを元のアイテムに変更するListener
	private final HashMap<Player, ItemStack> cancelPlayerMap = new HashMap<>();

	@EventHandler(priority = EventPriority.LOWEST)
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

		// クラフト後のアイテムがダイヤかエメラルドの場合は無視
		if (result.getType() == Material.DIAMOND || result.getType() == Material.EMERALD) {
			return;
		}

		// クラフト後のアイテムがダイヤブロックかエメラルドブロックの場合は無視
		if (result.getType() == Material.DIAMOND_BLOCK || result.getType() == Material.EMERALD_BLOCK) {
			return;
		}

		// 普通のアイテムはクラフト禁止！！
		e.setCancelled(true);

		// 音を鳴らす
		p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

		// Priority MONITORで元のアイテムに戻すためにHashMapに追加
		ItemStack item = result.clone();
		cancelPlayerMap.put(p, item);
	}

	/**
	 * CrackShotがignoreCancelled = trueにしていないため、こちらでキャンセルしても向こう側でResultアイテムの名前が変わってしまい、
	 * 2回目のクリックでクラフトできてはいけないアイテムまでクラフトできてしまうため、MONITORでデフォルトアイテムを再セットするメソッド
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
	public void restoreDefaultItem(CraftItemEvent e) {
		Player p = (Player) e.getWhoClicked();

		// mapにプレイヤーが含まれている場合アイテムをセット
		if (cancelPlayerMap.containsKey(e.getWhoClicked())) {
			e.getInventory().setResult(cancelPlayerMap.get(p));

			// 削除
			cancelPlayerMap.remove(p);
		}
	}
}

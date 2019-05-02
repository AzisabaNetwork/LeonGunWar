package net.azisaba.lgw.core.utils;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

// 動的にフレッシュなアイテムを提供するクラス
public class CustomItem {

	// は？(威圧)
	protected CustomItem() {
	}

	// 勝利したチームに配布するアイテム (卍勝者の証卍)
	public static ItemStack getWonItem() {
		ItemStack item = new ItemStack(Material.END_CRYSTAL);
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.GOLD + "勝者の証");
		meta.setLore(Arrays.asList("勝者に与えられる証", "ダイヤと交換できる"));
		item.setItemMeta(meta);
		return item;
	}

	// 色付きチェストプレート！！
	public static ItemStack getColorChestplate(Color color) {
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setColor(color);
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		return item;
	}
}

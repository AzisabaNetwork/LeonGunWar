package net.azisaba.lgw.core.utils;

import java.util.Arrays;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.azisaba.lgw.core.teams.BattleTeam;

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

	// チームの色付きチェストプレート！！
	public static ItemStack getTeamChestplate(BattleTeam team) {
		ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
		LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
		meta.setDisplayName(team.getTeamName());
		meta.setColor(team.getTeamColor());
		meta.setUnbreakable(true);
		item.setItemMeta(meta);
		item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
		return item;
	}
}

package net.azisaba.lgw.core.utils;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import net.azisaba.lgw.core.util.BattleTeam;

import lombok.experimental.UtilityClass;

// 動的にフレッシュなアイテムを提供するクラス
@UtilityClass
public class CustomItem {

    // 勝利したチームに配布するアイテム (卍勝者の証卍)
    public ItemStack getWonItem() {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Chat.f("&6勝者の証&6"));
        meta.setLore(Arrays.asList(Chat.f("&5勝者に与えられる証"), Chat.f("&5ダイヤと交換できる")));
        item.setItemMeta(meta);
        return item;
    }

    // チームの色付きチェストプレート！！
    public static ItemStack getTeamChestplate(BattleTeam team) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.setDisplayName(team.getTeamName());
        meta.setColor(team.getColor());
        meta.setUnbreakable(true);
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return item;
    }
}

package net.azisaba.lgw.core.utils;

import java.util.Arrays;

import org.bukkit.Color;
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
    public static ItemStack getWonItem() {
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

    // 体力表示用ヘルメット！！！！
    public static ItemStack getHealthHelmet(double health) {
        ItemStack helmet = new ItemStack(Material.LEATHER_HELMET);

        LeatherArmorMeta meta = (LeatherArmorMeta) helmet.getItemMeta();
        meta.setDisplayName(Chat.f("現在の体力:{0}", health));
        meta.setColor(getHealthColor(health));
        meta.setUnbreakable(true);

        helmet.setItemMeta(meta);

        helmet.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        helmet.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);

        return helmet;
    }

    private Color getHealthColor(double health) {
        int colorHealthRate = (int) (510 * (health / 20.0));
        int red = Math.min(colorHealthRate, 255);
        int green = colorHealthRate <= 255 ? 255 : (510 - colorHealthRate);
        int blue = 0; // 説明変数とはこのことだ

        return Color.fromRGB(red, green, blue);
    }
}

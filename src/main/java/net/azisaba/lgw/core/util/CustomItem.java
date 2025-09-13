package net.azisaba.lgw.core.util;

import lombok.experimental.UtilityClass;
import net.azisaba.lgw.core.battlesystem.BattleTeam;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import java.util.Arrays;
import java.util.UUID;

// 動的にフレッシュなアイテムを提供するクラス
@UtilityClass
public class CustomItem {

    // 勝利したチームに配布するアイテム (卍勝者の証卍)
    //todo:これ今使われてないかもしれない　勝者の証ではなく勝利の塊魂だったかな
    public ItemStack getWonItem() {
        ItemStack item = new ItemStack(Material.END_CRYSTAL);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Chat.c("&6勝者の証&6"));
        meta.lore(Arrays.asList(Chat.c("&5勝者に与えられる証"), Chat.c("&5ダイヤと交換できる")));
        item.setItemMeta(meta);
        return item;
    }

    // チームの色付きチェストプレート！！
    public static ItemStack getTeamChestPlate(BattleTeam team) {
        ItemStack item = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta meta = (LeatherArmorMeta) item.getItemMeta();
        meta.displayName(team.getComponentTeamName());
        meta.setColor(team.getColor());
        meta.setUnbreakable(true);
        meta.addAttributeModifier(Attribute.GENERIC_ARMOR, new AttributeModifier(UUID.randomUUID(), "generic.armor", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST));
        item.setItemMeta(meta);
        item.addUnsafeEnchantment(Enchantment.DURABILITY, 10);
        item.addUnsafeEnchantment(Enchantment.BINDING_CURSE, 1);
        return item;
    }
}

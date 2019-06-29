package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

// オフハンドを無効化するリスナー
public class DisableOffhandListener implements Listener {

    // Fキーでのオフハンド切り替えを無効化
    @EventHandler
    public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
        e.setCancelled(true);
    }

    // インベントリーを閉じた際にオフハンドにアイテムがある場合は移動するかドロップ
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        PlayerInventory inventory = p.getInventory();
        ItemStack offhand = inventory.getItemInOffHand();

        // オフハンドにアイテムがない場合はreturn
        if ( offhand == null || offhand.getType() == Material.AIR ) {
            return;
        }

        if ( inventory.firstEmpty() != -1 ) {
            // インベントリーに空きがある場合はアイテムを追加
            inventory.addItem(offhand);
        } else {
            // インベントリーに空きがない場合はドロップ
            p.getWorld().dropItem(p.getLocation(), offhand);
        }

        // オフハンドからアイテムを消す
        inventory.setItemInOffHand(null);
    }
}

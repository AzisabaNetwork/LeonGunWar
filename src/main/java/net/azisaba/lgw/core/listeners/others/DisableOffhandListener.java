package net.azisaba.lgw.core.listeners.others;

import com.shampaggon.crackshot.CSUtility;
import net.azisaba.lgw.core.util.LgwLog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.slf4j.Logger;

// オフハンドを無効化するリスナー
public class DisableOffhandListener implements Listener {
    private final Logger logger = LgwLog.getLogger(this.getClass());

    // Fキーでのオフハンド切り替えを無効化
    //@EventHandler(priority = EventPriority.LOWEST)
    //public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
    //    e.setCancelled(true);
    //}

    // インベントリーを閉じた際にオフハンドにアイテムがある場合は移動するかドロップ
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player p = (Player) e.getPlayer();
        PlayerInventory inventory = p.getInventory();
        ItemStack offhand = inventory.getItemInOffHand();

        // オフハンドにアイテムがない場合はreturn
        if (offhand == null || offhand.getType() == Material.AIR) {
            return;
        }

        if (inventory.firstEmpty() != -1) {
            // インベントリーに空きがある場合はアイテムを追加
            inventory.addItem(offhand);
        } else {
            // インベントリーに空きがない場合はドロップ
            //p.getWorld().dropItem(p.getLocation(), offhand);
            String weaponTitle = new CSUtility().getWeaponTitle(offhand);
            if (weaponTitle == null) {
                logger.info("[LeonGunWar] " + p.getName() + " がオフハンドの " + offhand.getType().name() + " をドロップしようとしました");
            } else {
                logger.info("[LeonGunWar] " + p.getName() + " がオフハンドの " + weaponTitle + " をドロップしようとしました");
            }
        }

        // オフハンドからアイテムを消す
        inventory.setItemInOffHand(null);
    }
}

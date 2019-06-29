package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.inventory.ItemStack;

/**
 * アイテムの耐久地が減ることを無効化します 釣り竿は釣りで使用されるため耐久値は減ります
 * 
 * @author siloneco
 *
 */
public class DisableItemDamageListener implements Listener {

    /**
     * アイテムの耐久値が減るのを無効化するリスナーです
     * 
     * @param e プレイヤーのアイテムの耐久値が減少するイベント
     */
    @EventHandler
    public void onPlayerItemDamage(PlayerItemDamageEvent e) {
        ItemStack item = e.getItem();

        // アイテムがnullの場合return
        if ( item == null ) {
            return;
        }

        // 釣り竿だった場合return
        if ( item.getType() == Material.FISHING_ROD ) {
            return;
        }

        // 耐久値の減少をキャンセル
        e.setCancelled(true);
        // インベントリを更新
        e.getPlayer().updateInventory();
    }
}

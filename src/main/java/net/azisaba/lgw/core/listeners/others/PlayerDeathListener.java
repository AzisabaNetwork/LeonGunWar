package net.azisaba.lgw.core.listeners.others;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDeathListener implements Listener {
    private final CSUtility crackShot = new CSUtility();

    //試合外でデスメッセージが変わるように
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerDeath (PlayerDeathEvent e) {
        if(e.getEntity().getPlayer() != null ){
            // 試合中の場合はreturn
            if ( LeonGunWar.getPlugin().getManager().isMatching() ) {
                return;
            }
            if(e.getEntity().getPlayer().getKiller()==null){
                return;
            }
            Player p = e.getEntity().getPlayer();
            e.deathMessage(null);
            String msg;
            ItemStack item = p.getKiller().getInventory().getItemInMainHand();
            String itemName;
            if ( item == null || item.getType() == Material.AIR ) { // null または Air なら素手
                itemName = Chat.f("&6素手");
            } else if ( item.hasItemMeta() && item.getItemMeta().hasDisplayName() ) { // DisplayNameが指定されている場合
                // CrackShot Pluginを取得
                CSDirector crackshot = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

                // 銃ID取得
                String nodes = crackShot.getWeaponTitle(item);
                // DisplayNameを取得
                itemName = crackshot.getString(nodes + ".Item_Information.Item_Name");

                // DisplayNameがnullの場合は普通にアイテム名を取得
                if ( itemName == null ) {
                    itemName = item.getItemMeta().getDisplayName();
                }
            } else { // それ以外
                itemName = Chat.f("&6{0}", item.getType().name());
            }
            msg = Chat.f("&7&l[&c&lFFA&7&l]&f{0}&7---[&6{1}&7]-->&f{2}", p.getKiller().getName(), itemName, p.getName());
            // メッセージ送信
            p.getWorld().getPlayers().forEach(player -> player.sendMessage(msg));

            // コンソールに出力
            Bukkit.getConsoleSender().sendMessage(msg);
        }
    }
}

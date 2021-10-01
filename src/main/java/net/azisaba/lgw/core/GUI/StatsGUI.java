package net.azisaba.lgw.core.GUI;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.PlayerStats;
import net.azisaba.lgw.core.utils.Chat;

import jp.azisaba.lgw.kdstatus.KDStatusReloaded;
import jp.azisaba.lgw.kdstatus.sql.KDUserData;
import jp.azisaba.lgw.kdstatus.utils.TimeUnit;

public class StatsGUI implements Listener {

    private final Player player;
    private PlayerStats stats;

    public StatsGUI(Player player) {

        this.player = player;
        this.stats = PlayerStats.getStats(player);

        LeonGunWar.getPlugin().getServer().getPluginManager().registerEvents(this,LeonGunWar.getPlugin());

        show();

    }

    public void show(){

        KDUserData data = KDStatusReloaded.getPlugin().getKdDataContainer().getPlayerData(player,true);

        Inventory inv = Bukkit.createInventory(null,27,"LeonGunWar の統計情報");
        addItem(inv,13,Material.PAPER, Chat.f("&a統計情報"),
                Chat.f("&7勝利数: &a{0}",stats.getWins()),
                Chat.f("&7負けた数: &a{0}",stats.getLoses()),
                " ",
                Chat.f("&7キル: &a{0}",data.getKills(TimeUnit.LIFETIME)),
                Chat.f("&7死んだ数: &a{0}",data.getDeaths())
        );

        player.openInventory(inv);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){

        if(e.getPlayer() == player){
            HandlerList.unregisterAll(this);
        }

    }

    @EventHandler
    public void onClick(InventoryClickEvent e){
        e.setCancelled(true);
    }

    private void addItem(Inventory i, int index, Material material, String name, String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        i.setItem(index, item);
    }

}

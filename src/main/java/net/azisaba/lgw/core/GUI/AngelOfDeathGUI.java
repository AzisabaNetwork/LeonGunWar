package net.azisaba.lgw.core.GUI;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import net.azisaba.lgw.core.utils.LevelingUtils;

public class AngelOfDeathGUI implements Listener {

    private final Player player;
    private PlayerStats stats;
    private AngelGUIPhase phase;
    private AngelGUIType type;

    public AngelOfDeathGUI(Player player){

        this.player = player;
        this.stats = PlayerStats.getStats(player);
        this.phase = AngelGUIPhase.SHOP;

        LeonGunWar.getPlugin().getServer().getPluginManager().registerEvents(this,LeonGunWar.getPlugin());

    }

    public void shop(){

        Inventory inv = Bukkit.createInventory(null,45,"ショップ");
        addItem(inv,13,Material.SEA_LANTERN,Chat.f("未せってい"));
        addItem(inv,29,Material.PRISMARINE_SHARD,Chat.f("未せってい"));
        addItem(inv,33,Material.SKULL_ITEM,Chat.f("未せってい"),Chat.f("&7倍増ゲームの確率が増えるだけ"));

        player.openInventory(inv);

    }

    public void buy(AngelGUIType type){

        if(type == AngelGUIType.ANGEL_OF_DEATH) {
            if ( stats.getCoins() >= LevelingUtils.getRequiredCoin(stats.getAngelOfDeathLevel() + 1) ) {

                stats.setCoins(stats.getCoins() - LevelingUtils.getRequiredCoin(stats.getAngelOfDeathLevel() + 1));
                stats.addAngelOfDeathLevel(1);
                stats.update();
                player.sendMessage(Chat.f("&4&lお前の〇〇レベルが {0} になったぞ... せいぜいLGWを楽しむんだな...ふはははははは", stats.getAngelOfDeathLevel()));

            } else {

                player.sendMessage(Chat.f("&cコインが足りません！"));

            }
        }

    }

    public void are_you_ok(){

        phase = AngelGUIPhase.OPENING_GUI;

        Inventory inv = Bukkit.createInventory(null,27,"本当によろしいですか？");
        addItem(inv, 11, Material.GREEN_GLAZED_TERRACOTTA, ChatColor.RESET + "" + ChatColor.GREEN + "購入する", ChatColor.YELLOW + "コスト: " + ChatColor.AQUA + LevelingUtils.getRequiredCoin(stats.getAngelOfDeathLevel() + 1) + ChatColor.YELLOW + " Coins");
        addItem(inv, 15, Material.RED_GLAZED_TERRACOTTA, ChatColor.RESET + "" + ChatColor.RED + "やめておく", "", ChatColor.RED + "クリックしてキャンセル");

        player.openInventory(inv);

        phase = AngelGUIPhase.ARE_YOU_OK;

    }

    @EventHandler
    public void onClick(InventoryClickEvent e){

        if((Player) e.getWhoClicked() == player){

            e.setCancelled(true);

            if(phase == AngelGUIPhase.SHOP){

                if(e.getSlot() == 33){

                    if(stats.getCoins() >= LevelingUtils.getRequiredCoin(stats.getAngelOfDeathLevel() + 1)) {
                        phase = AngelGUIPhase.OPENING_GUI;
                        type = AngelGUIType.ANGEL_OF_DEATH;
                        are_you_ok();
                    }else {
                        player.sendMessage(Chat.f("&cコインが足りません！"));
                    }
                }

            }else if(phase == AngelGUIPhase.ARE_YOU_OK){
                if(e.getSlot() == 11){
                    if(stats.getAngelOfDeathLevel() == 12){
                        player.sendMessage(Chat.f("&cすでに最高レベルに到達しています！"));
                        return;
                    }
                    if(type == AngelGUIType.ANGEL_OF_DEATH){
                        buy(type);
                    }
                }else if(e.getSlot() == 15){
                    player.closeInventory();
                }

            }

        }

    }

    @EventHandler
    public void onClose(InventoryCloseEvent e){

        if(e.getPlayer() == player && phase != AngelGUIPhase.OPENING_GUI){
            HandlerList.unregisterAll(this);
        }

    }

    private void addItem(Inventory i, int index, Material material, String name, String... lore) {

        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.setDisplayName(name);

        // Set the lore of the item
        meta.setLore(Arrays.asList(lore));

        item.setItemMeta(meta);


        i.setItem(index, item);

    }


    public enum AngelGUIPhase{

        SHOP,
        ARE_YOU_OK,
        OPENING_GUI

    }

    public enum AngelGUIType{

        ANGEL_OF_DEATH

    }



}

package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableBlockInteractListener implements Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onBlockInteract(PlayerInteractEvent event){
        //右クリックしたブロックが特定のブロックだった場合イベントをキャンセルするリスナー
        Block block = event.getClickedBlock();
        if(block != null&& block.getType() == Material.COMPOSTER){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.ENCHANTING_TABLE){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LOOM){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.DISPENSER){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.BLACK_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.RED_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.ORANGE_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.MAGENTA_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LIGHT_BLUE_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.YELLOW_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LIME_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.PINK_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.GRAY_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LIGHT_GRAY_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.CYAN_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.PURPLE_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.BLUE_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.BROWN_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.GREEN_SHULKER_BOX){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.BLACK_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.RED_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.ORANGE_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.MAGENTA_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LIGHT_BLUE_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.YELLOW_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LIME_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.PINK_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.GRAY_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.LIGHT_GRAY_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.CYAN_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.PURPLE_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.BLUE_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.BROWN_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
        if(block != null&& block.getType() == Material.GREEN_BED){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.DROPPER){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.HOPPER){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.BARREL){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.SMOKER){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.BLAST_FURNACE){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.CARTOGRAPHY_TABLE){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.GRINDSTONE){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.STONECUTTER){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.SMITHING_TABLE){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.BREWING_STAND){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }

        if(block != null&& block.getType() == Material.CHEST){
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
            }
        }
    }
}

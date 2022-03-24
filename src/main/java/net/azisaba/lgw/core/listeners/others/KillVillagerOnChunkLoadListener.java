package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class KillVillagerOnChunkLoadListener implements Listener {
    // damage all villagers in the chunk with 9999 damage
    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        for ( Entity entity : e.getChunk().getEntities() ) {
            if ( entity.getType().equals(org.bukkit.entity.EntityType.VILLAGER) ) {
                ((Villager) entity).damage(9999);
            }
        }
    }
}

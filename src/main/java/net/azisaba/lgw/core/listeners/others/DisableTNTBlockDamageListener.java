package net.azisaba.lgw.core.listeners.others;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class DisableTNTBlockDamageListener implements Listener {

    @EventHandler
    public void onGrenadeExplode(EntityExplodeEvent event) {
        event.blockList().clear();
        // event.setCancelled(true);
    }
}

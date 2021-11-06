package net.azisaba.lgw.core.listeners.others;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.CustomItem;

public class ChestplateChangeListener implements Listener {
    private LeonGunWar plugin;

    public ChestplateChangeListener(LeonGunWar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!plugin.getManager().isPlayerMatching(player)) {
            return;
        }

        player.getInventory().setHelmet(CustomItem.getHealthHelmet(player.getHealth()));
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!plugin.getManager().isPlayerMatching(player)) {
            return;
        }

        player.getInventory().setHelmet(CustomItem.getHealthHelmet(player.getHealth()));
    }
}
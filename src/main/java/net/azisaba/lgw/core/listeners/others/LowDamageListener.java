package net.azisaba.lgw.core.listeners.others;

import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;

import net.minecraft.server.v1_12_R1.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_12_R1.WorldBorder;

public class LowDamageListener implements Listener {
    private final double HEALTH_THRESHOLD = 6.0; // ここ閾値のパラメーターです

    @EventHandler
    public void onDamaged(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player ) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();

        if (player.getHealth() > HEALTH_THRESHOLD) {
            return;

        } else {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            WorldBorder worldBorder = new WorldBorder();
            worldBorder.setSize(1);
            worldBorder.setCenter(craftPlayer.getLocation().getX() + 10_000, craftPlayer.getLocation().getZ() + 10_000);
            craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
        }
    }

    @EventHandler
    public void onHeal(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player ) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();

        if (!(HEALTH_THRESHOLD >= (player.getHealth() - event.getAmount()) && HEALTH_THRESHOLD < (player.getHealth() - event.getAmount()))) { // 閾値から回復した時
            return;

        } else {
            CraftPlayer craftPlayer = (CraftPlayer) player;
            WorldBorder worldBorder = new WorldBorder();
            worldBorder.setSize(30_000_000);
            worldBorder.setCenter(craftPlayer.getLocation().getX(), craftPlayer.getLocation().getZ());
            craftPlayer.getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));
        }
    }
}
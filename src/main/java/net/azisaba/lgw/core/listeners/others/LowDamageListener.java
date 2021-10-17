package net.azisaba.lgw.core.listeners.others;

public class LowDamageListener {
    private final double HEALTH_THRESHOLD = 6.0;
    @EventHandler
    public void onHit(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) event.getEntity();

        if (player.getHealth() > HEALTH_THRESHOLD) {
            return;
        } else {
            CraftPlayer cp = (CraftPlayer) p;

            WorldBorder w = new WorldBorder();
            w.setSize(1);
            w.setCenter(p.getLocation().getX() + 10_000, p.getLocation().getZ() + 10_000);
             .getHandle().playerConnection.sendPacket(new PacketPlayOutWorldBorder(w, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE));

        }
    }
}
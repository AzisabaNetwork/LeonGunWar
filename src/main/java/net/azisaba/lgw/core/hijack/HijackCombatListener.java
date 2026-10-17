package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.events.MatchFinishedEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class HijackCombatListener implements Listener {
    private final HijackController controller;
    public HijackCombatListener(HijackController controller) { this.controller = controller; }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        boolean explosion = controller.isExplosion(player)
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                || event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION;
        // Preserve void/escape enforcement, suicide, falls and environmental damage.
        boolean combat = event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK
                || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE;
        if (combat || explosion) event.setDamage(event.getDamage() * (1 - controller.reduction(player, explosion) / 100));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer != null) controller.publishKill(killer, event.getEntity());
    }

    // Snapshot before the existing finish listeners teleport players and clear teams/points.
    @EventHandler(priority = EventPriority.LOWEST)
    public void onFinish(MatchFinishedEvent event) {
        if (controller.isRunning()) controller.publishResult(event.getWinners());
    }
}

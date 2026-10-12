package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.AfkMatchReturn;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class EssentialsAfkListener implements Listener {
    private final LeonGunWar plugin;

    public EssentialsAfkListener(LeonGunWar plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAfk(AfkStatusChangeEvent event) {
        if (!event.getValue() || event.isCancelled()) return;
        Player player = event.getAffected().getBase();
        // Essentials can fire this event asynchronously and before committing the new state.
        plugin.getServer().getScheduler().runTask(plugin, () -> AfkMatchReturn.leave(plugin, player));
    }
}

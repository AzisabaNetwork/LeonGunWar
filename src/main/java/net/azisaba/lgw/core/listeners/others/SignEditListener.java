package net.azisaba.lgw.core.listeners.others;

import io.papermc.paper.event.player.PlayerOpenSignEvent;
import net.azisaba.lgw.core.util.AdventureUtil;
import net.azisaba.lgw.core.util.Chat;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Controls player-initiated editing of placed signs. */
public final class SignEditListener implements Listener {
    private static final String ADMIN_PERMISSION = "leongunwar.command.leongunwaradmin";
    private final Set<UUID> editingEnabled = new HashSet<>();

    public boolean toggle(Player player) {
        UUID id = player.getUniqueId();
        if (editingEnabled.remove(id)) return false;
        editingEnabled.add(id);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSignOpen(PlayerOpenSignEvent event) {
        if (event.getCause() == PlayerOpenSignEvent.Cause.PLACE) return;
        Player player = event.getPlayer();
        if (player.hasPermission(ADMIN_PERMISSION) && editingEnabled.contains(player.getUniqueId())) return;
        event.setCancelled(true);
        if (player.getGameMode() == GameMode.SURVIVAL) return;
        AdventureUtil.actionBar(player, Chat.f(player.hasPermission(ADMIN_PERMISSION)
                ? "&c看板を編集するには /toggle sign を使用してください"
                : "&c看板の編集は禁止されています"));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        editingEnabled.remove(event.getPlayer().getUniqueId());
    }
}

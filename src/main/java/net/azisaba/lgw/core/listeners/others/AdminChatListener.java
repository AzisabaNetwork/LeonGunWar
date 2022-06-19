package net.azisaba.lgw.core.listeners.others;

import java.util.ArrayList;
import net.azisaba.lgw.core.commands.AdminChatCommand;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * AdminChatをトグルできるようにするListener
 *
 * @author siloneco
 */
public class AdminChatListener implements Listener {

  private final AdminChatCommand adminChatCommand;

  public AdminChatListener(AdminChatCommand adminchat) {
    this.adminChatCommand = adminchat;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onChat(AsyncPlayerChatEvent e) {
    Player p = e.getPlayer();
    if (!adminChatCommand.isAdminChat(p)) {
      return;
    }
    if (!p.hasPermission("leongunwar.adminchat.send")) {
      adminChatCommand.setAdminChat(p, false);
      return;
    }

    e.setFormat(Chat.f("&b[&r%s&b] &d%s"));
    new ArrayList<>(e.getRecipients())
        .forEach(
            target -> {
              if (target.equals(p)) {
                return;
              }
              if (!target.hasPermission("leongunwar.adminchat.receive")) {
                e.getRecipients().remove(target);
              }
            });
  }
}

package net.azisaba.lgw.core.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.utils.Chat;

/**
 * Modとかにも聞こえるAdminChat
 *
 * @author siloneco
 *
 */
public class AdminChatCommand implements CommandExecutor {

    private final List<UUID> adminChats = new ArrayList<>();

    public boolean isAdminChat(Player p) {
        return adminChats.contains(p.getUniqueId());
    }

    public void setAdminChat(Player p, boolean value) {
        if ( value && !adminChats.contains(p.getUniqueId()) ) {
            adminChats.add(p.getUniqueId());
        } else if ( !value && adminChats.contains(p.getUniqueId()) ) {
            adminChats.remove(p.getUniqueId());
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( args.length <= 0 ) {
            if ( sender instanceof Player ) {
                Player p = (Player) sender;
                if ( adminChats.contains(p.getUniqueId()) ) {
                    adminChats.remove(p.getUniqueId());
                    p.sendMessage(Chat.f("&dAdminChat を&c無効化&dしました"));
                } else {
                    adminChats.add(p.getUniqueId());
                    p.sendMessage(Chat.f("&dAdminChat を&a有効化&dしました"));
                }
                return true;
            } else {
                sender.sendMessage(Chat.f("&cUsage: {0}", cmd.getUsage()));
                return true;
            }
        }

        String msg = String.join(" ", args);
        String format = Chat.f("&b[&r{0}&b] &d{1}", sender.getName(), msg);

        sender.sendMessage(format);

        Bukkit.getOnlinePlayers().forEach(p -> {
            if ( !p.hasPermission("leongunwar.adminchat.receive") ) {
                return;
            }
            if ( sender.equals(p) ) {
                return;
            }

            p.sendMessage(format);
        });
        return true;
    }
}

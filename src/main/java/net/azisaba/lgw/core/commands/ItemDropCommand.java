package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.listeners.others.PreventItemDropListener;
import net.azisaba.lgw.core.utils.Chat;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ItemDropCommand implements CommandExecutor {

    private final PreventItemDropListener listener;

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( !(sender instanceof Player) ) {
            return true;
        }

        Player p = (Player) sender;

        boolean now = listener.toggleAllowDrop(p);
        if ( now ) {
            p.sendMessage(Chat.f("&aアイテムドロップが可能になりました"));
        } else {
            p.sendMessage(Chat.f("&cアイテムドロップが不可能になりました"));
        }
        return true;
    }
}

package net.azisaba.lgw.core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.listeners.others.LimitActionListener;
import net.azisaba.lgw.core.utils.Chat;

import lombok.RequiredArgsConstructor;

public class LimitCommand implements CommandExecutor {

    private final LimitActionListener listener;

    public LimitCommand(LimitActionListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if ( !(sender instanceof Player) ) {
            sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です"));
            return true;
        }

        Player p = (Player) sender;

        if ( args.length <= 0 ) {
            p.sendMessage(Chat.f("&c使い方: /limit [&edrop&c/&ebuild&c]"));
            return true;
        }

        if ( args[0].equalsIgnoreCase("drop") ) {
            boolean now = listener.toggleAllowDrop(p);
            if ( now ) {
                p.sendMessage(Chat.f("&aアイテムドロップが可能になりました"));
            } else {
                p.sendMessage(Chat.f("&cアイテムドロップが不可能になりました"));
            }
        } else if ( args[0].equalsIgnoreCase("build") ) {
            boolean now = listener.toggleAllowBuild(p);
            if ( now ) {
                p.sendMessage(Chat.f("&a建築が可能になりました"));
            } else {
                p.sendMessage(Chat.f("&c建築が不可能になりました"));
            }
        }
        return true;
    }
}

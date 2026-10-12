package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.listeners.others.LimitActionListener;
import net.azisaba.lgw.core.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

public class LimitCommand implements CommandExecutor, TabCompleter {

    private final LimitActionListener listener;

    public LimitCommand(LimitActionListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です"));
            return true;
        }

        if (args.length <= 0) {
            p.sendMessage(Chat.f("&c使い方: /limit [&edrop&c/&ebuild&c]"));
            return true;
        }

        if (args[0].equalsIgnoreCase("drop")) {
            boolean now = listener.toggleAllowDrop(p);
            if (now) {
                p.sendMessage(Chat.f("&aアイテムドロップが可能になりました"));
            } else {
                p.sendMessage(Chat.f("&cアイテムドロップが不可能になりました"));
            }
        } else if (args[0].equalsIgnoreCase("build")) {
            boolean now = listener.toggleAllowBuild(p);
            if (now) {
                p.sendMessage(Chat.f("&a建築が可能になりました"));
            } else {
                p.sendMessage(Chat.f("&c建築が不可能になりました"));
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission("leongunwar.command.limit")) return List.of();
        String prefix = args[0].toLowerCase(Locale.ROOT);
        return List.of("drop", "build").stream().filter(option -> option.startsWith(prefix)).toList();
    }
}

package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.listeners.others.SignEditListener;
import net.azisaba.lgw.core.util.Chat;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.List;

public final class ToggleCommand implements TabExecutor {
    private final SignEditListener signEditListener;

    public ToggleCommand(SignEditListener signEditListener) {
        this.signEditListener = signEditListener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ実行可能です"));
            return true;
        }
        if (args.length != 1 || !args[0].equalsIgnoreCase("sign")) {
            player.sendMessage(Chat.f("&c使い方: /toggle sign"));
            return true;
        }
        boolean enabled = signEditListener.toggle(player);
        player.sendMessage(Chat.f(enabled ? "&a看板の編集を有効にしました" : "&c看板の編集を無効にしました"));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1 || !sender.hasPermission("leongunwar.command.leongunwaradmin")) return List.of();
        return "sign".startsWith(args[0].toLowerCase(java.util.Locale.ROOT)) ? List.of("sign") : List.of();
    }
}

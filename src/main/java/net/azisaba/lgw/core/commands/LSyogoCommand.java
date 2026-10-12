package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.syogo.SyogoGui;
import net.azisaba.lgw.core.syogo.SyogoManager;
import net.azisaba.lgw.core.syogo.SyogoStore;
import net.azisaba.lgw.core.syogo.SyogoTickets;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static net.azisaba.lgw.core.syogo.SyogoGui.color;

public final class LSyogoCommand implements TabExecutor {
    private final LeonGunWar plugin;
    private final SyogoManager manager;
    private final SyogoGui gui;
    private final SyogoTickets tickets;
    public LSyogoCommand(LeonGunWar plugin, SyogoManager manager, SyogoGui gui, SyogoTickets tickets) {
        this.plugin = plugin; this.manager = manager; this.gui = gui; this.tickets = tickets;
    }
    private static String permission(String sub) {
        return switch (sub) {
            case "give" -> "leongunwar.syogo.give";
            case "revoke" -> "leongunwar.syogo.revoke";
            case "ticket", "item", "bind" -> "leongunwar.syogo.item";
            case "check" -> "leongunwar.syogo.check";
            case "reload" -> "leongunwar.syogo.reload";
            case "remove", "unequip" -> "leongunwar.syogo.remove";
            default -> "leongunwar.command.syogo";
        };
    }
    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String sub = args.length == 0 ? "gui" : args[0].toLowerCase(Locale.ROOT);
        if (!sender.hasPermission(permission(sub))) { sender.sendMessage(color("&c権限がありません。")); return true; }
        if (sub.equals("gui")) {
            if (sender instanceof Player player) gui.open(player);
            else sender.sendMessage("プレイヤー専用です。");
            return true;
        }
        if (sub.equals("reload")) {
            try { plugin.getSyogoConfig().loadConfig(); sender.sendMessage(color("&a称号設定をリロードしました。")); }
            catch (Exception ex) { plugin.getLogger().warning("Cannot reload syogo.yml: " + ex); sender.sendMessage(color("&cリロードに失敗しました。ログを確認してね。")); }
            return true;
        }
        if (sub.equals("ticket") || sub.equals("item")) {
            if (args.length < 3 || args.length > 4) return false;
            Player target = Bukkit.getPlayerExact(args[1]);
            if (target == null) { sender.sendMessage(color("&c獲得券の発行先はオンラインのプレイヤーを指定してね。")); return true; }
            int amount;
            try { amount = args.length == 4 ? Integer.parseInt(args[3]) : 1; }
            catch (NumberFormatException ex) { sender.sendMessage(color("&c個数は1〜64の整数にしてね。")); return true; }
            tickets.issue(sender, target, args[2], amount, false);
            return true;
        }
        if (sub.equals("bind")) {
            if (!(sender instanceof Player player)) { sender.sendMessage("プレイヤー専用です。"); return true; }
            if (args.length != 2) return false;
            tickets.issue(sender, player, args[1], 1, true);
            return true;
        }
        if (sub.equals("select")) {
            if (!(sender instanceof Player player)) return false;
            if (args.length != 2) return false;
            manager.select(player, args[1], result -> report(sender, result));
            return true;
        }
        if (sub.equals("remove") || sub.equals("unequip")) {
            if (args.length > 2) return false;
            if (args.length == 1) {
                if (!(sender instanceof Player player)) return false;
                manager.select(player, null, result -> report(sender, result));
            } else {
                if (!sender.hasPermission("leongunwar.syogo.remove-admin")) { sender.sendMessage(color("&c権限がありません。")); return true; }
                Target target = target(sender, args[1]);
                if (target != null) manager.change(target.uuid(), () -> manager.store().select(target.uuid(), target.name(), null), result -> report(sender, result));
            }
            return true;
        }
        if (sub.equals("give") || sub.equals("revoke")) {
            if (args.length != 3) return false;
            if (sub.equals("give") && !plugin.getSyogoConfig().exist(args[2])) { sender.sendMessage(color("&c称号IDが存在しません。")); return true; }
            Target target = target(sender, args[1]);
            if (target == null) return true;
            sender.sendMessage(color("&7称号データを保存中…"));
            manager.change(target.uuid(), () -> sub.equals("give")
                    ? manager.store().grant(target.uuid(), target.name(), args[2])
                    : manager.store().revoke(target.uuid(), target.name(), args[2]), result -> report(sender, result));
            return true;
        }
        if (sub.equals("check")) {
            if (args.length != 2) return false;
            Target target = target(sender, args[1]);
            if (target == null) return true;
            manager.store().load(target.uuid(), target.name()).whenComplete((snapshot, error) -> manager.onMain(() -> {
                if (error != null) { sender.sendMessage(color("&c称号データを読み込めませんでした。")); return; }
                sender.sendMessage(color("&e" + target.name() + " &7装備中: &f" + (snapshot.activeId() == null ? "なし" : snapshot.activeId())));
                sender.sendMessage(color("&7所持称号: &f" + String.join(", ", snapshot.owned().stream().sorted().toList())));
            }));
            return true;
        }
        return false;
    }
    private record Target(UUID uuid, String name) { }
    private static Target target(CommandSender sender, String input) {
        Player online = Bukkit.getPlayerExact(input);
        if (online != null) return new Target(online.getUniqueId(), online.getName());
        try {
            UUID uuid = UUID.fromString(input);
            return new Target(uuid, input);
        } catch (IllegalArgumentException ignored) { }
        OfflinePlayer cached = Bukkit.getOfflinePlayerIfCached(input);
        if (cached != null) return new Target(cached.getUniqueId(), cached.getName() == null ? input : cached.getName());
        sender.sendMessage(color("&cプレイヤーが見つかりません。オンライン名またはUUIDで指定してね。"));
        return null;
    }
    private static void report(CommandSender sender, SyogoStore.Result result) {
        if (result == null) { sender.sendMessage(color("&c保存できませんでした。処理中なら少し待って、DB設定も確認してね。")); return; }
        sender.sendMessage(color(switch (result.status()) {
            case OK -> "&a称号データを保存しました！";
            case ALREADY_OWNED -> "&eこの称号は取得済みです。";
            case NOT_OWNED -> "&cこの称号を所持していません。";
            case INVALID_TICKET -> "&c無効な獲得券です。";
        }));
    }
    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> candidates = new ArrayList<>();
        String sub = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "";
        if (args.length == 1) {
            for (String value : List.of("gui", "select", "remove", "give", "revoke", "ticket", "bind", "check", "reload")) {
                if (sender.hasPermission(permission(value))) candidates.add(value);
            }
        } else if (sender.hasPermission(permission(sub))) {
            if (args.length == 2 && List.of("give", "revoke", "ticket", "item", "check").contains(sub)) Bukkit.getOnlinePlayers().forEach(p -> candidates.add(p.getName()));
            if (args.length == 2 && List.of("select", "bind").contains(sub)
                    || args.length == 3 && List.of("give", "revoke", "ticket", "item").contains(sub)) {
                if (sub.equals("select") && sender instanceof Player player) candidates.addAll(manager.snapshot(player).owned());
                else candidates.addAll(plugin.getSyogoConfig().entries().keySet());
            }
            if (args.length == 4 && List.of("ticket", "item").contains(sub)) candidates.addAll(List.of("1", "16", "64"));
        }
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return candidates.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().toList();
    }
}

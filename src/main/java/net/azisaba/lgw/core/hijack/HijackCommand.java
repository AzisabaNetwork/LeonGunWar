package net.azisaba.lgw.core.hijack;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class HijackCommand {
    private final HijackController controller;
    private final HijackAreaDisplay display;
    private final Logger logger;
    public HijackCommand(HijackController controller, HijackAreaDisplay display, Logger logger) {
        this.controller = controller; this.display = display; this.logger = logger;
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("leongunwar.command.leongunwaradmin")) {
            sender.sendMessage("§c/lgwadmin の権限が必要です。");
            return true;
        }
        String action = args.length > 1 ? args[1].toLowerCase(Locale.ROOT) : "show";
        try {
            switch (action) {
                case "show" -> {
                    sender.sendMessage("§bHIJACK設定（この鯖のみ）: " + controller.getConfig().getPath());
                    if (controller.isRunning()) {
                        sender.sendMessage("§b試合開始時の参加人数: " + controller.getStartingPlayerCount() + "人（途中参加・退出では変更しません）");
                        show(sender, "適用中", controller.getActive());
                    }
                    show(sender, controller.isRunning() ? "次の試合" : "開始時に適用", controller.getConfig().getPending());
                    sender.sendMessage("§e報酬連携: 未実装・無効（倍率は将来用の予約値です）");
                }
                case "reload" -> {
                    controller.getConfig().reload();
                    sender.sendMessage("§aHIJACK設定を読み込みました。この鯖の次の試合から適用します。");
                }
                case "preview" -> {
                    if (!(sender instanceof Player player)) throw new IllegalArgumentException("previewはプレイヤー専用です。");
                    String mode = args.length > 2 ? args[2].toLowerCase(Locale.ROOT) : controller.isRunning() ? "active" : "next";
                    if (mode.equals("stop") && args.length == 3) {
                        display.stopPreview(player);
                        sender.sendMessage("§a占領範囲のプレビューを停止しました。");
                    } else if (mode.equals("active") || mode.equals("next")) {
                        display.preview(player, mode.equals("next"), String.join(" ", Arrays.copyOfRange(args, Math.min(3, args.length), args.length)));
                    } else throw new IllegalArgumentException("/lgwadmin hijack preview [active|next <マップ名>|stop]");
                }
                case "set" -> {
                    if (args.length < 4) throw new IllegalArgumentException("/lgwadmin hijack set <設定名> <値>");
                    controller.getConfig().set(args[2], String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
                    sender.sendMessage("§a保存しました: " + args[2] + " = " + controller.getConfig().getPending().values().get(args[2])
                            + " §7（この鯖の次の試合から適用）");
                }
                default -> sender.sendMessage("§e/lgwadmin hijack <show|set <設定名> <値>|reload|preview [active|next <マップ名>|stop]>");
            }
        } catch (IllegalArgumentException exception) {
            sender.sendMessage("§c" + exception.getMessage());
        } catch (Exception exception) {
            logger.log(Level.WARNING, "HIJACK設定を読み込み/保存できませんでした。既存設定を維持します。", exception);
            sender.sendMessage("§c読み込み/保存に失敗しました。設定は変更していません。サーバーログを確認してください。");
        }
        return true;
    }
    private void show(CommandSender sender, String title, HijackRules rules) {
        sender.sendMessage("§6[" + title + "] " + rules.description());
        sender.sendMessage("§7HIJACK勝利アイテム条件: §f" + rules.victoryItemRequirement());
        rules.values().forEach((key, value) -> sender.sendMessage("§7" + key + " §f= " + value));
    }
    public List<String> complete(CommandSender sender, String[] args) {
        if (!sender.hasPermission("leongunwar.command.leongunwaradmin")) return List.of();
        List<String> choices = new ArrayList<>();
        if (args.length == 2) choices.addAll(List.of("show", "set", "reload", "preview"));
        else if (args.length == 3 && args[1].equalsIgnoreCase("preview")) choices.addAll(List.of("active", "next", "stop"));
        else if (args.length == 4 && args[1].equalsIgnoreCase("preview") && args[2].equalsIgnoreCase("next")) choices.addAll(display.mapNames());
        else if (args.length == 3 && args[1].equalsIgnoreCase("set")) choices.addAll(HijackRules.defaultsMap().keySet());
        else if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            Object type = HijackRules.defaultsMap().get(args[2]);
            if (type instanceof Boolean) choices.addAll(args[2].startsWith("rewards.") ? List.of("false") : List.of("true", "false"));
            else if (args[2].equals("victory.mode")) choices.addAll(List.of("points_or_time", "points", "time"));
            else if (args[2].equals("victory.tie")) choices.addAll(List.of("draw", "shared_win"));
            else if (args[2].equals("victory-item.mode")) choices.addAll(List.of("none", "time", "kills", "time_or_kills", "time_and_kills"));
            else if (type instanceof List<?> list) choices.add(String.join(",", list.stream().map(Object::toString).toList()));
            else if (args[2].endsWith(".target")) choices.addAll(List.of("all", "controller", "capturing"));
            else if (type != null) choices.add(type.toString());
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(item -> item.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().toList();
    }
}

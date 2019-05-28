package net.azisaba.lgw.core.commands;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Args;
import net.azisaba.lgw.core.utils.Chat;

/**
 *
 * @author siloneco
 *
 */
public class MatchCommand implements CommandExecutor, TabCompleter {

	// 連打防止のクールダウン
	private final HashMap<UUID, Long> cooldown = new HashMap<>();
	// このコマンドのPrefix
	private final String prefix = Chat.f("&7[&bQuick&7] &r");

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		// 1秒以内に実行したことがある場合はreturn
		if (sender instanceof Player
				&& cooldown.getOrDefault(((Player) sender).getUniqueId(), 0L) + 1000 > System.currentTimeMillis()) {
			return true;
		}

		if (sender instanceof Player) {
			cooldown.put(((Player) sender).getUniqueId(), System.currentTimeMillis());
		}

		// 引数がない場合は使用方法を表示してreturn
		if (Args.isEmpty(args)) {
			sender.sendMessage(Chat.f("&c使用方法: {0}", cmd.getUsage()));
			return true;
		}

		// 自身が対象かどうか
		boolean self = true;
		// Playerを取得。
		Player target = null;

		// 引数にプレイヤーが指定されている & 権限持ちならばそのプレイヤーを設定する
		if (Args.check(args, 1) && sender.hasPermission("")) {
			target = Bukkit.getPlayerExact(args[1]);

			// targetが存在しない場合はメッセージを表示してreturn
			if (target == null) {
				sender.sendMessage(Chat.f("&e{0} &cというプレイヤーが見つかりませんでした。", args[1]));
				return true;

			}

			// 自分以外のプレイヤーを指定していた場合はself = falseに変更
			if (!(sender instanceof Player) || (Player) sender != target) {
				self = false;
			}

			// 引数にプレイヤーが設定されておらず、senderがPlayerなら自身をtargetにする
		} else if (sender instanceof Player) {
			target = (Player) sender;
		}

		// 自分自身でありプレイヤーではない場合はreturn
		if (self && !(target instanceof Player)) {
			sender.sendMessage(Chat.f("&cあなたはプレイヤーではありません。", prefix));
			return true;
		}

		// 1つ目の引数がentryの場合
		if (Args.check(args, 0, "entry")) {
			boolean success = LeonGunWar.getPlugin().getManager().addEntryPlayer(target);

			if (success) { // エントリーした場合
				target.sendMessage(Chat.f("{0}&aゲームにエントリーしました。", prefix));

				if (!self) {
					sender.sendMessage(
							Chat.f("{0}&r{1} &7をエントリーさせました。", prefix, target.getDisplayName()));
				}
			} else { // すでにエントリーしている場合
				if (self) {
					sender.sendMessage(
							Chat.f("{0}&cあなたは既に参加しています。", prefix));
				} else {
					sender.sendMessage(
							Chat.f("{0}&r{1} &cは既に参加しています。", prefix, target.getDisplayName()));
				}
			}

			return true;
		}

		// 1つ目の引数がleaveの場合
		if (Args.check(args, 0, "leave")) {
			boolean success = LeonGunWar.getPlugin().getManager().removeEntryPlayer(target);

			if (success) { // エントリー解除した場合
				target.sendMessage(Chat.f("{0}&aゲームから退出しました。", prefix));

				if (!self) {
					sender.sendMessage(
							Chat.f("{0}&r{1} &7をゲームから退出させました。", prefix, target.getDisplayName()));
				}
			} else { // エントリーしていない場合
				if (self) {
					sender.sendMessage(
							Chat.f("{0}&7あなたはエントリーしていません。", prefix));
				} else {
					sender.sendMessage(
							Chat.f("{0}&r{1} &cは、エントリーしていません。", prefix, target.getDisplayName()));
				}
			}

			return true;
		}

		// 1つ目の引数がrejoinの場合
		if (Args.check(args, 0, "rejoin")) {

			// 試合中ではない場合return
			if (!LeonGunWar.getPlugin().getManager().isMatching()) {
				sender.sendMessage(Chat.f("{0}&7現在試合をしていないため途中参加はできません。", prefix));
				return true;
			}

			// プレイヤーを追加
			boolean success = LeonGunWar.getPlugin().getManager().addPlayerIntoBattle(target);

			if (success) { // 途中参加した場合
				target.sendMessage(Chat.f("{0}&7途中参加しました。", prefix));

				if (!self) {
					sender.sendMessage(
							Chat.f("{0}&r{1} &7を途中参加させました。", prefix, target.getDisplayName()));
				}
			} else { // すでに試合に参加している場合
				if (self) {
					sender.sendMessage(
							Chat.f("{0}&cあなたはすでに試合に参加しています。", prefix));
				} else {
					sender.sendMessage(
							Chat.f("{0}&r{1} &cはすでに試合に参加しています。", prefix, target.getDisplayName()));
				}
			}
			return true;
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if (args.length == 1) {
			return Args.complete(args, 0, "entry", "leave", "rejoin");
		}
		return null;
	}
}

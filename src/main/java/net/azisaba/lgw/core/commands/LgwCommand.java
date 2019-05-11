package net.azisaba.lgw.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.azisaba.lgw.core.LeonGunWar;

public class LgwCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO helpメッセージ実装
		if (args.length <= 0) {
			return true;
		}

		// debug1なら
		if (args[0].equalsIgnoreCase("debug_start")) {
			// 試合中ならreturn
			if (LeonGunWar.getPlugin().getManager().isMatching()) {
				return true;
			}
			// サーバー内のプレイヤーを試合に参加
			Bukkit.getOnlinePlayers().forEach(p -> {
				LeonGunWar.getPlugin().getManager().addEntryPlayer(p);
			});

			// カウントダウン終了
			LeonGunWar.getPlugin().getCountdown().stopCountdown();

			// 試合開始
			LeonGunWar.getPlugin().getManager().startMatch();
			return true;
		}

		return true;
	}
}

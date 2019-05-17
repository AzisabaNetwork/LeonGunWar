package net.azisaba.lgw.core.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.MatchMode;

public class LgwCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO helpメッセージ実装
		if (args.length <= 0) {
			return true;
		}

		// debug_startなら
		if (args[0].equalsIgnoreCase("debug_start")) {
			// 試合中ならreturn
			if (LeonGunWar.getPlugin().getManager().isMatching()) {
				return true;
			}
			// サーバー内のプレイヤーを試合に参加
			Bukkit.getOnlinePlayers().forEach(p -> {
				LeonGunWar.getPlugin().getManager().addEntryPlayer(p);
			});

			// モード指定されてなければTDMに指定
			if (LeonGunWar.getPlugin().getManager().getMatchMode() == null) {
				LeonGunWar.getPlugin().getManager().setMatchMode(MatchMode.TEAM_DEATH_MATCH);
			}

			// カウントダウン終了
			LeonGunWar.getPlugin().getCountdown().stopCountdown();

			// 試合開始
			LeonGunWar.getPlugin().getManager().startMatch();
			return true;
		}

		return true;
	}
}

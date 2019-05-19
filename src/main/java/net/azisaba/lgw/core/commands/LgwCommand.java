package net.azisaba.lgw.core.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;

public class LgwCommand implements CommandExecutor {

	// ミスって本家で実行してしまうとまずいので/lgw debug_startにロックをかけれるように
	private final boolean allowDebug = false;

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO helpメッセージ実装
		if (args.length <= 0) {
			return true;
		}

		// debug_startなら
		if (args[0].equalsIgnoreCase("debug_start")) {

			// allowDebugがfalseならreturn
			if (!allowDebug) {
				sender.sendMessage(Chat.f("&cこの引数は現在無効化されているため実行できません！"));
				return true;
			}

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

		// teleportかtpなら
		if (args[0].equalsIgnoreCase("teleport") || args[0].equalsIgnoreCase("tp")) {

			// senderがプレイヤーではない場合はreturn
			if (!(sender instanceof Player)) {
				sender.sendMessage(Chat.f("&cこのコマンドはプレイヤーのみ有効です！"));
				return true;
			}

			Player p = (Player) sender;

			// マップ名が指定されていない場合はreturn
			if (args.length <= 1) {
				p.sendMessage(Chat.f("&cマップ名を指定してください！"));
				return true;
			}

 			// args[1]移行を取得
			String mapName = String.join(" ", args).substring(args[0].length() + 1);

			// 指定されたマップがない場合はreturn
			List<GameMap> correctMapList = LeonGunWar.getPlugin().getMapContainer().getAllGameMap().stream()
					.filter(map -> map.getMapName().equalsIgnoreCase(mapName)
							|| map.getMapName().equalsIgnoreCase(mapName.replace(" ", "_")))
					.collect(Collectors.toList());

			// サイズが1ならテレポート
			if (correctMapList.size() == 1) {
				p.teleport(correctMapList.get(0).getSpawnPoint(BattleTeam.RED));
				p.sendMessage(Chat.f("&e{0} &7にテレポートしました。", correctMapList.get(0).getMapName()));

				// 1より多い場合
			} else if (correctMapList.size() > 1) {
				p.sendMessage(Chat.f("&cマッチしたマップが2つあります"));

				// 各マップのJSONMessageを表示
				correctMapList.forEach(map -> {
					Location spawn = map.getSpawnPoint(BattleTeam.RED);
					JSONMessage msg = JSONMessage.create(Chat.f("&7 - &e{0}: &7{1}, {2}, {3} &7({4})", map.getMapName(),
							spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getWorld().getName()));
					msg.tooltip(Chat.f("&eクリックでテレポート"));
					msg.runCommand(Chat.f("/essentials:tppos {0} {1} {2} {3} {4} {5}", spawn.getX(), spawn.getY(),
							spawn.getZ(), spawn.getYaw(), spawn.getPitch(), spawn.getWorld().getName()));
				});

				// 1より少ない場合 (0以下の場合)
			} else if (correctMapList.size() < 1) {
				p.sendMessage(Chat.f("&c指定したマップが見つかりませんでした。"));
			}

			return true;
		}

		return true;
	}
}

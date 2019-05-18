package net.azisaba.lgw.core.commands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.util.BattleTeam;
import net.md_5.bungee.api.ChatColor;

public class UAVCommand implements CommandExecutor {

	private final double uavRadius = 60d;
	private final double uavSeconds = 10d;

	private final HashMap<Player, Long> lastExecuted = new HashMap<>();

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// コンソールではない場合はreturn
		if (sender instanceof Player) {
			sender.sendMessage(ChatColor.RED + "このコマンドはConsoleでのみ実行可能です。");
			return true;
		}

		// UAVを使用したプレイヤーが指定されていない場合はreturn
		if (args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Usage: " + cmd.getUsage().replace("{LABEL}", label));
			return true;
		}

		// 使用したプレイヤーを取得
		Player shooter = Bukkit.getPlayerExact(args[0]);

		// プレイヤーが存在しない場合はメッセージを表示してreturn
		if (shooter == null) {
			sender.sendMessage(ChatColor.RED + "プレイヤーが見つかりません。");
			return true;
		}

		// LeonGunWar pluginが有効ではない場合はreturn
		if (!isLeonGunWarEnabled()) {
			LeonGunWar.getPlugin().getLogger().warning("LeonGunWarがロードされていないためUAVを正常に実行できませんでした。");
			return true;
		}

		// 前に実行した時間から1秒経っていない場合はreturn
		if (lastExecuted.getOrDefault(shooter, 0L) + 1000 > System.currentTimeMillis()) {
			return true;
		}

		// 実行した時間を指定
		lastExecuted.put(shooter, System.currentTimeMillis());

		// 試合のプレイヤーリスト取得
		MatchManager manager = LeonGunWar.getPlugin().getManager();
		List<Player> allPlayers = manager.getAllTeamPlayers();
		Map<BattleTeam, List<Player>> teamPlayerMap = manager.getTeamPlayers();

		// プレイヤーがチームに所属していない場合はメッセージを表示
		if (!allPlayers.contains(shooter)) {
			LeonGunWar.getPlugin().getLogger().warning(shooter.getName() + " はどのチームにも所属していません。");
			shooter.sendMessage(ChatColor.RED + "あなたはどのチームにも所属していません。");
			return true;
		}

		// 各チームのプレイヤーを取得し、発行を付与する
		for (BattleTeam team : teamPlayerMap.keySet()) {
			// プレイヤーリスト取得
			List<Player> players = teamPlayerMap.get(team);

			// 使用したプレイヤーが含まれている場合return
			if (players.contains(shooter)) {
				continue;
			}

			// 各プレイヤーに発行を付与する
			players.forEach(target -> {
				// ワールドが違う場合はreturn
				if (target.getLocation().getWorld() != shooter.getLocation().getWorld()) {
					return;
				}

				// 距離がConfigで指定された距離よりも遠い場合はreturn
				if (target.getLocation().distance(shooter.getLocation()) > uavRadius) {
					return;
				}

				// tickと強さを取得/設定
				int l = (int) (uavSeconds * 20);
				int amp = 1;

				// ログを出力
				LeonGunWar.getPlugin().getLogger()
						.info(target.getName() + "にGLOWINGを付与 (time=" + l + "ticks, level=" + amp + ")");

				// 発行を付与
				target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, l, amp, false, false));
			});
		}

		// 完了ログを出力
		LeonGunWar.getPlugin().getLogger().info("正常に " + shooter.getName() + " のUAVを実行しました。");
		return true;
	}

	/**
	 * LeonGunWar pluginが有効化されているかどうか確認します
	 * @return LeonGunWar pluginが有効化されていればtrue, されていなければfalse
	 */
	private boolean isLeonGunWarEnabled() {
		LeonGunWar plugin = (LeonGunWar) Bukkit.getPluginManager().getPlugin("LeonGunWar");

		if (plugin != null) {
			return plugin.isEnabled();
		}

		return false;
	}
}

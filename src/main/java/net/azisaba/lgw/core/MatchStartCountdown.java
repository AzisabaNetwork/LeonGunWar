package net.azisaba.lgw.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class MatchStartCountdown {

	private static LeonGunWar plugin;
	private static BukkitTask task = null;

	/**
	 * プラグインを取得するための初期化メソッド
	 * プラグインの起動時に呼び出されることを前提として作成されています
	 * @param plugin
	 */
	protected static void init(LeonGunWar plugin) {
		MatchStartCountdown.plugin = plugin;
	}

	/**
	 * カウントダウンが行われていない場合、カウントダウンをスタートします
	 */
	public static void startCountdown() {
		// すでにタイマースタートしている場合はreturn
		if (task != null) {
			return;
		}

		// Runnable取得してスタート
		task = getRunnable().runTaskTimer(plugin, 0, 20);
	}

	/**
	 * カウントダウンが実行されていた場合、カウントダウンを停止します
	 */
	public static void stopCountdown() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	/**
	 * 試合を開始する前のカウントダウンを実行するBukkitRunnableを作成します
	 * @return 作成されたカウントダウンのBukkitRunnable
	 */
	private static BukkitRunnable getRunnable() {
		return new BukkitRunnable() {

			private int timeLeft = 21;

			@Override
			public void run() {

				timeLeft--;

				// 0の場合ゲームスタート
				if (timeLeft <= 0) {
					stopCountdown();
					MatchManager.startMatch();
					return;
				}

				boolean chat = false;
				boolean title = false;

				// 以下の場合残り秒数をチャット欄もしくはタイトルに表示する
				if (timeLeft % 10 == 0) { // 10の倍数の場合
					chat = true;
				} else if (timeLeft <= 5) { // 数字が5以下の場合
					chat = true;
					title = true;
				}

				// chatがtrueの場合表示
				if (chat) {
					String msg = ChatColor.GRAY + "試合開始まで残り" + ChatColor.RED + timeLeft + ChatColor.GRAY + "秒！";
					Bukkit.broadcastMessage(msg);
				}

				// titleがtrueの場合表示
				if (title) {
					for (Player p : plugin.getServer().getOnlinePlayers()) {
						p.sendTitle(timeLeft + "", "", 0, 40, 10);
					}
				}
			}
		};
	}
}

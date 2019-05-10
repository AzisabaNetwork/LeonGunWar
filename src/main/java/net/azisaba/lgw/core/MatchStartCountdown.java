package net.azisaba.lgw.core;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class MatchStartCountdown implements Runnable {

	private static BukkitTask task = null;

	/**
	 * カウントダウンが行われていない場合、カウントダウンをスタートします
	 */
	public static void startCountdown() {
		// すでにタイマースタートしている場合はreturn
		if (task != null) {
			return;
		}

		// Runnable取得してスタート
		task = LeonGunWar.getPlugin().getServer().getScheduler().runTaskTimer(LeonGunWar.getPlugin(),
				new MatchStartCountdown(), 0, 20);
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

	//			private int timeLeft = 21;
	private int timeLeft = 5; // デバッグ

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
			for (Player p : LeonGunWar.getPlugin().getServer().getOnlinePlayers()) {
				p.sendTitle(timeLeft + "", "", 0, 40, 10);
			}
		}
	}
}

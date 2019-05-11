package net.azisaba.lgw.core;

import org.bukkit.scheduler.BukkitTask;

import net.azisaba.lgw.core.tasks.MatchStartCountdownTask;

public class MatchStartCountdown {

	private BukkitTask task = null;

	/**
	 * カウントダウンが行われていない場合、カウントダウンをスタートします
	 */
	public void startCountdown() {
		// すでにタイマースタートしている場合はreturn
		if (task != null) {
			return;
		}

		// Runnable取得してスタート
		task = new MatchStartCountdownTask().runTaskTimer(LeonGunWar.getPlugin(), 0, 20);
	}

	/**
	 * カウントダウンが実行されていた場合、カウントダウンを停止します
	 */
	public void stopCountdown() {
		if (task != null) {
			task.cancel();
			task = null;
		}
	}
}

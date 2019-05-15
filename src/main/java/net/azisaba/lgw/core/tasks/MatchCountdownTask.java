package net.azisaba.lgw.core.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;

public class MatchCountdownTask extends BukkitRunnable {

	@Override
	public void run() {
		// 1秒減らす
		int timeLeft = LeonGunWar.getPlugin().getManager().getTimeLeft().decrementAndGet();

		// イベントを呼び出す
		MatchTimeChangedEvent event = new MatchTimeChangedEvent(timeLeft);
		Bukkit.getPluginManager().callEvent(event);

		// 0になったらストップ
		if (timeLeft == 0) {
			cancel();
			return;
		}
	}
}

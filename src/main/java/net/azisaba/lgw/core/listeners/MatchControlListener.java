package net.azisaba.lgw.core.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.azisaba.lgw.core.events.MatchTimeChangedEvent;

public class MatchControlListener implements Listener {

	@EventHandler
	public void onMatchFinished(MatchTimeChangedEvent e) {
		// 時間を取得して0じゃなかったらreturn
		if (e.getTimeLeft() > 0) {
			return;
		}
	}
}

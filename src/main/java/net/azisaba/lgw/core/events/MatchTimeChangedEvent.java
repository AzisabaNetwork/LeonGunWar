package net.azisaba.lgw.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 試合の残り秒数が変化したときに呼び出されるイベント
 * @author siloneco
 *
 */
public class MatchTimeChangedEvent extends Event {

	// 現在の残り秒数
	private final int matchTime;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param matchTime 試合の残り時間
	 */
	public MatchTimeChangedEvent(int matchTime) {
		this.matchTime = matchTime;
	}

	public int getTimeLeft() {
		return matchTime;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}

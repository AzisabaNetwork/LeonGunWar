package net.azisaba.lgw.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * マッチが開始されたときに呼び出されるイベント
 * @author siloneco
 *
 */
public class MatchTimeChangedEvent extends Event {

	// 現在の残り秒数
	private int matchTime;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param redTeamPlayers 赤チームのプレイヤーリスト
	 * @param blueTeamPlayers 青チームのプレイヤーリスト
	 * @param map 試合を行うGameMap
	 */
	public MatchTimeChangedEvent(int matchTime) {
		this.matchTime = matchTime;
	}

	public int getMatchTime() {
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
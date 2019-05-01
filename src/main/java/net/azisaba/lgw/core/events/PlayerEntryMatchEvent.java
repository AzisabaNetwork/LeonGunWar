package net.azisaba.lgw.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * プレイヤーがエントリーしたときに呼び出されるイベント
 * @author siloneco
 *
 */
public class PlayerEntryMatchEvent extends Event {

	// エントリーしたプレイヤー
	private Player entryPlayer;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param redTeamPlayers 赤チームのプレイヤーリスト
	 * @param blueTeamPlayers 青チームのプレイヤーリスト
	 * @param map 試合を行うGameMap
	 */
	public PlayerEntryMatchEvent(Player entryPlayer) {
		this.entryPlayer = entryPlayer;
	}

	public Player getEntryPlayer() {
		return entryPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
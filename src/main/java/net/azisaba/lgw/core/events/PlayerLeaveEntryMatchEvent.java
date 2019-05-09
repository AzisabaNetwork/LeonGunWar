package net.azisaba.lgw.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * プレイヤーがエントリー解除したときに呼び出されるイベント
 * @author siloneco
 *
 */
public class PlayerLeaveEntryMatchEvent extends Event {

	// エントリー解除したプレイヤー
	private final Player leavePlayer;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param leavePlayer エントリー解除したプレイヤー
	 */
	public PlayerLeaveEntryMatchEvent(Player leavePlayer) {
		this.leavePlayer = leavePlayer;
	}

	public Player getEntryPlayer() {
		return leavePlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
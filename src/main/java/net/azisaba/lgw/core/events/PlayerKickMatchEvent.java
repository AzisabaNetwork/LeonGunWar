package net.azisaba.lgw.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * プレイヤーが試合からキックされたときに呼び出されるイベント
 * @author siloneco
 *
 */
public class PlayerKickMatchEvent extends Event {

	// キックされたプレイヤー
	private final Player kickedPlayer;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param kickPlayer キックされたプレイヤー
	 */
	public PlayerKickMatchEvent(Player kickedPlayer) {
		this.kickedPlayer = kickedPlayer;
	}

	public Player getPlayer() {
		return kickedPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}

package net.azisaba.lgw.core.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * プレイヤーがエントリーしたときに呼び出されるイベント
 * @author siloneco
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerEntryMatchEvent extends Event {

	// エントリーしたプレイヤー
	private final Player entryPlayer;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}

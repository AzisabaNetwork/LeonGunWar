package net.azisaba.lgw.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.lgw.core.teams.BattleTeam;

/**
 * チームのポイントが増えたときに呼び出されるイベント
 * @author siloneco
 *
 */
public class TeamPointIncreasedEvent extends Event {

	// ポイントが増えたチーム
	private final BattleTeam team;
	private final int currentPoint;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param entryPlayer エントリーしたプレイヤー
	 */
	public TeamPointIncreasedEvent(BattleTeam team, int currentPoint) {
		this.team = team;
		this.currentPoint = currentPoint;
	}

	public BattleTeam getTeam() {
		return team;
	}

	public int getCurrentPoint() {
		return currentPoint;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}

package net.azisaba.lgw.core.events;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.lgw.core.maps.GameMap;

/**
 * マッチが開始されたときに呼び出されるイベント
 * @author siloneco
 *
 */
public class MatchStartedEvent extends Event {

	// 各チームのプレイヤーリスト
	private List<Player> redTeamPlayers, blueTeamPlayers;
	// 試合を行うマップ
	private GameMap map;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param redTeamPlayers 赤チームのプレイヤーリスト
	 * @param blueTeamPlayers 青チームのプレイヤーリスト
	 * @param map 試合を行うGameMap
	 */
	public MatchStartedEvent(List<Player> redTeamPlayers, List<Player> blueTeamPlayers, GameMap map) {
		this.redTeamPlayers = redTeamPlayers;
		this.blueTeamPlayers = blueTeamPlayers;
		this.map = map;
	}

	public List<Player> getRedTeamPlayers() {
		return redTeamPlayers;
	}

	public List<Player> getBluePlayers() {
		return blueTeamPlayers;
	}

	public GameMap getGameMap() {
		return map;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
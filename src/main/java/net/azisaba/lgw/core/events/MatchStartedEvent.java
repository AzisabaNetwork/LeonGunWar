package net.azisaba.lgw.core.events;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.lgw.core.maps.GameMap;
import net.azisaba.lgw.core.teams.BattleTeam;

/**
 * マッチが開始されたときに呼び出されるイベント
 * @author siloneco
 *
 */
public class MatchStartedEvent extends Event {

	// 各チームのプレイヤーリスト
	private Map<BattleTeam, List<Player>> teamPlayers;
	// 試合を行うマップ
	private GameMap map;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param teamPlayers チームのプレイヤーリスト
	 * @param map 試合を行うGameMap
	 */
	public MatchStartedEvent(Map<BattleTeam, List<Player>> teamPlayers, GameMap map) {
		this.teamPlayers = teamPlayers;
		this.map = map;
	}

	public List<Player> getAllTeamPlayers() {
		return teamPlayers.values().stream().flatMap(List::stream).collect(Collectors.toList());
	}

	public List<Player> getTeamPlayers(BattleTeam team) {
		return teamPlayers.get(team);
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
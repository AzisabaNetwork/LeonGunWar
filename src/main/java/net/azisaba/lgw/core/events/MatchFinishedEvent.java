package net.azisaba.lgw.core.events;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.lgw.core.maps.GameMap;
import net.azisaba.lgw.core.teams.BattleTeam;

// TODO MVPとかの情報も載せたい。
/**
 * マッチが終了したときに呼び出されるイベント
 * @author siloneco
 *
 */
public class MatchFinishedEvent extends Event {

	// 各チームのプレイヤー
	private final Map<BattleTeam, List<Player>> teamPlayers;
	// マッチを行ったマップ
	private final GameMap map;
	// 勝利したチーム
	private final List<BattleTeam> winners;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param map マッチを行ったマップ
	 * @param winner 勝利したチーム
	 * @param teamPlayers チームのプレイヤー
	 */
	public MatchFinishedEvent(GameMap map, List<BattleTeam> winners, Map<BattleTeam, List<Player>> teamPlayers) {
		this.map = map;
		this.winners = winners;
		this.teamPlayers = teamPlayers;
	}

	public List<Player> getAllTeamPlayers() {
		return teamPlayers.values().stream().flatMap(List::stream).collect(Collectors.toList());
	}

	public List<Player> getTeamPlayers(BattleTeam team) {
		return teamPlayers.get(team);
	}

	public GameMap getMap() {
		return map;
	}

	public List<BattleTeam> getWinners() {
		return winners;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}

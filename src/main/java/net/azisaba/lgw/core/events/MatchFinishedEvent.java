package net.azisaba.lgw.core.events;

import java.util.List;

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
	private List<Player> redTeamPlayers, blueTeamPlayers;
	// マッチを行ったマップ
	private GameMap map;
	// 勝利したチーム
	private BattleTeam winner;

	private static final HandlerList HANDLERS_LIST = new HandlerList();

	/**
	 * @param map マッチを行ったマップ
	 * @param winner 勝利したチーム
	 * @param redTeamPlayer 赤チームのプレイヤー
	 * @param blueTeamPlayer 青チームのプレイヤー
	 */
	public MatchFinishedEvent(GameMap map, BattleTeam winner, List<Player> redTeamPlayer, List<Player> blueTeamPlayer) {
		this.map = map;
		this.winner = winner;
		this.redTeamPlayers = redTeamPlayer;
		this.blueTeamPlayers = blueTeamPlayer;
	}

	public List<Player> getRedTeamPlayers() {
		return redTeamPlayers;
	}

	public List<Player> getBlueTeamPlayers() {
		return blueTeamPlayers;
	}

	public GameMap getMap() {
		return map;
	}

	public BattleTeam getWinner() {
		return winner;
	}

	@Override
	public HandlerList getHandlers() {
		return HANDLERS_LIST;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS_LIST;
	}
}
package net.azisaba.lgw.core.events;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * マッチが開始されたときに呼び出されるイベント
 *
 * @author siloneco
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MatchStartedEvent extends Event {

    // 試合を行うマップ
    private GameMap map;
    // 各チームのプレイヤーリスト
    private Map<BattleTeam, List<Player>> teamPlayers;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public MatchStartedEvent(GameMap currentGameMap, Map<BattleTeam, List<Player>> teamPlayers) {
        this.map = currentGameMap;
        this.teamPlayers = teamPlayers;
    }

    public List<Player> getAllTeamPlayers() {
        return teamPlayers.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public List<Player> getTeamPlayers(BattleTeam team) {
        return teamPlayers.get(team);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}

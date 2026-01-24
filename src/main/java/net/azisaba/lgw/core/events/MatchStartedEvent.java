package net.azisaba.lgw.core.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * マッチが開始されたときに呼び出されるイベント
 *
 * @author siloneco
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MatchStartedEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    // 試合を行うマップ
    private GameMap map;
    // 各チームのプレイヤーリスト
    private Map<BattleTeam, List<Player>> teamPlayers;

    public MatchStartedEvent(GameMap currentGameMap, Map<BattleTeam, List<Player>> teamPlayers) {
        this.map = currentGameMap;
        this.teamPlayers = teamPlayers;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
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
}

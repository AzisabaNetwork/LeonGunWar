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

// TODO MVPとかの情報も載せたい。

/**
 * マッチが終了したときに呼び出されるイベント
 *
 * @author siloneco
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MatchFinishedEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    // マッチを行ったマップ
    private final GameMap map;
    // 勝利したチーム
    private final List<BattleTeam> winners;
    // 各チームのプレイヤー
    private final Map<BattleTeam, List<Player>> teamPlayers;

    public MatchFinishedEvent(GameMap map, List<BattleTeam> winners, Map<BattleTeam, List<Player>> teamPlayers) {
        this.map = map;
        this.winners = winners;
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

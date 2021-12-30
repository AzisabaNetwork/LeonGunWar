package net.azisaba.lgw.core.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import net.azisaba.lgw.core.util.BattleTeam;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * チームのポイントが増えたときに呼び出されるイベント
 *
 * @author siloneco
 *
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TeamPointIncreasedEvent extends Event {

    // ポイントが増えたチーム
    private final BattleTeam team;
    private final int currentPoint;

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    public TeamPointIncreasedEvent(BattleTeam team,int currentPoint) {
        this.team = team;
        this.currentPoint = currentPoint;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}

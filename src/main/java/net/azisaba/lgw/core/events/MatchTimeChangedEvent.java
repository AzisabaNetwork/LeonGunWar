package net.azisaba.lgw.core.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * 試合の残り秒数が変化したときに呼び出されるイベント
 *
 * @author siloneco
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MatchTimeChangedEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    // 現在の残り秒数
    private final int timeLeft;

    public MatchTimeChangedEvent(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}

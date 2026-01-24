package net.azisaba.lgw.core.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * プレイヤーのアシストが増えたときに呼び出されるイベント
 *
 * @author siloneco
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerAssistEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    // アシストをしたプレイヤー
    private final Player player;

    public PlayerAssistEvent(Player player) {
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}

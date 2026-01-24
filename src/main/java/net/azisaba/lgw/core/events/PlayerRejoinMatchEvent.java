package net.azisaba.lgw.core.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerRejoinMatchEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    // 途中参加をしたプレイヤー
    private final Player player;

    public PlayerRejoinMatchEvent(Player player) {
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

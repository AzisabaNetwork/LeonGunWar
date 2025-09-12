package net.azisaba.lgw.core.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * プレイヤーがエントリーしたときに呼び出されるイベント
 *
 * @author siloneco
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PlayerEntryMatchEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();
    // エントリーしたプレイヤー
    private final Player entryPlayer;

    public PlayerEntryMatchEvent(Player player) {
        entryPlayer = player;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }
}

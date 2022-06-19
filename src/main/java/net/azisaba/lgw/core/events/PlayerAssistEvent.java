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

  // アシストをしたプレイヤー
  private final Player player;

  private static final HandlerList HANDLERS_LIST = new HandlerList();

  public PlayerAssistEvent(Player player) {
    this.player = player;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS_LIST;
  }
}

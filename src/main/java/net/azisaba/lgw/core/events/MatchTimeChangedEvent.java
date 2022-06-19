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

  // 現在の残り秒数
  private final int timeLeft;

  private static final HandlerList HANDLERS_LIST = new HandlerList();

  public MatchTimeChangedEvent(int timeLeft) {
    this.timeLeft = timeLeft;
  }

  @Override
  public HandlerList getHandlers() {
    return HANDLERS_LIST;
  }

  public static HandlerList getHandlerList() {
    return HANDLERS_LIST;
  }
}

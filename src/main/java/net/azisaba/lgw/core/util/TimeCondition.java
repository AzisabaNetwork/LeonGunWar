package net.azisaba.lgw.core.util;

import java.util.Date;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class TimeCondition {

  private final Date start;
  private final Date end;

  public boolean isDuring() {
    long current = System.currentTimeMillis();
    return start.getTime() <= current && current <= end.getTime();
  }
}

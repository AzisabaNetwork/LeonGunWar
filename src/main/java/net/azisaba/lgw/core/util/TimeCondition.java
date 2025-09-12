package net.azisaba.lgw.core.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Date;

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

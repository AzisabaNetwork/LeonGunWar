package net.azisaba.lgw.core.util;

import java.util.Date;

public record TimeCondition(Date start, Date end) {

    public boolean isDuring() {
        long current = System.currentTimeMillis();
        return start.getTime() <= current && current <= end.getTime();
    }
}

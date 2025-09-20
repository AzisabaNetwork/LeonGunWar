package net.azisaba.lgw.core.api.killstreak;

import java.util.Date;

public record TimeCondition(Date start, Date end) {

    public boolean isDuring() {
        long current = System.currentTimeMillis();
        return start.getTime() <= current && current <= end.getTime();
    }
}

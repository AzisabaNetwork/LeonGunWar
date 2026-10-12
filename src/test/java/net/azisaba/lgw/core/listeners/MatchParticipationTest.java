package net.azisaba.lgw.core.listeners;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MatchParticipationTest {
    private final MatchControlListener listener = new MatchControlListener();

    @Test
    void missingJoinTimeDoesNotThrowOrQualifyForDurationReward() {
        assertFalse(listener.isOverSixMinutes(1_000_000L, null));
    }

    @Test
    void sixMinuteRewardBoundaryIsPreserved() {
        assertFalse(listener.isOverSixMinutes(1_000_000L, 640_001L));
        assertTrue(listener.isOverSixMinutes(1_000_000L, 640_000L));
    }

    @Test
    void futureJoinTimeDoesNotQualify() {
        assertFalse(listener.isOverSixMinutes(1_000_000L, 2_000_000L));
    }
}

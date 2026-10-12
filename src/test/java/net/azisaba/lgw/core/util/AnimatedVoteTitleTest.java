package net.azisaba.lgw.core.util;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnimatedVoteTitleTest {
    @Test
    void usesRequestedColorsAndLoopsBackToFirst() {
        assertEquals(TextColor.color(0xBABCBF), AnimatedVoteTitle.colorAt(0));
        assertEquals(TextColor.color(0xB5DFE8), AnimatedVoteTitle.colorAt(1));
        assertEquals(TextColor.color(0xEBC7EB), AnimatedVoteTitle.colorAt(2));
        assertEquals(AnimatedVoteTitle.colorAt(0), AnimatedVoteTitle.colorAt(3));
    }

    @Test
    void preservesTitleAndRepeatsWithoutJumping() {
        String title = "LeonGunWarII v1.2.1-another";
        var start = AnimatedVoteTitle.render(title, 0);
        assertEquals(title, PlainTextComponentSerializer.plainText().serialize(start));
        assertEquals(start, AnimatedVoteTitle.render(title, AnimatedVoteTitle.PERIOD_TICKS));
        assertNotEquals(start, AnimatedVoteTitle.render(title, 2));
    }

    @Test
    void adjacentFramesRemainSmoothIncludingLoopBoundary() {
        for (int tick = 0; tick < AnimatedVoteTitle.PERIOD_TICKS; tick += 2) {
            for (int character = 0; character < 25; character++) {
                double position = character * 2.0 / 24;
                TextColor from = AnimatedVoteTitle.colorAt(position + tick * 3.0 / AnimatedVoteTitle.PERIOD_TICKS);
                TextColor to = AnimatedVoteTitle.colorAt(position + (tick + 2) * 3.0 / AnimatedVoteTitle.PERIOD_TICKS);
                assertTrue(Math.abs(from.red() - to.red()) <= 5);
                assertTrue(Math.abs(from.green() - to.green()) <= 5);
                assertTrue(Math.abs(from.blue() - to.blue()) <= 5);
            }
        }
    }
}

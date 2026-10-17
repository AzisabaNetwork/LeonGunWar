package net.azisaba.lgw.core.hijack;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HijackObjectivesTest {
    @TempDir Path directory;
    private HijackRules rules(Map<String, Object> changes) {
        var values = new HashMap<>(HijackRules.defaults().values());
        values.putAll(changes);
        return new HijackRules(values);
    }

    @Test void populationThresholdsSelectHighestMatchingMinimumRegardlessOfOrder() {
        var rules = rules(Map.of("victory.player-count.enabled", true,
                "victory.player-count.targets", List.of("12:200", "1:100", "6:150")));
        assertEquals(200, rules.forPlayerCount(0).points());
        assertEquals(100, rules.forPlayerCount(2).points());
        assertEquals(100, rules.forPlayerCount(5).points());
        assertEquals(150, rules.forPlayerCount(6).points());
        assertEquals(150, rules.forPlayerCount(11).points());
        assertEquals(200, rules.forPlayerCount(12).points());
        assertEquals(200, rules.forPlayerCount(100).points());
        assertEquals(200, rules.points()); // Resolving a match does not mutate the pending rules.
    }

    @Test void disabledOrUnmatchedPopulationRulesUseConfiguredFallback() {
        var disabled = rules(Map.of("victory.player-count.enabled", false, "victory.target-points", 270));
        assertEquals(270, disabled.forPlayerCount(5).points());
        var sparse = rules(Map.of("victory.player-count.enabled", true, "victory.player-count.targets", List.of("10:300")));
        assertEquals(200, sparse.forPlayerCount(9).points());
        assertEquals(300, sparse.forPlayerCount(10).points());
        assertThrows(IllegalArgumentException.class, () -> sparse.forPlayerCount(-1));
    }

    @Test void invalidOrDuplicatePopulationBandsNeverPublishOrOverwriteConfiguration() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml")); config.reload();
        String original = Files.readString(config.getPath());
        for (String invalid : List.of("[]", "1:100,1:200", "0:100", "2:0", "2:1000001", "2.5:100", "2:100:extra", "wrong")) {
            assertThrows(IllegalArgumentException.class, () -> config.set("victory.player-count.targets", invalid), invalid);
            assertEquals(original, Files.readString(config.getPath()));
            assertEquals(HijackRules.defaults(), config.getPending());
        }
    }

    @Test void victoryItemModesSupportTimeKillsEitherBothAndNoRequirement() {
        long now = 1_000_000;
        for (var mode : HijackRules.VictoryItem.values()) {
            var rule = rules(Map.of("victory-item.mode", mode.name().toLowerCase(java.util.Locale.ROOT)));
            boolean neither = mode == HijackRules.VictoryItem.NONE;
            boolean timeOnly = neither || mode == HijackRules.VictoryItem.TIME || mode == HijackRules.VictoryItem.TIME_OR_KILLS;
            boolean killsOnly = neither || mode == HijackRules.VictoryItem.KILLS || mode == HijackRules.VictoryItem.TIME_OR_KILLS;
            assertEquals(neither, rule.qualifiesForVictoryItem(now, now, 0), mode.toString());
            assertEquals(timeOnly, rule.qualifiesForVictoryItem(now, now - 360_000L, 0), mode.toString());
            assertEquals(killsOnly, rule.qualifiesForVictoryItem(now, now, 20), mode.toString());
            assertTrue(rule.qualifiesForVictoryItem(now, now - 360_000L, 20), mode.toString());
        }
    }

    @Test void timeAndKillBoundariesUseConfiguredValuesAndRejectUnknownJoinTime() {
        var rules = rules(Map.of("victory-item.mode", "time_and_kills", "victory-item.minimum-participation-seconds", 30,
                "victory-item.minimum-kills", 3));
        assertTrue(rules.qualifiesForVictoryItem(100_000, 70_000L, 3));
        assertFalse(rules.qualifiesForVictoryItem(100_000, 70_001L, 3));
        assertFalse(rules.qualifiesForVictoryItem(100_000, 70_000L, 2));
        assertFalse(rules.qualifiesForVictoryItem(100_000, null, 100));
        assertFalse(rules.qualifiesForVictoryItem(100_000, 100_001L, 100));
    }

    @Test void zeroThresholdsAndInvalidModesHaveExplicitBehavior() {
        var zero = rules(Map.of("victory-item.mode", "time_and_kills", "victory-item.minimum-participation-seconds", 0,
                "victory-item.minimum-kills", 0));
        assertTrue(zero.qualifiesForVictoryItem(100, 100L, 0));
        assertFalse(zero.qualifiesForVictoryItem(100, null, 0));
        assertThrows(IllegalArgumentException.class, () -> rules(Map.of("victory-item.minimum-kills", -1)));
        assertThrows(IllegalArgumentException.class, () -> rules(Map.of("victory-item.minimum-participation-seconds", -1)));
        assertThrows(IllegalArgumentException.class, () -> rules(Map.of("victory-item.mode", "unknown")));
    }
}

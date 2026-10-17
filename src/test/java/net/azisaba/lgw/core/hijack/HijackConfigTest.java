package net.azisaba.lgw.core.hijack;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HijackConfigTest {
    @TempDir Path directory;

    @Test void defaultsMatchBundledTemplateAndAreImmutable() throws Exception {
        try (var resource = getClass().getResourceAsStream("/configs/hijack.yml")) {
            var yaml = new YamlConfiguration();
            yaml.loadFromString(new String(resource.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            assertEquals(HijackRules.defaults(), HijackRules.read(yaml));
        }
        assertThrows(UnsupportedOperationException.class, () -> HijackRules.defaults().values().put("victory.target-points", 1));
    }

    @Test void commandSavesAndReloadsWithoutChangingOldMatchSnapshot() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml"));
        config.reload();
        var old = config.getPending();
        config.set("victory.target-points", "321");
        config.set("victory-item.enabled", "false");
        config.set("weapons.blocked-ids", "C4_Special, Grenade2");
        var reloaded = new HijackConfig(config.getPath());
        reloaded.reload();
        assertEquals(321, reloaded.getPending().points());
        assertFalse(reloaded.getPending().flag("victory-item.enabled"));
        assertTrue(old.flag("victory-item.enabled"));
        assertEquals(200, old.points());
        assertEquals(List.of("C4_Special", "Grenade2"), reloaded.getPending().blockedIds());
        config.set("weapons.blocked-ids", "[]");
        assertTrue(config.getPending().blockedIds().isEmpty());
    }

    @Test void invalidValuesDoNotChangeDiskOrMemory() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml"));
        config.reload();
        String original = Files.readString(config.getPath());
        for (String[] change : List.of(new String[]{"resistance.percent", "101"},
                new String[]{"resistance.percent", "NaN"}, new String[]{"resistance.percent", "-1"},
                new String[]{"victory.target-points", "1.5"}, new String[]{"victory.time-limit-seconds", "86400"},
                new String[]{"victory.mode", "typo"}, new String[]{"resistance.enabled", "yes"},
                new String[]{"rewards.area-kill.enabled", "true"}, new String[]{"rewards.victory.enabled", "true"},
                new String[]{"weapons.blocked-ids", "A,,B"}, new String[]{"unknown", "1"})) {
            assertThrows(IllegalArgumentException.class, () -> config.set(change[0], change[1]), change[0]);
            assertEquals(original, Files.readString(config.getPath()));
            assertEquals(HijackRules.defaults(), config.getPending());
        }
    }

    @Test void malformedReloadAndFailedSaveRetainPendingRules() throws Exception {
        Path file = directory.resolve("hijack.yml");
        var config = new HijackConfig(file);
        config.reload();
        Files.writeString(file, "victory: [broken");
        assertThrows(Exception.class, config::reload);
        assertEquals(HijackRules.defaults(), config.getPending());
        Files.delete(file);
        Files.createDirectory(file);
        assertThrows(Exception.class, () -> config.set("victory.target-points", "1"));
        assertEquals(200, config.getPending().points());
    }

    @Test void editUsesLatestDiskValuesAndValidatesWholeDocument() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml"));
        config.reload();
        Files.writeString(config.getPath(), "resistance:\n  percent: 25\n");
        config.set("victory.mode", "time");
        assertEquals(25, config.getPending().number("resistance.percent"));
        assertFalse(config.getPending().hasPointLimit());
        assertTrue(config.getPending().hasTimeLimit());
        Files.writeString(config.getPath(), "resistance:\n  percent: wrong\n");
        assertThrows(IllegalArgumentException.class, () -> config.set("victory.mode", "points"));
        assertEquals(HijackRules.Victory.TIME, config.getPending().victory());
    }

    @Test void allModeAndTargetChoicesParseAndMutableInputsAreCopied() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml"));
        config.set("victory.mode", "points");
        config.set("resistance.target", "controller");
        config.set("victory.tie", "shared_win");
        assertFalse(config.getPending().hasTimeLimit());
        assertTrue(config.getPending().hasPointLimit());
        assertFalse(config.getPending().drawOnTie());
        assertEquals(HijackRules.Target.CONTROLLER, config.getPending().target("resistance"));
        config.set("resistance.target", "capturing");
        config.set("explosion-resistance.target", "capturing");
        assertEquals(HijackRules.Target.CAPTURING, config.getPending().target("resistance"));
        assertEquals(HijackRules.Target.CAPTURING, config.getPending().target("explosion-resistance"));
        var values = new HashMap<>(config.getPending().values());
        var rules = new HijackRules(values);
        values.put("victory.target-points", 1);
        assertEquals(200, rules.points());
    }

    @Test void retiredCaptureSettingsAreRemovedWithoutChangingOtherRulesAndCannotBeSetAgain() throws Exception {
        Path file = directory.resolve("hijack.yml");
        Files.writeString(file, """
                capture:
                  expand-enabled: true
                  minimum-width: 64
                  jump-height: 0
                resistance:
                  percent: 25
                """);
        assertEquals(25, HijackRules.read(YamlConfiguration.loadConfiguration(file.toFile())).number("resistance.percent"));
        var config = new HijackConfig(file); config.reload();
        assertEquals(25, config.getPending().number("resistance.percent"));
        assertFalse(YamlConfiguration.loadConfiguration(file.toFile()).contains("capture"));
        assertTrue(config.getPending().values().keySet().stream().noneMatch(key -> key.startsWith("capture.")));
        String migrated = Files.readString(file);
        for (String key : List.of("capture.expand-enabled", "capture.minimum-width", "capture.jump-height")) {
            assertThrows(IllegalArgumentException.class, () -> config.set(key, "1"));
            assertEquals(migrated, Files.readString(file));
        }
    }

    @Test void failedValidationDoesNotPartiallyMigrateLegacyFilesButAValidEditDoes() throws Exception {
        var config = new HijackConfig(directory.resolve("hijack.yml")); config.reload();
        Files.writeString(config.getPath(), "capture:\n  jump-height: 2\nresistance:\n  percent: wrong\n");
        String original = Files.readString(config.getPath());
        assertThrows(IllegalArgumentException.class, config::reload);
        assertEquals(original, Files.readString(config.getPath()));
        assertEquals(15, config.getPending().number("resistance.percent"));
        config.set("resistance.percent", "30");
        assertEquals(30, config.getPending().number("resistance.percent"));
        assertFalse(YamlConfiguration.loadConfiguration(config.getPath().toFile()).contains("capture"));
        Files.writeString(config.getPath(), "capture:\n  unknown: true\n");
        assertThrows(IllegalArgumentException.class, config::reload);
    }
}

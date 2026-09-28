package net.azisaba.lgw.core.killaction;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class KillActionCatalogTest {
    @Test
    void sampleDefinitionsParse() throws Exception {
        for (KillActionCatalog.Category category : KillActionCatalog.Category.values()) {
            try (InputStream input = getClass().getResourceAsStream("/" + category.folder + "/example.yml")) {
                assertNotNull(input);
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                String id = yaml.getKeys(false).iterator().next();
                var section = yaml.getConfigurationSection(id);
                assertNotNull(section);
                assertFalse(section.getString("Unlock.permission", "").isBlank());
                assertFalse(section.getString("Unlock.description", "").isBlank());
                if (category == KillActionCatalog.Category.SOUND)
                    assertEquals(4, KillActionCatalog.parseSoundSpecs(section.getString("Sounds"), 100).size());
                if (category == KillActionCatalog.Category.PARTICLE) {
                    assertEquals(2, section.getMapList("Effects").size());
                    assertEquals(2, KillActionCatalog.parseFrames(section.getMapList("Effects").get(1), 100).size());
                }
            }
        }
    }

    @Test
    void soundDuplicatesAndAbsoluteDelaysArePreserved() {
        var cues = KillActionCatalog.parseSoundSpecs("ITEM_SHIELD_BLOCK-2-0.7-0,ITEM_SHIELD_BLOCK-2-0.7-0,ITEM_FIRECHARGE_USE-1-1-5", 100);
        assertEquals(3, cues.size());
        assertEquals(cues.get(0), cues.get(1));
        assertEquals(5, cues.get(2).tick());
        assertThrows(IllegalArgumentException.class,
                () -> KillActionCatalog.parseSoundSpecs("ITEM_SHIELD_BLOCK-1-1-101", 100));
    }

    @Test
    void allBuiltInShapesProduceFiniteCoordinates() {
        for (String shape : KillActionCatalog.SHAPES) {
            if (shape.equals("points")) continue;
            var effect = new KillActionCatalog.ParticleEffect(null, null, shape,
                    false, 0, 20, 1, 48, 2, 2, 0, List.of());
            List<KillActionCatalog.Point> points = ParticlePattern.points(effect, 10);
            assertEquals(48, points.size(), shape);
            assertTrue(points.stream().allMatch(point -> Double.isFinite(point.x())
                    && Double.isFinite(point.y()) && Double.isFinite(point.z())), shape);
        }
    }
}

package net.azisaba.lgw.core.killaction;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import org.bukkit.Material;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KillActionCatalogTest {
    @Test void dbTicketDefinitionsDoNotRequireLuckPermsPermission() throws Exception {
        Material icon = mock(Material.class); when(icon.isItem()).thenReturn(true);
        try (var materials = mockStatic(Material.class, CALLS_REAL_METHODS)) {
            materials.when(() -> Material.matchMaterial("PAPER")).thenReturn(icon);
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString("""
                    Displayname: 'イベントログ'
                    GUIicon: PAPER
                    lore: 'イベントで取得'
                    rarity: special
                    Unlock:
                      description: '獲得券で解放'
                    Format: '{killer} -> {victim}'
                    """);
            assertEquals("", KillActionCatalog.parse(KillActionCatalog.Category.LOG, "ticket", yaml, 100).permission());
        }
    }
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
    void samplePacksContainValidDistinctEntries() throws Exception {
        for (KillActionCatalog.Category category : KillActionCatalog.Category.values()) {
            try (InputStream input = getClass().getResourceAsStream("/" + category.folder + "/sample-pack.yml")) {
                assertNotNull(input);
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.loadFromString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
                assertTrue(yaml.getKeys(false).size() >= 2);
                for (String id : yaml.getKeys(false)) {
                    var section = yaml.getConfigurationSection(id);
                    assertNotNull(section, id);
                    assertFalse(section.getString("Unlock.permission", "").isBlank(), id);
                    assertFalse(section.getString("Displayname", "").isBlank(), id);
                    assertFalse(section.getString("GUIicon", "").isBlank(), id);
                    if (category == KillActionCatalog.Category.SOUND)
                        assertFalse(KillActionCatalog.parseSoundSpecs(section.getString("Sounds"), 100).isEmpty(), id);
                    if (category == KillActionCatalog.Category.LOG)
                        assertTrue(section.getString("Format", "").contains("{victim}"), id);
                    if (category == KillActionCatalog.Category.PARTICLE) {
                        assertFalse(section.getMapList("Effects").isEmpty(), id);
                        for (var effect : section.getMapList("Effects"))
                            assertTrue(KillActionCatalog.SHAPES.contains(effect.get("shape")), id);
                    }
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

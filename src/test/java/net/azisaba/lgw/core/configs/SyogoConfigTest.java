package net.azisaba.lgw.core.configs;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SyogoConfigTest {
    @Test void hexTitlesNormalizeForGuiAndChatInBothDefinitionFormats() throws Exception {
        Material icon = mock(Material.class);
        when(icon.isItem()).thenReturn(true);
        try (var materials = mockStatic(Material.class, CALLS_REAL_METHODS)) {
            materials.when(() -> Material.matchMaterial("DIAMOND")).thenReturn(icon);
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString("""
                    old: '&xB5DFE8[古参]'
                    rich:
                      Displayname: '&#EBC7EB[支援者]'
                      GUIicon: DIAMOND
                      lore: '&xBABCBF説明|&6従来の色'
                    """);
            var entries = SyogoConfig.parse(yaml, message -> fail(message));
            assertEquals("&x&B&5&D&F&E&8[古参]", entries.get("old").display());
            assertEquals("&x&E&B&C&7&E&B[支援者]", entries.get("rich").display());
            assertEquals(List.of("&x&B&A&B&C&B&F説明", "&6従来の色"), entries.get("rich").lore());
        }
    }
    @Test void oldStringDefinitionsRemainCompatible() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.loadFromString("veteran: '&6[古参]'\n");
        var entries = SyogoConfig.parse(yaml, message -> fail(message));
        assertEquals("&6[古参]", entries.get("veteran").display());
        assertEquals(Material.NAME_TAG, entries.get("veteran").icon());
        assertEquals(List.of(), entries.get("veteran").lore());
    }
    @Test void richDefinitionsSupportStringAndListLoreWithPipeBreaks() throws Exception {
        // The live item registry is owned by Paper; stub just that boundary.
        Material icon = mock(Material.class);
        when(icon.isItem()).thenReturn(true);
        try (var materials = mockStatic(Material.class, CALLS_REAL_METHODS)) {
            materials.when(() -> Material.matchMaterial("DIAMOND")).thenReturn(icon);
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString("""
                    one:
                      Displayname: '&d[支援者]'
                      GUIicon: DIAMOND
                      lore: '説明1|説明2|'
                    two:
                      Displayname: '&b[参加者]'
                      GUIicon: DIAMOND
                      lore: ['説明A|説明B', '説明C']
                    """);
            var entries = SyogoConfig.parse(yaml, message -> fail(message));
            assertEquals(List.of("説明1", "説明2", ""), entries.get("one").lore());
            assertEquals(List.of("説明A", "説明B", "説明C"), entries.get("two").lore());
            assertSame(icon, entries.get("two").icon());
        }
    }
    @Test void malformedEntriesAreSkippedWithIds() throws Exception {
        YamlConfiguration yaml = new YamlConfiguration();
        // Keep a literal dotted key here so the ID validator sees the original value.
        yaml.options().pathSeparator('/');
        yaml.loadFromString("""
                'bad id': 'display'
                'bad.id': 'display'
                empty: ''
                wrongType: 123
                badIcon:
                  Displayname: 'display'
                  GUIicon: DOES_NOT_EXIST
                good: '&a[OK]'
                """);
        List<String> warnings = new ArrayList<>();
        var entries = SyogoConfig.parse(yaml, warnings::add);
        assertEquals(List.of("good"), entries.keySet().stream().toList());
        assertEquals(5, warnings.size());
        for (String id : List.of("bad id", "bad.id", "empty", "wrongType", "badIcon")) {
            assertTrue(warnings.stream().anyMatch(message -> message.contains("syogo.yml") && message.contains(id)), id);
        }
    }
    @Test void bundledExamplesHaveDisplayAndLore() throws Exception {
        try (InputStream input = getClass().getResourceAsStream("/configs/syogo.yml")) {
            assertNotNull(input);
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            assertEquals(3, yaml.getKeys(false).size());
            for (String id : yaml.getKeys(false)) {
                assertFalse(yaml.getString(id + ".Displayname", "").isBlank());
                assertNotNull(Material.matchMaterial(yaml.getString(id + ".GUIicon", "")));
                assertFalse(yaml.getString(id + ".lore", "").isBlank());
            }
        }
    }
}

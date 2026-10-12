package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapSpawnArgumentsTest {
    private final GameMap gym = map("Sports Gym");
    private final GameMap endingInTeam = map("Arena blue");
    private final List<GameMap> maps = List.of(gym, endingInTeam);

    @Test
    void preservesOldTpAndMapNamesEndingInTeam() {
        var selection = MapSpawnArguments.resolve(new String[]{"tp", "Arena", "blue"}, maps, false);
        assertEquals(List.of(endingInTeam), selection.maps());
        assertEquals(BattleTeam.RED, selection.team());
    }

    @Test
    void acceptsSpacedNamesAndUnderscoreAliasesWithTeam() {
        var spaced = MapSpawnArguments.resolve(new String[]{"tp", "Sports", "Gym", "BLUE"}, maps, false);
        var alias = MapSpawnArguments.resolve(new String[]{"setspawn", "Sports_Gym", "青"}, maps, true);
        assertEquals(List.of(gym), spaced.maps());
        assertEquals(BattleTeam.BLUE, spaced.team());
        assertEquals(spaced, alias);
    }

    @Test
    void neverSavesWithoutExplicitValidTeam() {
        assertThrows(IllegalArgumentException.class,
                () -> MapSpawnArguments.resolve(new String[]{"setspawn", "Sports_Gym"}, maps, true));
        assertThrows(IllegalArgumentException.class,
                () -> MapSpawnArguments.resolve(new String[]{"setspawn", "Sports_Gym", "green"}, maps, true));
    }

    @Test
    void exactNameWinsOverUnderscoreAlias() {
        GameMap underscore = map("Sports_Gym");
        assertEquals(List.of(underscore), MapSpawnArguments.resolve(
                new String[]{"tp", "Sports_Gym", "blue"}, List.of(gym, underscore), false).maps());
    }

    @Test
    void completesTeamAfterSpacedOrUnderscoreMapName() {
        // The map picker must not multiply its entries with red/blue suffixes.
        assertEquals(List.of("Arena blue", "Sports Gym"),
                MapSpawnArguments.complete(new String[]{"tp", ""}, maps, false));
        assertEquals(List.of("Sports Gym"),
                MapSpawnArguments.complete(new String[]{"setspawn", "Sports"}, maps, true));
        assertEquals(List.of("Gym"),
                MapSpawnArguments.complete(new String[]{"setspawn", "Sports", ""}, maps, true));
        assertEquals(List.of("blue", "red"),
                MapSpawnArguments.complete(new String[]{"tp", "Sports", "Gym", ""}, maps, false));
        assertEquals(List.of("blue"), MapSpawnArguments.complete(new String[]{"tp", "Sports", "Gym", "b"}, maps, false));
        assertEquals(List.of("blue", "red"),
                MapSpawnArguments.complete(new String[]{"setspawn", "Sports_Gym", ""}, maps, true));
    }

    @Test
    void acceptsNewMapNameAndCompletesTeamsBeforeRegistration() {
        var selection = MapSpawnArguments.resolve(new String[]{"setspawn", "New", "Arena", "red"}, maps, true);
        assertTrue(selection.maps().isEmpty());
        assertEquals("New Arena", selection.mapName());
        assertEquals(BattleTeam.RED, selection.team());
        assertEquals(List.of("blue", "red"),
                MapSpawnArguments.complete(new String[]{"setspawn", "New", "Arena", ""}, maps, true));
        assertTrue(MapSpawnArguments.complete(new String[]{"tp", "New", "Arena", ""}, maps, false).isEmpty());
    }

    private static GameMap map(String name) {
        return new GameMap(name, null, new HashMap<>(), List.of());
    }
}

package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/** Map names may contain spaces; the optional team is always the last argument. */
final class MapSpawnArguments {
    private MapSpawnArguments() {
    }

    record Selection(List<GameMap> maps, BattleTeam team, String mapName) {
    }

    static Selection resolve(String[] args, List<GameMap> maps, boolean requireTeam) {
        if (args.length < 2) {
            throw new IllegalArgumentException("マップ名を指定してください。");
        }
        // Preserve old tp commands, including map names ending in 'red' or 'blue'.
        List<GameMap> fullName = find(maps, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        if (!requireTeam && !fullName.isEmpty()) {
            return new Selection(fullName, BattleTeam.RED, fullName.get(0).getMapName());
        }
        BattleTeam team = team(args[args.length - 1]);
        if (args.length < 3 || team == null) {
            if (!requireTeam) {
                return new Selection(fullName, BattleTeam.RED,
                        String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
            }
            throw new IllegalArgumentException("マップ名の後に red（赤）または blue（青）を指定してください。");
        }
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)).trim();
        List<GameMap> matches = find(maps, name);
        return new Selection(matches, team, matches.isEmpty() ? name : matches.get(0).getMapName());
    }

    private static BattleTeam team(String value) {
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "red", "赤" -> BattleTeam.RED;
            case "blue", "青" -> BattleTeam.BLUE;
            default -> null;
        };
    }

    private static List<GameMap> find(List<GameMap> maps, String name) {
        // Exact names take precedence over space/underscore aliases.
        List<GameMap> exact = maps.stream().filter(map -> map.getMapName().equalsIgnoreCase(name)).toList();
        if (!exact.isEmpty()) {
            return exact;
        }
        return maps.stream().filter(map -> map.getMapName().replace('_', ' ')
                .equalsIgnoreCase(name.replace('_', ' '))).toList();
    }

    static List<String> complete(String[] args, List<GameMap> maps, boolean allowNewMap) {
        String entered = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        String before = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1));
        String prefix = before.isEmpty() ? "" : before + " ";
        List<String> mapSuggestions = maps.stream()
                .map(map -> entered.contains("_") ? map.getMapName().replace(' ', '_') : map.getMapName())
                .filter(name -> name.replace('_', ' ').toLowerCase(Locale.ROOT)
                        .startsWith(entered.replace('_', ' ').toLowerCase(Locale.ROOT)))
                .map(value -> value.substring(prefix.length()))
                .filter(value -> !value.isEmpty()).distinct().sorted().toList();

        // Only offer teams after a complete map name and the following space.
        // For new names, first allow any matching existing map name to be completed.
        if (!before.isBlank() && (!find(maps, before).isEmpty() || (allowNewMap && mapSuggestions.isEmpty()))) {
            String current = args[args.length - 1].toLowerCase(Locale.ROOT);
            return Stream.of("red", "blue").filter(team -> team.startsWith(current)).sorted().toList();
        }
        return mapSuggestions;
    }
}

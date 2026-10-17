package net.azisaba.lgw.core.hijack;

import org.bukkit.configuration.file.YamlConfiguration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

/** Immutable, validated rules. A running match retains its own instance. */
public record HijackRules(Map<String, Object> values) {
    public enum Victory { POINTS_OR_TIME, POINTS, TIME }
    public enum Target { ALL, CONTROLLER, CAPTURING }
    public enum Tie { DRAW, SHARED_WIN }
    public enum VictoryItem { NONE, TIME, KILLS, TIME_OR_KILLS, TIME_AND_KILLS }

    // Compatibility only: old files must load without reintroducing removed geometry controls.
    static final Set<String> RETIRED_CAPTURE_KEYS = Set.of(
            "capture.expand-enabled", "capture.minimum-width", "capture.jump-height");

    private static final Map<String, Object> DEFAULTS;
    static {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("victory.mode", "points_or_time");
        defaults.put("victory.target-points", 200);
        defaults.put("victory.time-limit-seconds", 600);
        defaults.put("victory.tie", "draw");
        defaults.put("victory.player-count.enabled", false);
        defaults.put("victory.player-count.targets", List.of("1:100", "6:150", "12:200"));
        defaults.put("victory-item.enabled", true);
        defaults.put("victory-item.mode", "time_or_kills");
        defaults.put("victory-item.minimum-participation-seconds", 360);
        defaults.put("victory-item.minimum-kills", 20);
        defaults.put("resistance.enabled", true);
        defaults.put("resistance.percent", 15.0);
        defaults.put("resistance.target", "all");
        defaults.put("explosion-resistance.enabled", false);
        defaults.put("explosion-resistance.percent", 50.0);
        defaults.put("explosion-resistance.radius", 0.0);
        defaults.put("explosion-resistance.target", "all");
        defaults.put("weapons.block-grenades", true);
        defaults.put("weapons.block-c4", true);
        defaults.put("weapons.blocked-ids", List.of());
        defaults.put("rewards.area-kill.enabled", false);
        defaults.put("rewards.area-kill.multiplier", 2.0);
        defaults.put("rewards.victory.enabled", false);
        defaults.put("rewards.victory.multiplier", 1.1);
        DEFAULTS = Collections.unmodifiableMap(defaults);
    }

    public HijackRules {
        // Only validated, complete maps may become a match snapshot.
        Map<String, Object> copy = new LinkedHashMap<>();
        for (var entry : DEFAULTS.entrySet()) {
            String key = entry.getKey();
            Object value = values.getOrDefault(key, entry.getValue());
            if (entry.getValue() instanceof Boolean && !(value instanceof Boolean)) fail(key);
            if (entry.getValue() instanceof Number) {
                if (!(value instanceof Number)) fail(key);
                double number = ((Number) value).doubleValue();
                if (!Double.isFinite(number)) fail(key);
                if (entry.getValue() instanceof Integer) {
                    int minimum = key.startsWith("victory-item.") ? 0 : 1;
                    if (number != Math.rint(number) || number < minimum || number > (key.endsWith("seconds") ? 86399 : 1000000)) fail(key);
                    value = (int) number;
                } else {
                    double max = key.endsWith("percent") ? 100 : key.contains("multiplier") ? 100 : 64;
                    if (number < 0 || number > max) fail(key);
                    value = number;
                }
            }
            if (entry.getValue() instanceof String) {
                if (!(value instanceof String)) fail(key);
                value = ((String) value).toLowerCase(Locale.ROOT);
                switch (key) {
                    case "victory.mode" -> Victory.valueOf(((String) value).toUpperCase(Locale.ROOT));
                    case "victory.tie" -> Tie.valueOf(((String) value).toUpperCase(Locale.ROOT));
                    case "victory-item.mode" -> VictoryItem.valueOf(((String) value).toUpperCase(Locale.ROOT));
                    default -> Target.valueOf(((String) value).toUpperCase(Locale.ROOT));
                }
            }
            if (entry.getValue() instanceof List<?>) {
                if (!(value instanceof List<?> list) || list.stream().anyMatch(item -> !(item instanceof String text) || text.isBlank())) fail(key);
                value = List.copyOf((List<?>) value);
                if (key.equals("victory.player-count.targets")) {
                    if (((List<?>) value).isEmpty()) fail(key);
                    var minimums = new HashSet<Integer>();
                    for (Object item : (List<?>) value) {
                        int[] band = parseBand(item.toString());
                        if (!minimums.add(band[0])) throw new IllegalArgumentException("人数帯の下限が重複しています: " + band[0]);
                    }
                }
            }
            if (key.startsWith("rewards.") && key.endsWith("enabled") && Boolean.TRUE.equals(value)) {
                throw new IllegalArgumentException("報酬連携は未実装です。" + key + " は false にしてください。");
            }
            copy.put(key, value);
        }
        for (String key : values.keySet()) if (!DEFAULTS.containsKey(key)) throw new IllegalArgumentException("不明な設定: " + key);
        values = Collections.unmodifiableMap(copy);
    }

    private static void fail(String key) { throw new IllegalArgumentException("設定値が不正です: " + key); }
    public static HijackRules defaults() { return new HijackRules(DEFAULTS); }
    public static HijackRules read(YamlConfiguration yaml) {
        Map<String, Object> flat = new LinkedHashMap<>();
        yaml.getValues(true).forEach((key, value) -> {
            if (!(value instanceof org.bukkit.configuration.ConfigurationSection) && !RETIRED_CAPTURE_KEYS.contains(key)) flat.put(key, value);
        });
        return new HijackRules(flat);
    }
    public static Map<String, Object> defaultsMap() { return DEFAULTS; }
    public boolean flag(String key) { return (Boolean) values.get(key); }
    public double number(String key) { return ((Number) values.get(key)).doubleValue(); }
    public int points() { return ((Number) values.get("victory.target-points")).intValue(); }
    private static int[] parseBand(String text) {
        String[] parts = text.split(":", -1);
        try {
            if (parts.length == 2) {
                int minimum = Integer.parseInt(parts[0].trim());
                int points = Integer.parseInt(parts[1].trim());
                if (minimum >= 1 && minimum <= 1000000 && points >= 1 && points <= 1000000) return new int[]{minimum, points};
            }
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("人数帯は 下限人数:目標点（各1～1000000）で指定してください: " + text);
    }
    public HijackRules forPlayerCount(int count) {
        if (count < 0) throw new IllegalArgumentException("参加人数は0以上にしてください。");
        if (!flag("victory.player-count.enabled")) return this;
        int minimum = 0;
        int selected = points();
        for (Object item : (List<?>) values.get("victory.player-count.targets")) {
            int[] band = parseBand(item.toString());
            if (band[0] <= count && band[0] > minimum) { minimum = band[0]; selected = band[1]; }
        }
        var resolved = new LinkedHashMap<>(values);
        resolved.put("victory.target-points", selected);
        return new HijackRules(resolved);
    }
    public VictoryItem victoryItem() { return VictoryItem.valueOf(values.get("victory-item.mode").toString().toUpperCase(Locale.ROOT)); }
    public boolean qualifiesForVictoryItem(long now, Long joinedAt, int kills) {
        if (!flag("victory-item.enabled")) return true;
        boolean enoughTime = joinedAt != null && now >= joinedAt
                && now - joinedAt >= (long) number("victory-item.minimum-participation-seconds") * 1000L;
        boolean enoughKills = kills >= number("victory-item.minimum-kills");
        return switch (victoryItem()) {
            case NONE -> true;
            case TIME -> enoughTime;
            case KILLS -> enoughKills;
            case TIME_OR_KILLS -> enoughTime || enoughKills;
            case TIME_AND_KILLS -> enoughTime && enoughKills;
        };
    }
    public String victoryItemRequirement() {
        if (!flag("victory-item.enabled")) return "勝利チーム全員（条件判定OFF）";
        String time = ((int) number("victory-item.minimum-participation-seconds")) + "秒以上参加";
        String kills = ((int) number("victory-item.minimum-kills")) + "キル以上";
        return switch (victoryItem()) {
            case NONE -> "勝利チーム全員（時間・キル条件なし）";
            case TIME -> time;
            case KILLS -> kills;
            case TIME_OR_KILLS -> time + " または " + kills;
            case TIME_AND_KILLS -> time + " かつ " + kills;
        };
    }
    public int seconds() { return ((Number) values.get("victory.time-limit-seconds")).intValue(); }
    public Victory victory() { return Victory.valueOf(values.get("victory.mode").toString().toUpperCase(Locale.ROOT)); }
    public boolean hasTimeLimit() { return victory() != Victory.POINTS; }
    public boolean hasPointLimit() { return victory() != Victory.TIME; }
    public boolean drawOnTie() { return values.get("victory.tie").equals("draw"); }
    public Target target(String prefix) { return Target.valueOf(values.get(prefix + ".target").toString().toUpperCase(Locale.ROOT)); }
    @SuppressWarnings("unchecked")
    public List<String> blockedIds() { return (List<String>) values.get("weapons.blocked-ids"); }
    public String description() {
        return (hasPointLimit() ? points() + "点先取" : "時間終了時の最多ポイントで勝利")
                + (hasTimeLimit() ? " / 上限" + seconds() + "秒" : " / 時間制限なし")
                + (drawOnTie() ? " / 同点は引き分け" : " / 同点は両チーム勝利");
    }
}

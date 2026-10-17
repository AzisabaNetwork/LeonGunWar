package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.HijackObjectiveEvent;
import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** All methods are used on the server thread. Match rules/geometry are frozen at start. */
public final class HijackController {
    public record AreaView(UUID worldId, String mapName, List<Area3D> areas) {
        public AreaView { areas = List.copyOf(areas); }
    }
    private final LeonGunWar plugin;
    private final HijackConfig config;
    private HijackRules active;
    private UUID matchId;
    private String mapName;
    private UUID worldId;
    private List<Area3D> areas = List.of();
    private BattleTeam controller;
    private boolean resultPublished;
    private int startingPlayerCount;
    private boolean playerCountFrozen;
    private final Set<UUID> explosions = new HashSet<>();

    public HijackController(LeonGunWar plugin, HijackConfig config) { this.plugin = plugin; this.config = config; }
    public HijackRules getActive() { return active; }
    public HijackConfig getConfig() { return config; }
    public BattleTeam getController() { return controller; }
    public int getStartingPlayerCount() { return startingPlayerCount; }
    public void setController(BattleTeam team) { controller = team; }
    public boolean isRunning() {
        return active != null && plugin.getManager().isMatching() && plugin.getManager().getMatchMode() == MatchMode.HIJACK;
    }

    public void begin(GameMap map) {
        active = config.getPending();
        matchId = UUID.randomUUID();
        mapName = map.getMapName();
        worldId = map.getWorld().getUID();
        areas = snapshotAreas(map);
        controller = null;
        resultPublished = false;
        startingPlayerCount = 0;
        playerCountFrozen = false;
    }
    /** Called once after team assignment, before the start announcement or first scoring tick. */
    public void freezeStartingPlayers(int count) {
        if (active == null || playerCountFrozen) throw new IllegalStateException("開始人数は試合開始時に1回だけ確定できます。");
        active = active.forPlayerCount(count);
        startingPlayerCount = count;
        playerCountFrozen = true;
    }
    public void clear() { active = null; areas = List.of(); controller = null; }

    private static List<Area3D> snapshotAreas(GameMap map) {
        return map.getHijackAreas().stream().map(area -> new Area3D(area.getMin(), area.getMax())).toList();
    }
    public AreaView activeAreaView() {
        return isRunning() ? new AreaView(worldId, mapName, areas) : null;
    }
    public AreaView nextAreaView(GameMap map) {
        if (map.getWorld() == null) throw new IllegalArgumentException("マップのワールドが読み込まれていません。");
        return new AreaView(map.getWorld().getUID(), map.getMapName(), snapshotAreas(map));
    }

    public boolean isParticipant(Player player) {
        return isMember(player) && !player.isDead() && player.getGameMode() != GameMode.SPECTATOR;
    }
    private boolean isMember(Player player) {
        return isRunning() && player.isOnline() && player.getWorld().getUID().equals(worldId)
                && plugin.getManager().getBattleTeam(player) != null;
    }
    public boolean isInside(Player player) { return isParticipant(player) && contains(player); }
    private boolean contains(Player player) {
        return player.getWorld().getUID().equals(worldId)
                && areas.stream().anyMatch(area -> area.isInArea(player.getLocation().toVector()));
    }
    public Map<UUID, BattleTeam> occupants() {
        Map<UUID, BattleTeam> result = new HashMap<>();
        for (Player player : plugin.getManager().getAllTeamPlayers()) {
            if (isInside(player)) result.put(player.getUniqueId(), plugin.getManager().getBattleTeam(player));
        }
        return Map.copyOf(result);
    }

    /** Current scoring eligibility, recomputed at the hit/tick rather than retaining the last scorer. */
    public BattleTeam getCapturingTeam() {
        Set<BattleTeam> present = new HashSet<>(occupants().values());
        return present.size() == 1 ? present.iterator().next() : null;
    }

    public double reduction(Player player, boolean explosion) {
        if (!isParticipant(player)) return 0;
        double reduction = 0;
        if (active.flag("resistance.enabled") && contains(player) && eligible(player, "resistance")) {
            reduction = active.number("resistance.percent");
        }
        if (explosion && active.flag("explosion-resistance.enabled") && eligible(player, "explosion-resistance")) {
            double radius = active.number("explosion-resistance.radius");
            if (areas.stream().anyMatch(area -> area.distanceWithinHeight(player.getLocation().toVector()) <= radius)) {
                reduction = Math.max(reduction, active.number("explosion-resistance.percent"));
            }
        }
        return reduction;
    }
    private boolean eligible(Player player, String prefix) {
        BattleTeam team = plugin.getManager().getBattleTeam(player);
        return switch (active.target(prefix)) {
            case ALL -> true;
            case CONTROLLER -> controller != null && controller == team;
            case CAPTURING -> team == getCapturingTeam();
        };
    }

    /** Preserve explosion provenance across LGW's synchronous synthetic damage call. */
    public void withExplosion(Entity target, Runnable damage) {
        boolean added = explosions.add(target.getUniqueId());
        try { damage.run(); }
        finally { if (added) explosions.remove(target.getUniqueId()); }
    }
    public boolean isExplosion(Entity target) { return explosions.contains(target.getUniqueId()); }

    private HijackObjectiveEvent.Context context() {
        Map<UUID, BattleTeam> participants = new HashMap<>();
        MatchManager manager = plugin.getManager();
        manager.getTeamPlayers().forEach((team, players) -> players.forEach(player -> participants.put(player.getUniqueId(), team)));
        return new HijackObjectiveEvent.Context(matchId, mapName, worldId, active, participants,
                isRunning() ? occupants().keySet() : Set.of(), controller);
    }
    public void publishCapture(BattleTeam team, boolean bothTeamsPresent) {
        publish(new HijackObjectiveEvent.Capture(team, team == null ? 0 : 1, bothTeamsPresent));
    }
    public void publishKill(Player killer, Player victim) {
        // A delayed projectile can legitimately kill after its shooter has died.
        if (!isMember(killer) || killer.getGameMode() == GameMode.SPECTATOR || killer == victim
                || !victim.getWorld().getUID().equals(worldId)) return;
        BattleTeam victimTeam = plugin.getManager().getBattleTeam(victim);
        if (victimTeam == null || victimTeam == plugin.getManager().getBattleTeam(killer)) return;
        publish(new HijackObjectiveEvent.Kill(killer.getUniqueId(), victim.getUniqueId(), contains(killer), contains(victim)));
    }
    public void publishResult(List<BattleTeam> winners) {
        if (active == null || resultPublished) return;
        resultPublished = true;
        Map<BattleTeam, Integer> points = new HashMap<>();
        for (BattleTeam team : BattleTeam.values()) points.put(team, plugin.getManager().getCurrentTeamPoint(team));
        publish(new HijackObjectiveEvent.Finished(winners, points));
    }
    private void publish(HijackObjectiveEvent.Detail detail) {
        plugin.getServer().getPluginManager().callEvent(new HijackObjectiveEvent(context(), detail));
    }
}

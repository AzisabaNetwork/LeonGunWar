package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.HijackObjectiveEvent;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.listeners.modes.HijackListener;
import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.GameMap;
import net.azisaba.lgw.core.util.MatchMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HijackGameplayTest {
    @TempDir Path directory;
    LeonGunWar plugin;
    MatchManager manager;
    HijackConfig config;
    HijackController controller;
    GameMap map;
    World world;
    Player red, blue;
    final List<Player> players = new ArrayList<>();
    final Map<BattleTeam, Integer> points = new EnumMap<>(BattleTeam.class);
    final List<Event> events = new ArrayList<>();

    @BeforeEach void setup() throws Exception {
        plugin = mock(LeonGunWar.class);
        manager = mock(MatchManager.class);
        Server server = mock(Server.class);
        PluginManager pm = mock(PluginManager.class);
        when(plugin.getManager()).thenReturn(manager);
        when(plugin.getServer()).thenReturn(server);
        when(server.getPluginManager()).thenReturn(pm);
        when(manager.isMatching()).thenReturn(true);
        when(manager.getMatchMode()).thenReturn(MatchMode.HIJACK);
        world = mock(World.class);
        when(world.getUID()).thenReturn(UUID.randomUUID());
        map = new GameMap("objective", world, Map.of(), new ArrayList<>(List.of(new Area3D(new Vector(-1, 64, -1), new Vector(1, 66, 1)))));
        config = new HijackConfig(directory.resolve("hijack.yml"));
        controller = new HijackController(plugin, config);
        when(plugin.getHijackController()).thenReturn(controller);
        when(manager.getCurrentGameMap()).thenReturn(map);
        when(manager.getAllTeamPlayers()).thenReturn(players);
        when(manager.getCurrentTeamPoint(any(BattleTeam.class))).thenAnswer(call -> points.getOrDefault(call.getArgument(0), 0));
        doAnswer(call -> { BattleTeam team = call.getArgument(0); points.merge(team, 1, Integer::sum); return null; }).when(manager).addTeamPoint(any());
        doAnswer(call -> {
            Event event = call.getArgument(0);
            events.add(event);
            if (event instanceof MatchFinishedEvent finish) {
                new HijackCombatListener(controller).onFinish(finish);
                when(manager.isMatching()).thenReturn(false);
            }
            return null;
        }).when(pm).callEvent(any());
        red = player(BattleTeam.RED, 0, 64, 0);
        blue = player(BattleTeam.BLUE, 20, 64, 0);
        when(manager.getTeamPlayers()).thenReturn(Map.of(BattleTeam.RED, List.of(red), BattleTeam.BLUE, List.of(blue)));
        controller.begin(map);
    }
    Player player(BattleTeam team, double x, double y, double z) {
        Player p = mock(Player.class);
        when(p.getUniqueId()).thenReturn(UUID.randomUUID());
        when(p.isOnline()).thenReturn(true);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(p.getWorld()).thenReturn(world);
        when(p.getLocation()).thenReturn(new Location(world, x, y, z));
        when(manager.getBattleTeam(p)).thenReturn(team);
        players.add(p);
        return p;
    }
    void move(Player p, double x, double y, double z) { when(p.getLocation()).thenReturn(new Location(world, x, y, z)); }
    void set(String key, String value) throws Exception { config.set(key, value); controller.begin(map); }
    void tick(int seconds) { new HijackListener(plugin).onTime(new MatchTimeChangedEvent(seconds)); }
    List<MatchFinishedEvent> finishes() { return events.stream().filter(e -> e instanceof MatchFinishedEvent).map(e -> (MatchFinishedEvent) e).toList(); }

    @Test void mapsBoundsControlEveryAxisAndRetiredCaptureSettingsCannotOverrideThem() throws Exception {
        move(red, 1, 65.3, -1); assertTrue(controller.isInside(red));
        move(red, 1.01, 65.3, 0); assertFalse(controller.isInside(red));
        move(red, 4, 64, 0); assertFalse(controller.isInside(red));
        java.nio.file.Files.writeString(config.getPath(), "capture:\n  expand-enabled: true\n  minimum-width: 64\n  jump-height: 0\n");
        config.reload(); controller.begin(map);
        assertFalse(controller.isInside(red));
        move(red, 0, 65.3, 0); assertTrue(controller.isInside(red));
        move(red, 0, 66.01, 0); assertFalse(controller.isInside(red));
        move(red, 0, 64, 0); assertTrue(controller.isInside(red));
        assertEquals(66, map.getHijackAreas().get(0).getMax().getY());
    }

    @Test void jumpsAndTenByTenAreaCountButOtherFloorsWorldsDeadAndSpectatorsDoNot() throws Exception {
        map.getHijackAreas().set(0, new Area3D(new Vector(-5, 64, -5), new Vector(5, 66, 5)));
        controller.begin(map);
        move(red, 4.99, 65.3, -4.99);
        assertTrue(controller.isInside(red));
        move(red, 5.01, 65.3, 0); assertFalse(controller.isInside(red));
        move(red, 0, 67, 0); assertFalse(controller.isInside(red));
        move(red, 0, 63, 0); assertFalse(controller.isInside(red));
        move(red, 0, 64, 0);
        when(red.isDead()).thenReturn(true); assertFalse(controller.isInside(red));
        when(red.isDead()).thenReturn(false);
        when(red.getGameMode()).thenReturn(GameMode.SPECTATOR); assertFalse(controller.isInside(red));
        when(red.getGameMode()).thenReturn(GameMode.SURVIVAL);
        World other = mock(World.class); when(other.getUID()).thenReturn(UUID.randomUUID());
        when(red.getWorld()).thenReturn(other); assertFalse(controller.isInside(red));
    }

    @Test void suppliedShaggerYamlAwardsPointsThroughoutAirbornePositionsUsingMapsBoundsAlone() throws Exception {
        java.nio.file.Files.writeString(directory.resolve("maps.yml"), """
                Shagger:
                  world: maps
                  spawns:
                    red: { world: maps, x: -29.5, y: 47.0, z: -1771.5, yaw: 0, pitch: 0.0 }
                    blue: { world: maps, x: -28.5, y: 47.0, z: -1704.5, yaw: 180, pitch: 0.0 }
                  areas:
                    1:
                      min: { x: -9.0, y: 48.0, z: -1741.0 }
                      max: { x: -14.0, y: 52.0, z: -1736.0 }
                """);
        when(plugin.getDataFolder()).thenReturn(directory.toFile());
        when(plugin.getServer().getWorld("maps")).thenReturn(world);
        var maps = new net.azisaba.lgw.core.configs.MapsConfig(plugin);
        maps.loadConfig();
        map = maps.getAllGameMap().get(0);
        controller.begin(map);
        when(red.isOnGround()).thenReturn(false);
        int expected = 0;
        for (double y : new double[]{48, 49.3, 50, 50.25, 51.99, 52}) {
            move(red, -11, y, -1738); assertTrue(controller.isInside(red));
            tick(599 - expected); assertEquals(++expected, points.get(BattleTeam.RED));
        }
        move(red, -11, 52.01, -1738); tick(590); assertEquals(expected, points.get(BattleTeam.RED));
        move(red, -11, 47.99, -1738); tick(589); assertEquals(expected, points.get(BattleTeam.RED));
        move(red, -11, 50.25, -1738); move(blue, -11, 49, -1738);
        tick(588); assertEquals(expected, points.get(BattleTeam.RED));
        assertEquals(52, map.getHijackAreas().get(0).getMax().getY());
    }

    @Test void previewUsesReloadedMapsWhileTheRunningMatchKeepsItsOriginalBounds() throws Exception {
        var active = controller.activeAreaView();
        map.getHijackAreas().set(0, new Area3D(new Vector(-5, 64, -5), new Vector(5, 70, 5)));
        var next = controller.nextAreaView(map);
        assertEquals(66, active.areas().get(0).getMax().getY());
        assertEquals(70, next.areas().get(0).getMax().getY());
        assertEquals(10, next.areas().get(0).getMax().getX() - next.areas().get(0).getMin().getX());
        assertEquals(active, controller.activeAreaView());
        assertEquals(70, map.getHijackAreas().get(0).getMax().getY());
        when(manager.isMatching()).thenReturn(false); assertNull(controller.activeAreaView());
    }

    @Test void airborneFeetInsideMapsAreNoLongerRejectedByTheRemovedHeightOverride() {
        map = new GameMap("Shagger", world, Map.of(), List.of(
                new Area3D(new Vector(-9, 48, -1741), new Vector(-14, 52, -1736))));
        controller.begin(map);
        move(red, -11, 50.25, -1738);
        assertTrue(map.getHijackAreas().get(0).isInArea(red.getLocation().toVector()));
        assertEquals(52, controller.activeAreaView().areas().get(0).getMax().getY());
        assertTrue(controller.isInside(red));
        tick(599);
        assertEquals(1, points.getOrDefault(BattleTeam.RED, 0));
    }

    @Test void uncontestedScoresOnceRegardlessOfNumbersAndContestStopsScoring() {
        player(BattleTeam.RED, 1, 64, 0);
        tick(599); assertEquals(1, points.get(BattleTeam.RED));
        move(blue, 0, 65, 0);
        tick(598); assertEquals(1, points.get(BattleTeam.RED));
        var detail = (HijackObjectiveEvent.Capture) ((HijackObjectiveEvent) events.get(events.size() - 1)).getDetail();
        assertNull(detail.scoringTeam()); assertEquals(0, detail.pointsAwarded()); assertTrue(detail.bothTeamsPresent());
    }

    @Test void rulesAndGeometryAreFrozenUntilNextMatch() throws Exception {
        map.getHijackAreas().set(0, new Area3D(new Vector(-5, 64, -5), new Vector(5, 66, 5)));
        controller.begin(map);
        config.set("resistance.percent", "70");
        map.getHijackAreas().set(0, new Area3D(new Vector(-1, 64, -1), new Vector(1, 66, 1)));
        move(red, 4, 64, 0);
        assertTrue(controller.isInside(red)); assertEquals(15, controller.reduction(red, false));
        controller.begin(map);
        assertFalse(controller.isInside(red));
        move(red, 0, 64, 0); assertEquals(70, controller.reduction(red, false));
    }

    @Test void startingPlayerCountFixesTargetAndCannotChangeAfterJoinOrLeave() throws Exception {
        config.set("victory.player-count.enabled", "true");
        controller.begin(map);
        controller.freezeStartingPlayers(5);
        assertEquals(5, controller.getStartingPlayerCount());
        assertEquals(100, controller.getActive().points());
        players.clear(); // A disconnect cannot lower a running match's threshold.
        config.set("victory.player-count.targets", "1:40,6:80");
        assertEquals(100, controller.getActive().points());
        assertThrows(IllegalStateException.class, () -> controller.freezeStartingPlayers(2));
        controller.begin(map);
        controller.freezeStartingPlayers(6);
        assertEquals(80, controller.getActive().points());
    }
    @Test void populationBasedTargetActuallyEndsTheMatchAtItsResolvedThreshold() throws Exception {
        config.set("victory.player-count.enabled", "true");
        controller.begin(map); controller.freezeStartingPlayers(5);
        points.put(BattleTeam.RED, 99);
        tick(599);
        assertEquals(1, finishes().size());
        assertEquals(List.of(BattleTeam.RED), finishes().get(0).getWinners());
    }

    @Test void bothSidesGetResistanceAndControllerOnlyPersistsThroughContest() throws Exception {
        move(blue, 0, 64, 0);
        assertEquals(15, controller.reduction(red, false)); assertEquals(15, controller.reduction(blue, false));
        set("resistance.target", "controller");
        assertEquals(0, controller.reduction(red, false));
        move(blue, 10, 64, 0); tick(599);
        move(blue, 0, 64, 0); tick(598);
        assertEquals(15, controller.reduction(red, false)); assertEquals(0, controller.reduction(blue, false));
        move(red, 10, 64, 0); tick(597);
        assertEquals(15, controller.reduction(blue, false));
    }

    @Test void explosionRadiusUsesAreaEdgeAndHeightAndHighestReductionOnly() throws Exception {
        map.getHijackAreas().set(0, new Area3D(new Vector(-5, 64, -5), new Vector(5, 66, 5)));
        config.set("explosion-resistance.enabled", "true");
        config.set("explosion-resistance.radius", "3");
        controller.begin(map);
        assertEquals(50, controller.reduction(red, true));
        assertEquals(15, controller.reduction(red, false));
        move(red, 8, 64, 0); assertEquals(50, controller.reduction(red, true));
        assertEquals(0, controller.reduction(red, false));
        move(red, 8.01, 64, 0); assertEquals(0, controller.reduction(red, true));
        move(red, 0, 67, 0); assertEquals(0, controller.reduction(red, true));
        config.set("resistance.percent", "75"); controller.begin(map);
        move(red, 0, 64, 0); assertEquals(75, controller.reduction(red, true));
    }

    @Test void capturingResistanceTracksTheCurrentUncontestedTeamBetweenScoringTicks() throws Exception {
        set("resistance.target", "capturing");
        assertEquals(15, controller.reduction(red, false));
        assertNull(controller.getController()); // Does not require a previous scoring tick.
        tick(599); assertEquals(BattleTeam.RED, controller.getController());
        move(blue, 0, 64, 0);
        assertEquals(0, controller.reduction(red, false));
        assertEquals(0, controller.reduction(blue, false));
        when(blue.isDead()).thenReturn(true);
        assertEquals(15, controller.reduction(red, false));
        when(blue.isDead()).thenReturn(false);
        when(blue.getGameMode()).thenReturn(GameMode.SPECTATOR);
        assertEquals(15, controller.reduction(red, false));
        when(blue.getGameMode()).thenReturn(GameMode.SURVIVAL);
        move(red, 10, 64, 0);
        assertEquals(15, controller.reduction(blue, false)); // Immediate takeover before next tick.
        assertEquals(BattleTeam.RED, controller.getController());
        assertEquals(0, controller.reduction(red, false));
        move(blue, 10, 64, 0);
        assertNull(controller.getCapturingTeam());
    }

    @Test void capturingExplosionProtectionCoversNearbyAlliesOnlyWhileTheirTeamIsCapturing() throws Exception {
        config.set("explosion-resistance.enabled", "true");
        config.set("explosion-resistance.radius", "3");
        config.set("explosion-resistance.target", "capturing");
        config.set("resistance.enabled", "false"); controller.begin(map);
        Player ally = player(BattleTeam.RED, 4, 64, 0);
        assertEquals(50, controller.reduction(ally, true));
        move(ally, 4.01, 64, 0); assertEquals(0, controller.reduction(ally, true));
        move(ally, 4, 64, 0);
        move(blue, 0, 64, 0); assertEquals(0, controller.reduction(ally, true));
        move(blue, 20, 64, 0); move(red, 10, 64, 0);
        assertEquals(0, controller.reduction(ally, true)); // No occupants, even with allies nearby.
    }

    @Test void projectileAndDirectGunDamageAreReducedOnceAndOnlyForEligibleParticipants() {
        var listener = new HijackCombatListener(controller);
        for (var cause : List.of(EntityDamageEvent.DamageCause.PROJECTILE, EntityDamageEvent.DamageCause.ENTITY_ATTACK)) {
            EntityDamageEvent event = mock(EntityDamageEvent.class);
            when(event.getEntity()).thenReturn(red);
            when(event.getDamage()).thenReturn(10.0);
            when(event.getCause()).thenReturn(cause);
            move(red, 0, 64, 0); listener.onDamage(event);
            verify(event).setDamage(8.5);
            clearInvocations(event);
            move(red, 2, 64, 0); listener.onDamage(event);
            verify(event).setDamage(10.0);
            clearInvocations(event);
            move(red, 0, 64, 0); when(manager.getMatchMode()).thenReturn(MatchMode.TEAM_DEATH_MATCH);
            listener.onDamage(event); verify(event).setDamage(10.0);
            when(manager.getMatchMode()).thenReturn(MatchMode.HIJACK);
        }
    }

    @Test void realDamageListenerReducesSyntheticExplosionExactlyOnceAndPreservesVoid() throws Exception {
        set("explosion-resistance.enabled", "true");
        var listener = new HijackCombatListener(controller);
        EntityDamageEvent event = mock(EntityDamageEvent.class);
        when(event.getEntity()).thenReturn(red);
        when(event.getDamage()).thenReturn(10.0);
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK);
        controller.withExplosion(red, () -> listener.onDamage(event));
        verify(event).setDamage(5.0);
        assertFalse(controller.isExplosion(red));
        assertThrows(RuntimeException.class, () -> controller.withExplosion(red, () -> { throw new RuntimeException(); }));
        assertFalse(controller.isExplosion(red));
        when(event.getCause()).thenReturn(EntityDamageEvent.DamageCause.VOID);
        clearInvocations(event); listener.onDamage(event); verify(event, never()).setDamage(anyDouble());
        when(manager.getMatchMode()).thenReturn(MatchMode.TEAM_DEATH_MATCH);
        assertEquals(0, controller.reduction(red, true));
    }

    @Test void pointThresholdWinsOnceEvenAtTimerBoundary() {
        points.put(BattleTeam.RED, 199);
        tick(0); tick(0);
        assertEquals(1, finishes().size()); assertEquals(List.of(BattleTeam.RED), finishes().get(0).getWinners());
    }
    @Test void genericFinishDetectorDoesNotRepeatAfterHijackFinalizesAtZero() {
        points.put(BattleTeam.RED, 199);
        tick(0);
        when(manager.getMatchMode()).thenReturn(null);
        try (var staticPlugin = mockStatic(LeonGunWar.class)) {
            staticPlugin.when(LeonGunWar::getPlugin).thenReturn(plugin);
            new net.azisaba.lgw.core.listeners.MatchControlListener().matchFinishDetector(new MatchTimeChangedEvent(0));
        }
        assertEquals(1, finishes().size());
    }

    @Test void unlimitedTimerKeepsPublishingTicksWithoutNegativeCountdown() {
        var remaining = new java.util.concurrent.atomic.AtomicInteger(-1);
        when(manager.getTimeLeft()).thenReturn(remaining);
        try (var staticPlugin = mockStatic(LeonGunWar.class); var bukkit = mockStatic(org.bukkit.Bukkit.class)) {
            staticPlugin.when(LeonGunWar::getPlugin).thenReturn(plugin);
            var pm = mock(PluginManager.class);
            bukkit.when(org.bukkit.Bukkit::getPluginManager).thenReturn(pm);
            var task = new net.azisaba.lgw.core.tasks.MatchCountdownTask();
            task.run(); task.run();
            var event = org.mockito.ArgumentCaptor.forClass(MatchTimeChangedEvent.class);
            verify(pm, times(2)).callEvent(event.capture());
            assertTrue(event.getAllValues().stream().allMatch(tick -> tick.getTimeLeft() == -1));
        }
        assertEquals(-1, remaining.get());
    }
    @Test void timeOnlyIgnoresPointThresholdAndDefaultTieIsDraw() throws Exception {
        set("victory.mode", "time");
        points.put(BattleTeam.RED, 200);
        tick(5); assertTrue(finishes().isEmpty());
        move(red, 20, 64, 0);
        points.put(BattleTeam.BLUE, 201);
        tick(0); assertTrue(finishes().get(0).getWinners().isEmpty());
    }
    @Test void pointsOnlyIgnoresTimerAndSharedWinIsOptional() throws Exception {
        set("victory.mode", "points"); tick(-1); assertTrue(finishes().isEmpty());
        config.set("victory.mode", "time"); config.set("victory.tie", "shared_win"); controller.begin(map);
        points.clear(); move(red, 20, 64, 0);
        tick(0); assertEquals(2, finishes().get(0).getWinners().size());
    }

    @Test void futureHooksSnapshotEnemyKillAndResultWithoutGrantingRewards() {
        controller.publishKill(red, blue);
        var kill = (HijackObjectiveEvent) events.get(0);
        assertTrue(((HijackObjectiveEvent.Kill) kill.getDetail()).killerInside());
        assertFalse(((HijackObjectiveEvent.Kill) kill.getDetail()).victimInside());
        assertEquals(2, kill.getContext().participants().size());
        assertThrows(UnsupportedOperationException.class, () -> kill.getContext().occupants().clear());
        controller.publishKill(red, red); assertEquals(1, events.size());
        controller.publishResult(List.of(BattleTeam.RED)); controller.publishResult(List.of(BattleTeam.BLUE));
        assertEquals(2, events.size());
        controller.clear(); assertFalse(controller.isRunning()); assertEquals(0, controller.reduction(red, true));
    }
    @Test void delayedEnemyKillStillNotifiesAfterShooterDeathButTeamKillDoesNot() {
        when(red.isDead()).thenReturn(true);
        controller.publishKill(red, blue);
        assertEquals(1, events.size());
        when(manager.getBattleTeam(blue)).thenReturn(BattleTeam.RED);
        controller.publishKill(red, blue);
        assertEquals(1, events.size());
    }
}

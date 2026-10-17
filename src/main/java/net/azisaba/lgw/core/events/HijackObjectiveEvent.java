package net.azisaba.lgw.core.events;

import net.azisaba.lgw.core.hijack.HijackRules;
import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Read-only extension point. No rewards are granted by LGW for these notifications. */
public final class HijackObjectiveEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    public record Context(UUID matchId, String mapName, UUID worldId, HijackRules rules,
                          Map<UUID, BattleTeam> participants, Set<UUID> occupants, BattleTeam controller) {
        public Context { participants = Map.copyOf(participants); occupants = Set.copyOf(occupants); }
    }
    public sealed interface Detail permits Capture, Kill, Finished { }
    /** pointsAwarded is 0 during contest/empty states. Presence alone is not proof of activity. */
    public record Capture(BattleTeam scoringTeam, int pointsAwarded, boolean bothTeamsPresent) implements Detail { }
    public record Kill(UUID killer, UUID victim, boolean killerInside, boolean victimInside) implements Detail { }
    public record Finished(List<BattleTeam> winners, Map<BattleTeam, Integer> points) implements Detail {
        public Finished { winners = List.copyOf(winners); points = Map.copyOf(points); }
    }
    private final Context context;
    private final Detail detail;
    public HijackObjectiveEvent(Context context, Detail detail) { this.context = context; this.detail = detail; }
    public Context getContext() { return context; }
    public Detail getDetail() { return detail; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}

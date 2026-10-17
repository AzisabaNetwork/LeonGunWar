package net.azisaba.lgw.core.listeners.modes;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.MatchTimeChangedEvent;
import net.azisaba.lgw.core.hijack.HijackController;
import net.azisaba.lgw.core.hijack.HijackRules;
import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

public class HijackListener implements Listener {
    private final LeonGunWar plugin;
    public HijackListener(LeonGunWar plugin) { this.plugin = plugin; }

    @EventHandler(priority = EventPriority.HIGH)
    public void onTime(MatchTimeChangedEvent event) {
        HijackController hijack = plugin.getHijackController();
        if (!hijack.isRunning()) return;
        MatchManager manager = plugin.getManager();
        HijackRules rules = hijack.getActive();
        BattleTeam scoringTeam = hijack.getCapturingTeam();
        if (scoringTeam != null) {
            hijack.setController(scoringTeam);
            manager.addTeamPoint(scoringTeam);
        }
        Set<BattleTeam> participants = new HashSet<>();
        manager.getTeamPlayers().forEach((team, players) -> {
            if (players.stream().anyMatch(player -> player.isOnline())) participants.add(team);
        });
        hijack.publishCapture(scoringTeam, participants.size() > 1);
        if (scoringTeam != null && rules.hasPointLimit() && manager.getCurrentTeamPoint(scoringTeam) >= rules.points()) {
            finish(List.of(scoringTeam));
        } else if (rules.hasTimeLimit() && event.getTimeLeft() <= 0) {
            int max = Arrays.stream(BattleTeam.values()).mapToInt(manager::getCurrentTeamPoint).max().orElse(0);
            List<BattleTeam> winners = Arrays.stream(BattleTeam.values())
                    .filter(team -> manager.getCurrentTeamPoint(team) == max).toList();
            finish(winners.size() > 1 && rules.drawOnTie() ? List.of() : winners);
        }
    }
    private void finish(List<BattleTeam> winners) {
        MatchManager manager = plugin.getManager();
        plugin.getServer().getPluginManager().callEvent(new MatchFinishedEvent(manager.getCurrentGameMap(), winners, manager.getTeamPlayers()));
    }
}

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
        if (!hijack.isRunning() || hijack.isFinishing()) return;
        MatchManager manager = plugin.getManager();
        HijackRules rules = hijack.getActive();
        // A lowered limit can end the match before another capture point is awarded.
        if (hijack.consumeLiveUpdate() && finishIfReached(rules, event.getTimeLeft())) return;
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
        if (!hijack.isRunning()) return;
        // Capture event subscribers may have changed settings synchronously.
        finishIfReached(hijack.getActive(), manager.getTimeLeft().get());
    }
    private boolean finishIfReached(HijackRules rules, int timeLeft) {
        MatchManager manager = plugin.getManager();
        int max = Arrays.stream(BattleTeam.values()).mapToInt(manager::getCurrentTeamPoint).max().orElse(0);
        if (!(rules.hasPointLimit() && max >= rules.points()) && !(rules.hasTimeLimit() && timeLeft <= 0)) return false;
        List<BattleTeam> winners = Arrays.stream(BattleTeam.values())
                .filter(team -> manager.getCurrentTeamPoint(team) == max).toList();
        finish(winners.size() > 1 && rules.drawOnTie() ? List.of() : winners);
        return true;
    }
    private void finish(List<BattleTeam> winners) {
        if (!plugin.getHijackController().requestFinish()) return;
        MatchManager manager = plugin.getManager();
        plugin.getServer().getPluginManager().callEvent(new MatchFinishedEvent(manager.getCurrentGameMap(), winners, manager.getTeamPlayers()));
    }
}

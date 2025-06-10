package net.azisaba.lgw.core.tasks;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.BattleTeam;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;

public class LeaderSelectionTask extends BukkitRunnable {
    private final BattleTeam team;
    private final Plugin plugin;

    public LeaderSelectionTask(BattleTeam team) {
        this.team = team;
    }



    @Override
    public void run() {
        LeonGunWar.getPlugin().getManager().setLeaderAtRandom(team);
    }
}
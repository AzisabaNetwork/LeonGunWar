package net.azisaba.lgw.core.battlesystem.gamemode.leaderdeathmatch.task;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.battlesystem.BattleTeam;
import org.bukkit.scheduler.BukkitRunnable;

public class LeaderSelectionTask extends BukkitRunnable {
    private final BattleTeam team;


    public LeaderSelectionTask(BattleTeam team) {
        this.team = team;
    }


    @Override
    public void run() {
        LeonGunWar.getPlugin().getManager().setLeaderAtRandom(team);
    }
}
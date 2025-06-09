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
    private final long delayTicks;

    public LeaderSelectionTask(BattleTeam team, Plugin plugin, long delayTicks) {
        this.team = team;
        this.plugin = plugin;
        this.delayTicks = delayTicks;
    }

    public static void scheduleOrExtend(BattleTeam team, Plugin plugin, long delayTicks) {
        // 既存のタスクがあればキャンセル
        LeaderSelectionTask existingTask = LeonGunWar.leaderSelectionTaskMap.get(team);
        if (existingTask != null && !existingTask.isCancelled()) {
            existingTask.cancel();
        }

        // 新しいタスクを作ってスケジュール
        LeaderSelectionTask newTask = new LeaderSelectionTask(team, plugin, delayTicks);
        LeonGunWar.leaderSelectionTaskMap.put(team, newTask);
        newTask.runTaskLater(plugin, delayTicks);
    }

    @Override
    public void run() {
        LeonGunWar.getPlugin().getManager().setLeaderAtRandom(team);
    }
}
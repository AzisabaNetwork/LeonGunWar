package net.azisaba.lgw.core.commands;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.util.ClockMachine;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class ToggleDoubleReward implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        LeonGunWar.doubleRewardEnable = !LeonGunWar.doubleRewardEnable;
        if(LeonGunWar.doubleRewardEnable){
            new ClockMachine().doubleRewardTaskStarter();
        }else {
            for(BukkitTask task : LeonGunWar.timeTaskList){
                task.cancel();
            }
            LeonGunWar.timeTaskList.clear();
        }
        sender.sendMessage("報酬２倍機能の有効無効化を切り替えました 現在:" + LeonGunWar.doubleRewardEnable);
        LeonGunWar.getPlugin().getConfig().set("DoubleRewardTaskEnable", LeonGunWar.doubleRewardEnable);
        return true;
    }
}

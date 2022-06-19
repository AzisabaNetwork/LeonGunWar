package net.azisaba.lgw.core.tasks;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.boss.BossBar;
import org.bukkit.scheduler.BukkitRunnable;

public class RemoveBossBarTask extends BukkitRunnable {

  @Override
  public void run() {
    BossBar progressBar = LeonGunWar.getPlugin().getManager().getBossBar();
    if (progressBar == null) {
      return;
    }

    // 全プレイヤーから削除
    progressBar.removeAll();
    // nullに設定
    LeonGunWar.getPlugin().getManager().setBossBar(null);
  }
}

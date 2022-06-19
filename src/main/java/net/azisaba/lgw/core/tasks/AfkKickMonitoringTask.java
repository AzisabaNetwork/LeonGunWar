package net.azisaba.lgw.core.tasks;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@RequiredArgsConstructor
public class AfkKickMonitoringTask extends BukkitRunnable {

  private final Map<Player, Long> lastMoved;

  @Override
  public void run() {
    Bukkit.getOnlinePlayers()
        .forEach(
            p -> {
              boolean matching = LeonGunWar.getPlugin().getManager().isPlayerMatching(p);
              boolean entrying = LeonGunWar.getPlugin().getManager().isEntryPlayer(p);

              // 試合もエントリーもしていないプレイヤーならreturn
              if (!matching && !entrying) {
                return;
              }

              // 試合はしていないがエントリーはしているプレイヤーもreturn
              if (!matching) {
                return;
              }

              // プレイヤーが最後に動いた秒数を取得
              long lastMovedMilliSecond = lastMoved.getOrDefault(p, 0L);

              // 30秒より少なければreturn
              if (lastMovedMilliSecond + 1000 * 60 > System.currentTimeMillis()) {
                return;
              }

              // 権限を持っていればreturn
              if (p.hasPermission("leongunwar.afkkick.exempt")) {
                return;
              }

              // 試合から退出 & エントリー解除
              LeonGunWar.getPlugin().getManager().removeEntryPlayer(p);
              LeonGunWar.getPlugin().getManager().kickPlayer(p);

              p.sendMessage(Chat.f("{0}&7放置と判定されたため試合から退出しました", LeonGunWar.GAME_PREFIX));

              LeonGunWar.getPlugin().getLogger().info(Chat.f("{0} を試合から退出させました", p.getName()));
            });
  }
}

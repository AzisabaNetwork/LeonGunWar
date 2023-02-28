package net.azisaba.lgw.core.util;

import com.google.common.util.concurrent.RateLimiter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.RespawnKillProtectionTask;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class RespawnProtection {


  private final RateLimiter protectedThrottle = RateLimiter.create(1);
  private final RateLimiter victimProtectedThrottle = RateLimiter.create(1);

  private final long duration = 5;

  private final Map<Player, Instant> remainTimes = new HashMap<>();
  private final Map<Player, BukkitTask> taskMap = new HashMap<>();
  private final Map<Player, BossBar> bossBars = new HashMap<>();

  private final List<Player> invincibleQueue = new ArrayList<>();

  public boolean isProtected(Player victim) {
    return invincibleQueue.contains(victim) ||
        Instant.now().isBefore(remainTimes.getOrDefault(victim, Instant.now()));
  }

  public void sendProtected(Player victim) {
    Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), () -> {
      if (protectedThrottle.tryAcquire()) {
        victim.sendMessage(Chat.f("{0}&rあなた &7は保護されています！", LeonGunWar.GAME_PREFIX));
      }
    });
  }

  public void sendVictimProtected(Player attacker, Player victim) {
    Bukkit.getScheduler().runTaskAsynchronously(LeonGunWar.getPlugin(), () -> {
      if (victimProtectedThrottle.tryAcquire()) {
        attacker.sendMessage(
            Chat.f("{0}{1} &7は保護されています！", LeonGunWar.GAME_PREFIX, victim.getDisplayName()));
      }
    });
  }

  public void startCountdown(Player player) {
    // リスポーン時間指定
    remainTimes.put(player, Instant.now().plusSeconds(duration));
    taskMap.compute(player, (a, task) -> {
      // タスク終了
      if (task != null) {
        task.cancel();
      }

      // ボスバー初期化
      bossBars.computeIfPresent(player, (b, bossBar) -> {
        bossBar.removePlayer(player);
        return null;
      });

      // タスク開始
      return new RespawnKillProtectionTask(player, remainTimes, duration, bossBars).runTaskTimer(
          LeonGunWar.getPlugin(), 0, 20);
    });
  }

  public void respawned(Player p) {
    if (!invincibleQueue.contains(p)) {
      invincibleQueue.add(p);
    }
  }

  public void detectedAction(Player p) {
    if (invincibleQueue.contains(p)) {
      startCountdown(p);
      invincibleQueue.remove(p);
    }
  }

  public void terminate(Player p) {
    invincibleQueue.remove(p);
  }
}

package net.azisaba.lgw.core.listeners.others;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.PlayerAssistEvent;
import net.azisaba.lgw.core.util.MatchMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class StreaksListener implements Listener {

  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerDeath(PlayerDeathEvent e) {
    Player deader = e.getEntity();
    Player killer = deader.getKiller();

    // 自滅は無視
    if (killer == null || killer == deader) {
      return;
    }

    // 試合中ではない場合は無視
    if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(killer)) {
      return;
    }

    // カスタムマッチは無視
    if (LeonGunWar.getPlugin().getManager().getMatchMode() == MatchMode.CUSTOM_DEATH_MATCH) {
      return;
    }

    // カウントを追加
    LeonGunWar.getPlugin().getKillStreaks().add(killer);
  }

  @EventHandler
  public void onPlayerAssist(PlayerAssistEvent e) {
    Player p = e.getPlayer();

    // 試合中ではない場合は無視
    if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(p)) {
      return;
    }

    // カスタムマッチは無視
    if (LeonGunWar.getPlugin().getManager().getMatchMode() == MatchMode.CUSTOM_DEATH_MATCH) {
      return;
    }

    // カウントを追加
    LeonGunWar.getPlugin().getAssistStreaks().add(p);
  }
}

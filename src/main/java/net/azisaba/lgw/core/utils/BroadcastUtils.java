package net.azisaba.lgw.core.utils;

import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class BroadcastUtils {

  private static final List<String> DISABLED_WORLD_NAMES = Collections.singletonList("tutorial");

  public static void broadcast(String message) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (DISABLED_WORLD_NAMES.contains(p.getWorld().getName())) {
        continue;
      }
      p.sendMessage(message);
    }
  }
}

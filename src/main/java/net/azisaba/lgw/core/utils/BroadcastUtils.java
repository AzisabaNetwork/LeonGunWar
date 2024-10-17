package net.azisaba.lgw.core.utils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import me.rayzr522.jsonmessage.JSONMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class BroadcastUtils {

  private static final List<String> DISABLED_WORLD_NAMES = Collections.singletonList("tutorial");

  public static void broadcast(String message) {
    Bukkit.getConsoleSender().sendMessage(message);
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (DISABLED_WORLD_NAMES.contains(p.getWorld().getName())) {
        continue;
      }
      p.sendMessage(message);
    }
  }
  public static void broadcast(JSONMessage message,Sound sound ) {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (DISABLED_WORLD_NAMES.contains(p.getWorld().getName())) {
        continue;
      }
      message.send(p);
      p.playSound(p.getLocation(), sound, 1, 1);
    }
  }

  public static List<Player> getOnlinePlayers() {
    return Bukkit.getOnlinePlayers().stream()
        .filter(p -> !DISABLED_WORLD_NAMES.contains(p.getWorld().getName()))
        .collect(Collectors.toList());
  }
}

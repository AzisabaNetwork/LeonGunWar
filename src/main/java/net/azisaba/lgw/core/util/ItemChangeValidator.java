package net.azisaba.lgw.core.util;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vindicator;

public class ItemChangeValidator {

  private final HashMap<UUID, Boolean> isShot = new HashMap<>();
  private final HashMap<UUID, BattleTeam> teamCache = new HashMap<>();

  public boolean isAllowedToChangeItem(Player p) {
    if (!LeonGunWar.getPlugin().getManager().isMatching()) {
      return true;
    }

    MatchManager manager = LeonGunWar.getPlugin().getManager();
    BattleTeam team = teamCache.getOrDefault(p.getUniqueId(), null);

    if (team == null) {
      team = manager.getBattleTeam(p);
      if (team == null) {
        return true;
      }
      teamCache.put(p.getUniqueId(), team);
    }

    if (isShot.getOrDefault(p.getUniqueId(), false)) {
      return false;
    }

    if (manager.getRespawnProtection().isProtected(p)) {
      return true;
    }

    Location spawnPoint = manager.getCurrentGameMap().getSpawnPoint(team);
    if (spawnPoint == null) {
      return true;
    }
    if (spawnPoint.getWorld() != p.getWorld()) {
      return true;
    }

    if (spawnPoint.distance(p.getLocation()) <= 5) {
      return true;
    }

    List<Entity> entities = p.getNearbyEntities(5, 5, 5);
    for (Entity e : entities) {
      if (e instanceof Vindicator) {
        return true;
      }
    }
    return false;
  }

  public void shot(Player p) {
    if (LeonGunWar.getPlugin().getManager().getRespawnProtection().isProtected(p)) {
      return;
    }
    isShot.put(p.getUniqueId(), true);
  }

  public void respawned(Player p) {
    isShot.remove(p.getUniqueId());
  }
}

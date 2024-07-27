package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PreventEscapeListener implements Listener {

  private static final List<Material> WATER_BLOCKS = Collections.unmodifiableList(
      Arrays.asList(Material.WATER, Material.LEGACY_STATIONARY_WATER));
  private static final List<Material> ILLEGAL_BLOCKS = Collections.unmodifiableList(
      Arrays.asList(Material.BARRIER, Material.STRUCTURE_BLOCK, Material.STRUCTURE_VOID));

  private final HashMap<UUID, Long> lastDamaged = new HashMap<>();

  @EventHandler
  public void onMoveEvent(PlayerMoveEvent e) {
    // 試合が開始されていない場合return
    if (!LeonGunWar.getPlugin().getManager().isMatching()) {
      return;
    }
    // プレイヤーが試合に参加していない場合return
    if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(e.getPlayer())) {
      return;
    }

    // 既にダメージを与えた場合return
    if (lastDamaged.getOrDefault(e.getPlayer().getUniqueId(), 0L) > System.currentTimeMillis()) {
      return;
    }

    // 水の中にいない場合return
    if (!WATER_BLOCKS.contains(e.getPlayer().getLocation().getBlock().getType())) {
      return;
    }

    Player p = e.getPlayer();

    // 下のブロックを探索する
    Block block = e.getTo().getBlock();
    Material underType = null;
    while (block.getY() > 0) {
      block = block.getRelative(0, -1, 0);
      if (WATER_BLOCKS.contains(block.getType()) || block.getType() == Material.AIR) {
        continue;
      }

      underType = block.getType();
      break;
    }

    // 下が奈落か、不正なブロックの場合ダメージを与える
    if (underType == null || ILLEGAL_BLOCKS.contains(underType)) {
      p.damage(9999);
      lastDamaged.put(p.getUniqueId(), System.currentTimeMillis() + 1000);
    }
  }
}

package net.azisaba.lgw.core.listeners.others;

import com.shampaggon.crackshot.CSUtility;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class RemoveKillStreakScoreListener implements Listener {

  private final CSUtility csUtility = new CSUtility();
  private final List<String> killStreakItemIds = Collections.unmodifiableList(
      Arrays.asList("5ks", "10ks", "15ks"));

  @EventHandler
  public void onChangeWorld(PlayerChangedWorldEvent e) {
    Player p = e.getPlayer();

    // ゲームモードがクリエイティブかスペクテイターの場合はreturn
    if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) {
      return;
    }

    Inventory inv = e.getPlayer().getInventory();
    for (int i = 0; i < 36; i++) {
      ItemStack item = inv.getItem(i);
      String id = csUtility.getWeaponTitle(item);

      // キルストリークアイテムのCrackShot IDと同じ場合はアイテムを削除
      if (id != null && killStreakItemIds.contains(id)) {
        inv.setItem(i, null);
      }
    }
  }
}

package net.azisaba.lgw.core.listeners.weaponcontrols;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import java.util.Arrays;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DisableToysDuringMatchListener implements Listener {

  private final CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

  @EventHandler
  public void onWeaponPrepareShoot(WeaponPrepareShootEvent event) {
    Player player = event.getPlayer();

    if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(player)) {
      return;
    }

    String weapon = event.getWeaponTitle();
    String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

    if (ctrl == null) {
      return;
    }

    String[] groups = ctrl.replaceAll(" ", "").split(",");

    if (Arrays.asList(groups).contains("toy")) {
      player.sendMessage(Chat.f("{0}&c試合中にこのアイテムは使用できません！", LeonGunWar.GAME_PREFIX));
      event.setCancelled(true);
    }
  }
}

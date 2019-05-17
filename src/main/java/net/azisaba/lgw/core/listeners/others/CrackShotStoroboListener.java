package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;

import net.azisaba.lgw.core.LeonGunWar;

public class CrackShotStoroboListener implements Listener {

	private final HashMap<Player, Long> lastShotLong = new HashMap<>();
	private double cooldown = 0;

	public CrackShotStoroboListener() {
		CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
		cooldown = cs.getDouble("STOROBO2.Airstrikes.Multiple_Strikes.Delay_Between_Strikes");
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onShoot(WeaponPreShootEvent e) {
		if (e.getWeaponTitle().equals("STOROBO2")) {

			Player p = e.getPlayer();

			if (!lastShotLong.containsKey(p)) {
				lastShotLong.put(p, System.currentTimeMillis());
				return;
			}

			if (lastShotLong.get(p) + 1000 * cooldown > System.currentTimeMillis()) {
				e.setCancelled(true);
				LeonGunWar.getPlugin().getLogger().info("Cancelled LightningStrike for " + p.getName());
				return;
			}

			lastShotLong.put(p, System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (lastShotLong.containsKey(e.getPlayer())) {
			lastShotLong.remove(e.getPlayer());
		}
	}
}

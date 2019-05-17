package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;

/**
 * CrackShotのアイテムの連打を無効化するリスナー
 * @author siloneco
 *
 */
public class CrackShotLimitListener implements Listener {

	private final HashMap<Player, Long> knifeMap = new HashMap<>();
	private double knifeCooldown = 0;

	private final HashMap<Player, Long> storoboMap = new HashMap<>();
	private double storoboCooldown = 0;

	public CrackShotLimitListener() {
		CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

		knifeCooldown = cs.getDouble("Combat_Knife.Shooting.Delay_Between_Shots") / 20;
		storoboCooldown = cs.getDouble("STOROBO2.Airstrikes.Multiple_Strikes.Delay_Between_Strikes") / 20;
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onWeaponDamage(WeaponDamageEntityEvent e) {
		if (!e.getWeaponTitle().equals("Combat_Knife")) {
			return;
		}

		Player p = e.getPlayer();

		if (knifeMap.getOrDefault(p, 0L) + (1000 * knifeCooldown) > System.currentTimeMillis()) {
			e.setCancelled(true);
			return;
		}

		knifeMap.put(p, System.currentTimeMillis());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onShoot(WeaponPreShootEvent e) {
		if (!e.getWeaponTitle().equals("STOROBO2")) {
			return;
		}

		Player p = e.getPlayer();

		if (storoboMap.getOrDefault(p, 0L) + (1000 * storoboCooldown) > System.currentTimeMillis()) {
			e.setCancelled(true);
			return;
		}

		storoboMap.put(p, System.currentTimeMillis());
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();

		if (knifeMap.containsKey(p)) {
			knifeMap.remove(p);
		}
		if (storoboMap.containsKey(p)) {
			storoboMap.remove(p);
		}
	}
}

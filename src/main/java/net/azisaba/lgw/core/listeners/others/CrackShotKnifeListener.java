package net.azisaba.lgw.core.listeners.others;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

/**
 * コンバットナイフの連打を無効化するリスナー
 * @author siloneco
 *
 */
public class CrackShotKnifeListener implements Listener {

	private final HashMap<Player, Long> cooldownMap = new HashMap<>();
	private double cooldown = 0;

	public CrackShotKnifeListener() {
		CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
		cooldown =  cs.getDouble("Combat_Knife.Shooting.Delay_Between_Shots") / 20;
	}

	@EventHandler
	public void onWeaponDamage(WeaponDamageEntityEvent e) {
		if (e.getWeaponTitle().equals("Combat_Knife")) {
			Player p = e.getPlayer();

			if (!cooldownMap.containsKey(p)) {
				cooldownMap.put(p, System.currentTimeMillis());
				return;
			}

			if (cooldownMap.get(p) + 1000 * cooldown > System.currentTimeMillis()) {
				e.setCancelled(true);
				return;
			}

			cooldownMap.put(p, System.currentTimeMillis());
		}
	}

	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		if (cooldownMap.containsKey(e.getPlayer())) {
			cooldownMap.remove(e.getPlayer());
		}
	}
}

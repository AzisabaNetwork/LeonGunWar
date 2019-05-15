package net.azisaba.lgw.core.listeners.others;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.teams.BattleTeam;

public class AutoRespawnListener implements Listener {

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// リスポーン
		deathPlayer.spigot().respawn();
	}

	@EventHandler
	public void onRespawn(PlayerRespawnEvent e) {
		e.setRespawnLocation(getRespawnLocation(e.getPlayer()));

		Player p = e.getPlayer();

		new BukkitRunnable() {
			@Override
			public void run() {

				// 体力と空腹度を1にする
				p.setHealth(1);
				p.setFoodLevel(1);

				// 体力をカッコよく回復！
				double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
				double diffHp = maxHp - p.getHealth();
				PotionEffect healHpEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) diffHp, 114514,
						false);
				p.addPotionEffect(healHpEffect, true);

				// 空腹度をカッコよく回復！
				double maxFood = 40;
				double diffFood = maxFood - p.getFoodLevel();
				PotionEffect healFoodEffect = new PotionEffect(PotionEffectType.SATURATION, (int) diffFood, 1, false);
				p.addPotionEffect(healFoodEffect, true);
			}
		}.runTaskLater(LeonGunWar.getPlugin(), 0);
	}

	private Location getRespawnLocation(Player p) {
		// チームを取得
		BattleTeam playerTeam = LeonGunWar.getPlugin().getManager().getBattleTeam(p);
		// スポーン地点
		Location spawnPoint = null;

		// チームがnullではないならそのチームのスポーン地点にTPする
		if (playerTeam != null) {
			spawnPoint = LeonGunWar.getPlugin().getManager().getCurrentGameMap().getSpawnPoint(playerTeam);
		}

		// それでもまだspawnPointがnullの場合lobbyのスポーン地点を指定
		if (spawnPoint == null) {
			spawnPoint = LeonGunWar.getPlugin().getManager().getLobbySpawnLocation();
		}

		return spawnPoint;
	}
}

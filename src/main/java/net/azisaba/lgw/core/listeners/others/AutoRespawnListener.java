package net.azisaba.lgw.core.listeners.others;

import java.util.Objects;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.teams.BattleTeam;

public class AutoRespawnListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onDeath(PlayerDeathEvent e) {
		Player deathPlayer = e.getEntity();

		// 落下速度を無くす (奈落対策)
		deathPlayer.setFallDistance(0);

		// 消火！！
		Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> deathPlayer.setFireTicks(0), 2);

		// エフェクトを消す！！
		Stream.of(PotionEffectType.values())
				.filter(Objects::nonNull)
				.forEach(deathPlayer::removePotionEffect);

		// 体力と空腹度を若干回復して死を免れる
		deathPlayer.setHealth(1);
		deathPlayer.setFoodLevel(1);

		// 体力をカッコよく回復！
		double maxHp = deathPlayer.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
		double diffHp = maxHp - deathPlayer.getHealth();
		PotionEffect healHpEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) diffHp, 114514, false);
		deathPlayer.addPotionEffect(healHpEffect, true);

		// 空腹度をカッコよく回復！
		double maxFood = 40;
		double diffFood = maxFood - deathPlayer.getFoodLevel();
		PotionEffect healFoodEffect = new PotionEffect(PotionEffectType.SATURATION, (int) diffFood, 1, false);
		deathPlayer.addPotionEffect(healFoodEffect, true);

		// リスポーンイベントをfire
		PlayerRespawnEvent event = new PlayerRespawnEvent(deathPlayer, getRespawnLocation(deathPlayer), true);
		Bukkit.getPluginManager().callEvent(event);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onRespawn(PlayerRespawnEvent e) {

		if (!e.getPlayer().isDead()) {
			e.getPlayer().teleport(e.getRespawnLocation());
		}
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

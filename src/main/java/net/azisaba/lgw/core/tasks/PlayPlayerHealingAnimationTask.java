package net.azisaba.lgw.core.tasks;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayPlayerHealingAnimationTask extends BukkitRunnable {

	private final Player p;

	public PlayPlayerHealingAnimationTask(Player p) {
		this.p = p;
	}

	@Override
	public void run() {
		// 体力と空腹度を1にする
		p.setHealth(1);
		p.setFoodLevel(1);

		// 体力をカッコよく回復！
		double maxHp = p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getDefaultValue();
		double diffHp = maxHp - p.getHealth();
		PotionEffect healHpEffect = new PotionEffect(PotionEffectType.REGENERATION, (int) diffHp, 114514, false);
		p.addPotionEffect(healHpEffect, true);

		// 空腹度をカッコよく回復！
		double maxFood = 40;
		double diffFood = maxFood - p.getFoodLevel();
		PotionEffect healFoodEffect = new PotionEffect(PotionEffectType.SATURATION, (int) diffFood, 1, false);
		p.addPotionEffect(healFoodEffect, true);
	}
}
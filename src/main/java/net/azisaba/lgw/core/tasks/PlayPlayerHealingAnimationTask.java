package net.azisaba.lgw.core.tasks;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PlayPlayerHealingAnimationTask extends BukkitRunnable {

    private final Player p;

    @Override
    public void run() {
        // 体力を安全に回復！
        PotionEffect healHpEffect = new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 5);
        p.addPotionEffect(healHpEffect, true);

        // 空腹度を安全に回復！
        PotionEffect healFoodEffect = new PotionEffect(PotionEffectType.SATURATION, 5 * 20, 1);
        p.addPotionEffect(healFoodEffect, true);
    }
}

package net.azisaba.lgw.core.listeners.others;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.kills.KillReward;

public class KillStreaksListener implements Listener {

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		// 自滅は無視
		if (p.getKiller() == null || p.getKiller() == p) {
			return;
		}

		Player killer = p.getKiller();

		// カウントを追加
		int streaks = LeonGunWar.getPlugin().getKillStreaks().get(killer).incrementAndGet();

		// 報酬を付与
		LeonGunWar.getPlugin().getKillStreaks().getRewards().entrySet().stream()
				.filter(entry -> streaks % entry.getKey() == 0)
				.map(Map.Entry::getValue)
				.map(KillReward::getCommands)
				.filter(Objects::nonNull)
				.map(commands -> commands.apply(killer))
				.flatMap(List::stream)
				.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

		// キルストリークをお知らせ
		LeonGunWar.getPlugin().getKillStreaks().getRewards().entrySet().stream()
				.filter(entry -> streaks == entry.getKey())
				.map(Map.Entry::getValue)
				.map(KillReward::getMessage)
				.filter(Objects::nonNull)
				.map(message -> message.apply(killer))
				.forEach(Bukkit::broadcastMessage);
	}
}

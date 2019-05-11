package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;

public class KillStreaksListener implements Listener {

	private final Map<UUID, AtomicInteger> streaksMap = new HashMap<>();
	private final Map<Integer, Reward> rewards = new HashMap<>();

	public KillStreaksListener() {
		rewards.put(1, new Reward(
				null,
				p -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:emerald 2", p.getName()))));
		rewards.put(3, new Reward(
				p -> Chat.f("{0}&r{1} &7が {2}{3}人 連続キル！", LeonGunWar.GAME_PREFIX, p.getDisplayName(), "", 3),
				null));
		rewards.put(5, new Reward(
				p -> Chat.f("{0}&r{1} &7が {2}{3}人 連続キル！", LeonGunWar.GAME_PREFIX, p.getDisplayName(), "", 5),
				p -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:diamond 1", p.getName()),
						Chat.f("crackshot:shot give {0} STOROBO2", p.getName()))));
	}

	public class Reward {

		private final Function<Player, String> message;
		private final Function<Player, List<String>> commands;

		public Reward(Function<Player, String> message, Function<Player, List<String>> commands) {
			this.message = message;
			this.commands = commands;
		}

		public Function<Player, String> getMessage() {
			return message;
		}

		public Function<Player, List<String>> getCommands() {
			return commands;
		}
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Player p = e.getEntity();

		// 自滅は無視
		if (p.getKiller() == null || p.getKiller() == p) {
			return;
		}

		Player killer = p.getKiller();

		// カウントを追加
		streaksMap.putIfAbsent(killer.getUniqueId(), new AtomicInteger(0));
		int streaks = streaksMap.get(killer.getUniqueId()).incrementAndGet();

		// 報酬を付与
		rewards.entrySet().stream()
				.filter(entry -> streaks % entry.getKey() == 0)
				.map(Map.Entry::getValue)
				.map(Reward::getCommands)
				.filter(Objects::nonNull)
				.map(commands -> commands.apply(p))
				.flatMap(List::stream)
				.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

		// キルストリークをお知らせ
		rewards.entrySet().stream()
				.filter(entry -> streaks == entry.getKey())
				.map(Map.Entry::getValue)
				.map(Reward::getMessage)
				.filter(Objects::nonNull)
				.map(message -> message.apply(p))
				.forEach(Bukkit::broadcastMessage);
	}
}

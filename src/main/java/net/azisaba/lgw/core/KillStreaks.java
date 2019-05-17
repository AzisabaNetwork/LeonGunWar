package net.azisaba.lgw.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;

import net.azisaba.lgw.core.util.KillReward;
import net.azisaba.lgw.core.utils.Chat;

public class KillStreaks {

	private final Map<UUID, AtomicInteger> streaksMap = new HashMap<>();
	private final Map<Integer, KillReward> rewards;

	public KillStreaks() {
		Map<Integer, KillReward> rewards = new HashMap<>();
		rewards.put(1, new KillReward(
				null,
				player -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:emerald 2", player.getName()))));
		rewards.put(3, new KillReward(
				player -> Chat.f("{0}&r{1} &7が {2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 3),
				null));
		rewards.put(5, new KillReward(
				player -> Chat.f("{0}&r{1} &7が {2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 5),
				player -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:diamond 1", player.getName()),
						Chat.f("crackshot:shot give {0} STOROBO2", player.getName()))));
		rewards.put(10, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &2{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 10),
				player -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:diamond 1", player.getName()),
						Chat.f("crackshot:shot give {0} UAV", player.getName()))));
		rewards.put(15, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &2{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 15),
				player -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:diamond 1", player.getName()),
						Chat.f("crackshot:shot give {0} DEATHMACHINE", player.getName()))));
		rewards.put(20, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &e{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 20),
				player -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:diamond 1", player.getName()),
						Chat.f("crackshot:shot give {0} STOROBO", player.getName()))));
		rewards.put(25, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &e{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 25),
				null));
		rewards.put(30, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &b{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 30),
				null));
		rewards.put(35, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &b{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 35),
				null));
		rewards.put(40, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &c{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 40),
				null));
		rewards.put(45, new KillReward(
				player -> Chat.f("{0}&r{1} &7が &c{2}人 &7連続キル！", LeonGunWar.GAME_PREFIX, player.getDisplayName(), 45),
				null));
		rewards.put(50, new KillReward(
				player -> Chat.f("{0}&7なんてこったい！ &r{1}&7 が &c{2}人 &7連続キル！ カンスト！", LeonGunWar.GAME_PREFIX,
						player.getDisplayName(),
						50),
				player -> Arrays.asList(
						Chat.f("minecraft:give {0} minecraft:diamond 64", player.getName()),
						Chat.f("crackshot:shot give {0} KANSUTO", player.getName()))));
		this.rewards = Collections.unmodifiableMap(rewards);
	}

	public void clear() {
		streaksMap.clear();
	}

	public void remove(Player player) {
		streaksMap.remove(player.getUniqueId());
	}

	public AtomicInteger get(Player player) {
		streaksMap.putIfAbsent(player.getUniqueId(), new AtomicInteger(0));
		return streaksMap.get(player.getUniqueId());
	}

	public Map<Integer, KillReward> getRewards() {
		return rewards;
	}
}

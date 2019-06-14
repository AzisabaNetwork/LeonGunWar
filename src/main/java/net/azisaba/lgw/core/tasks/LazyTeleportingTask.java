package net.azisaba.lgw.core.tasks;

import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.NonNull;

// たくさんのプレイヤーを順番にテレポートすることでラグを軽減する
public class LazyTeleportingTask extends BukkitRunnable {

	private final Queue<Map.Entry<Player, Location>> requests = new ArrayDeque<>();

	public void request(@NonNull Player player, @NonNull Location location) {
		Map.Entry<Player, Location> request = new AbstractMap.SimpleEntry<>(player, location);
		requests.add(request);
	}

	public void requestAll(@NonNull List<Player> players, @NonNull Location location) {
		players.forEach(player -> request(player, location));
	}

	/**
	 * Pluginの終了時に呼び出し
	 */
	public void teleportAll() {
		while (!requests.isEmpty()) {
			Map.Entry<Player, Location> request = requests.poll();
			teleport(request);
		}
	}

	@Override
	public void run() {
		if (!requests.isEmpty()) {
			Map.Entry<Player, Location> request = requests.poll();
			teleport(request);
		}
	}

	private void teleport(Map.Entry<Player, Location> request) {
		Player player = request.getKey();
		Location location = request.getValue();
		player.teleport(location);
	}
}

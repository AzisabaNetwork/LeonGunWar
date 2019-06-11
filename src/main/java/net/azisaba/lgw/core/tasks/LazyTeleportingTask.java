package net.azisaba.lgw.core.tasks;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import lombok.NonNull;

// たくさんのプレイヤーを順番にテレポートすることでラグを軽減する
public class LazyTeleportingTask extends BukkitRunnable {

	private final Location location;
	private final Queue<Player> players;

	public LazyTeleportingTask(@NonNull Location location, @NonNull Collection<Player> players) {
		this.location = location;
		this.players = new ArrayBlockingQueue<>(players.size(), false, players);
	}

	@Override
	public void run() {
		if (players.isEmpty()) {
			cancel();
		} else {
			Player player = players.poll();
			if (player != null) {
				player.teleport(location);
			}
		}
	}
}

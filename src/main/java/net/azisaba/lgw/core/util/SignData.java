package net.azisaba.lgw.core.util;

import java.util.UUID;

import org.bukkit.Location;

public class SignData {

	private final String playerName;
	private final UUID author;
	private final long breakAt;
	private final Location loc;

	public SignData(Location loc, String playerName, UUID author, long breakAt) {
		this.loc = loc;
		this.playerName = playerName;
		this.author = author;
		this.breakAt = breakAt;
	}

	public String getPlayerName() {
		return playerName;
	}

	public UUID getAuthor() {
		return author;
	}

	public long getBreakAt() {
		return breakAt;
	}

	public Location getLocation() {
		return loc;
	}
}

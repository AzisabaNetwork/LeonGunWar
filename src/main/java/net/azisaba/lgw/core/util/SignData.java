package net.azisaba.lgw.core.util;

import java.util.UUID;

import org.bukkit.Location;

import lombok.Data;

@Data
public class SignData {

	private final Location location;
	private final String playerName;
	private final UUID author;
	private final long breakAt;
}

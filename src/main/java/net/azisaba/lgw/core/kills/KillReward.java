package net.azisaba.lgw.core.kills;

import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;

public class KillReward {

	private final Function<Player, String> message;
	private final Function<Player, List<String>> commands;

	public KillReward(Function<Player, String> message, Function<Player, List<String>> commands) {
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

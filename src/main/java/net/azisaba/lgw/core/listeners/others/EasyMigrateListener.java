package net.azisaba.lgw.core.listeners.others;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.plugin.Plugin;

public class EasyMigrateListener implements Listener {

	private final List<String> unnecessaryPlugins = Arrays.asList(
			"UnbreakableArmor",
			"NoArrowGround",
			"noKnockback",
			"CancelMan",
			"RecipeHaMouShindeiru",
			"Yaitekanai",
			"KillStreaks",
			"Protectiontime",
			"BugSolver",
			"CatFood",
			"ColorTeaming",
			"ColorTeamingEntry",
			"ExpTimer",
			"BarAPI");
	private final List<String> unnecessaryWorlds = Arrays.asList(
			"cargo",
			"carrier",
			"drone",
			"encore",
			"express",
			"grind",
			"hijacked",
			"magma",
			"meltdown",
			"nuketown",
			"overflow",
			"plaza",
			"raid",
			"slums",
			"standoff",
			"studio",
			"vertigo");

	public EasyMigrateListener() {
		unnecessaryPlugins.stream()
				.map(Bukkit.getPluginManager()::getPlugin)
				.filter(Objects::nonNull)
				.forEach(Bukkit.getPluginManager()::disablePlugin);
		unnecessaryWorlds.stream()
				.map(Bukkit::getWorld)
				.filter(Objects::nonNull)
				.forEach(world -> Bukkit.unloadWorld(world, true));
	}

	@EventHandler
	public void onPluginEnable(PluginEnableEvent e) {
		Plugin plugin = e.getPlugin();
		if (plugin != null && unnecessaryPlugins.contains(plugin.getName())) {
			Bukkit.getPluginManager().disablePlugin(plugin);
		}
	}

	@EventHandler
	public void onWorldInit(WorldInitEvent e) {
		World world = e.getWorld();
		if (world != null && unnecessaryWorlds.contains(world.getName())) {
			Bukkit.unloadWorld(world, true);
		}
	}
}

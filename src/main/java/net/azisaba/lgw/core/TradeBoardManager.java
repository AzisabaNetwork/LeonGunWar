package net.azisaba.lgw.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class TradeBoardManager {

	private static File dataFolder = null;
	private static HashMap<Location, SignData> signs = new HashMap<>();

	protected static void init() {
		dataFolder = new File(LeonGunWar.getPlugin().getDataFolder(), "Signs");

		if (!dataFolder.exists()) {
			return;
		}

		for (File file : dataFolder.listFiles()) {
			if (!file.isFile()) {
				continue;
			}

			if (!file.getName().endsWith(".yml")) {
				continue;
			}

			String locStr = file.getName().substring(0, file.getName().length() - 4);
			Location loc = locationFromString(locStr);

			if (loc == null) {
				Bukkit.getLogger().warning("Error trying parsing location \"" + file.getName() + "\"");
				continue;
			}

			YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

			String playerName = conf.getString("PlayerName", null);
			String uuidStr = conf.getString("UUID", null);
			UUID uuid = null;
			try {
				uuid = UUID.fromString(uuidStr);
			} catch (Exception e) {

			}

			long expire = conf.getLong("Expire", 0L);

			if (uuid != null && playerName != null) {
				SignData data = new SignData(loc, playerName, uuid, expire);
				signs.put(loc, data);
				LeonGunWar.getPlugin().getLogger()
						.info(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + ","
								+ loc.getBlockZ() + "の看板をロードしました");
			} else {
				LeonGunWar.getPlugin().getLogger()
						.warning(loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + ","
								+ loc.getBlockZ() + "の看板はロードされませんでした");
			}
		}
	}

	public static SignData getSignData(Location loc) {
		if (signs.containsKey(loc)) {
			return signs.get(loc);
		}

		return null;
	}

	public static void removeSignData(Location loc) {
		if (signs.containsKey(loc)) {
			signs.remove(loc);
		}
	}

	public static boolean addSignData(Location loc, String playerName, UUID author, long breakAt) {
		if (signs.containsKey(loc)) {
			return false;
		}
		SignData sign = new SignData(loc, playerName, author, breakAt);
		signs.put(loc, sign);
		return true;
	}

	public static List<SignData> getAllSignData() {
		return new ArrayList<>(signs.values());
	}

	protected static void saveAll() {

		if (!dataFolder.exists()) {
			dataFolder.mkdirs();
		}

		for (File file : dataFolder.listFiles()) {
			if (file.getName().endsWith(".yml")) {
				file.delete();
			}
		}

		for (Location loc : signs.keySet()) {
			File file = locationToFile(loc);
			YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);
			SignData data = signs.get(loc);

			conf.set("PlayerName", data.getPlayerName());
			conf.set("UUID", data.getAuthor().toString());
			conf.set("Expire", data.getBreakAt());

			try {
				conf.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static Location locationFromString(String str) {
		String[] split = str.split(",");
		Location loc = null;
		try {
			World world = Bukkit.getWorld(split[0]);
			loc = new Location(world, Integer.parseInt(split[1]), Integer.parseInt(split[2]),
					Integer.parseInt(split[3]));
		} catch (Exception e) {
			return null;
		}

		return loc;
	}

	private static File locationToFile(Location loc) {
		return new File(dataFolder,
				loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()
						+ ".yml");
	}
}

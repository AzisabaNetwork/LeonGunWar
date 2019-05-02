package net.azisaba.lgw.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;

public class LocationLoader {

	/**
	 *
	 * 座標を指定されたYamlConfigurationにセットします
	 * マップデータの保存形式上ワールドは必要ないので保存しません
	 *
	 * @param conf Locationを設定するYamlConfiguration
	 * @param loc 設定するLocation
	 * @param key 設定するキー
	 */
	public static void setLocation(YamlConfiguration conf, Location loc, String key) {
		conf.set(key + ".X", loc.getX());
		conf.set(key + ".Y", loc.getY());
		conf.set(key + ".Z", loc.getZ());
		conf.set(key + ".Yaw", (double) loc.getYaw());
		conf.set(key + ".Pitch", (double) loc.getPitch());
	}

	/**
	 *
	 * 座標を指定されたYamlConfigurationにセットします
	 * マップデータの保存形式上ワールドは必要ないので保存しません
	 *
	 * @param conf Locationを設定するYamlConfiguration
	 * @param loc 設定するLocation
	 * @param key 設定するキー
	 */
	public static void setLocationWithWorld(YamlConfiguration conf, Location loc, String key) {
		conf.set(key + ".World", loc.getWorld().getName());
		conf.set(key + ".X", loc.getX());
		conf.set(key + ".Y", loc.getY());
		conf.set(key + ".Z", loc.getZ());
		conf.set(key + ".Yaw", (double) loc.getYaw());
		conf.set(key + ".Pitch", (double) loc.getPitch());
	}

	/**
	 *
	 * 上記の setLocation メソッドで保存されたLocationをロードします
	 * ワールドは適用されません
	 *
	 * @param conf
	 * @param key
	 * @return
	 */
	public static Location getLocation(YamlConfiguration conf, String key) {
		Location loc = null;

		// x, y, zの値が保存されているかの確認 (保存されていなければnullを返す)
		if (!conf.isSet(key + ".X") || !conf.isSet(key + ".Y") || !conf.isSet(key + ".Z")) {
			return null;
		}

		double x = conf.getDouble(key + ".X");
		double y = conf.getDouble(key + ".Y");
		double z = conf.getDouble(key + ".Z");

		// 座標作成
		loc = new Location(null, x, y, z);

		// YawとPitchが指定されているなら設定する
		if (conf.isSet(key + ".Yaw"))
			loc.setYaw((float) conf.getDouble(key + ".Yaw"));
		if (conf.isSet(key + ".Pitch"))
			loc.setPitch((float) conf.getDouble(key + ".Pitch"));

		// Worldが指定されているなら設定する
		if (conf.isSet(key + ".World"))
			loc.setWorld(Bukkit.getWorld(conf.getString(key + ".World")));

		return loc;
	}

}

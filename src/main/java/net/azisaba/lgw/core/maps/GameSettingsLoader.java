package net.azisaba.lgw.core.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import net.azisaba.lgw.core.LeonGunWar;

/**
 *
 * セーブされたマップのデータの読み込みやセーブを行うためのクラス
 * @author siloneco
 *
 */
public class GameSettingsLoader {

	private static LeonGunWar plugin = null;

	// Mapデータを格納するフォルダ
	private static File dataFolder;

	/**
	 * 初期化メゾッド
	 * Pluginのロード時に呼び出す
	 * @param plugin
	 */
	protected static void init(LeonGunWar plugin) {
		GameSettingsLoader.plugin = plugin;
		dataFolder = new File(plugin.getDataFolder(), "Maps");
	}

	/**
	 * 保存されているマップデータを読み込み、MapDataのリストを返します
	 * @return 読み込まれたマップデータのリスト
	 */
	protected static List<GameMap> loadMapData() {
		if (plugin == null) {
			throw new IllegalStateException("\"plugin\" field is not initialized yet.");
		}

		// GameMapリスト
		List<GameMap> gameMapList = new ArrayList<>();

		// file = マップデータ
		for (File file : dataFolder.listFiles()) {
			// ymlでもyamlでもなければcontinue
			if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) {
				continue;
			}

			// ロード
			YamlConfiguration yamlData = YamlConfiguration.loadConfiguration(file);

			// フィールド作成
			String mapName = null;
			World world = null;
			Location redSpawn = null, blueSpawn = null;

			// 全データを読み込めたか
			boolean successLoad = true;

			// 各値を読みこむ (設定されていなければ警告)
			if (yamlData.isSet(MAP_NAME_KEY))
				mapName = yamlData.getString(MAP_NAME_KEY);
			else {
				plugin.getLogger().warning("\"" + MAP_NAME_KEY + "\"の値が設定されていません。 (FileName=" + file.getName() + ")");
				successLoad = false;
			}

			if (yamlData.isSet(WORLD_NAME_KEY)) {
				String worldName = yamlData.getString(WORLD_NAME_KEY);
				world = Bukkit.getWorld(worldName);

				// worldが存在しない場合
				if (world == null) {
					plugin.getLogger()
							.warning("\"" + worldName + "\"という名前のワールドは存在しません。 (FileName=" + file.getName() + ")");
					successLoad = false;
				}
			} else {
				plugin.getLogger().warning("\"" + WORLD_NAME_KEY + "\"の値が設定されていません。 (FileName=" + file.getName() + ")");
				successLoad = false;
			}

			redSpawn = getLocation(yamlData, RED_SPAWN_KEY);
			if (redSpawn == null) {
				plugin.getLogger().warning("\"" + RED_SPAWN_KEY + "\"が読み込めませんでした (FileName=" + file.getName() + ")");
				successLoad = false;
			}

			blueSpawn = getLocation(yamlData, BLUE_SPAWN_KEY);
			if (redSpawn == null) {
				plugin.getLogger().warning("\"" + BLUE_SPAWN_KEY + "\"が読み込めませんでした (FileName=" + file.getName() + ")");
				successLoad = false;
			}

			// 正常に読み込めていない値がある場合はcontinue
			if (!successLoad)
				continue;

			// GameMap作成
			GameMap data = new GameMap(mapName, world, redSpawn, blueSpawn);
			// リストに追加
			gameMapList.add(data);
		}

		// gameMapListを返す
		return gameMapList;
	}

	/**
	 * 作成されたGameMapをファイルに保存します。
	 * @param map 保存したいGameMap
	 * @param 保存するファイルの名前
	 * @param 上書きを許可するかどうか
	 *
	 * @return データを保存したかどうか
	 */
	public static boolean saveGameMap(GameMap map, String fileName, boolean allowOverwrite) {
		// ファイル取得
		File file = new File(dataFolder, fileName);

		// 上書きを許可せずファイルが存在する場合はreturn
		if (!allowOverwrite && file.exists()) {
			return false;
		}

		// YamlConfiguration作成
		YamlConfiguration dataYaml = new YamlConfiguration();

		// 各データを保存
		dataYaml.set(MAP_NAME_KEY, map.getMapName());
		dataYaml.set(WORLD_NAME_KEY, map.getWorld().getName());
		setLocation(dataYaml, map.getRedSpawn(), RED_SPAWN_KEY);
		setLocation(dataYaml, map.getBlueSpawn(), BLUE_SPAWN_KEY);

		// セーブ
		try {
			dataYaml.save(file);
		} catch (IOException e) {
			// 失敗したらエラーを出してfalseを返す
			e.printStackTrace();
			return false;
		}

		// 成功したらtrueを返す
		return true;
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
	private static void setLocation(YamlConfiguration conf, Location loc, String key) {
		conf.set(key + ".X", loc.getX());
		conf.set(key + ".Y", loc.getY());
		conf.set(key + ".Z", loc.getZ());
		conf.set(key + ".Yaw", (double) loc.getYaw());
		conf.set(key + ".Pitch", (double) loc.getYaw());
	}

	/**
	 *
	 * 上記の setLocation メゾッドで保存されたLocationをロードします
	 * ワールドは適用されません
	 *
	 * @param conf
	 * @param key
	 * @return
	 */
	private static Location getLocation(YamlConfiguration conf, String key) {
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

		return loc;
	}

	// プレイヤーに表示するマップ名のキー
	private static final String MAP_NAME_KEY = "MapName";
	// ワールド名のキー
	private static final String WORLD_NAME_KEY = "World";
	// 赤スポーンのキー
	private static final String RED_SPAWN_KEY = "SpawnPoint.Red";
	// 青スポーンのキー
	private static final String BLUE_SPAWN_KEY = "SpawnPoint.Blue";
}

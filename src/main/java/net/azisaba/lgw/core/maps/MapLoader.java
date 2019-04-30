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
import net.azisaba.lgw.core.utils.LocationLoader;

/**
 *
 * セーブされたマップのデータの読み込みやセーブを行うためのクラス
 * @author siloneco
 *
 */
public class MapLoader {

	private static LeonGunWar plugin = null;

	// Mapデータを格納するフォルダ
	private static File dataFolder;

	/**
	 * 初期化メゾッド
	 * Pluginのロード時に呼び出す
	 * @param plugin
	 */
	protected static void init(LeonGunWar plugin) {
		dataFolder = new File(plugin.getDataFolder(), "Maps");
		MapLoader.plugin = plugin;
	}

	/**
	 * 保存されているマップデータを読み込み、MapDataのリストを返します
	 * @return 読み込まれたマップデータのリスト
	 *
	 * @exception IllegalStateException 初期化される前にメゾッドが呼び出された場合
	 */
	protected static List<GameMap> loadMapData() {
		// pluginがnullの場合は初期化前としてIllegalStateException
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

			redSpawn = LocationLoader.getLocation(yamlData, RED_SPAWN_KEY);
			if (redSpawn == null) {
				plugin.getLogger().warning("\"" + RED_SPAWN_KEY + "\"が読み込めませんでした (FileName=" + file.getName() + ")");
				successLoad = false;
			}

			blueSpawn = LocationLoader.getLocation(yamlData, BLUE_SPAWN_KEY);
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
	 *
	 * @exception IllegalStateException 初期化される前にメゾッドが呼び出された場合
	 */
	public static boolean saveGameMap(GameMap map, String fileName, boolean allowOverwrite) {
		// pluginがnullの場合は初期化前としてIllegalStateException
		if (plugin == null) {
			throw new IllegalStateException("\"plugin\" field is not initialized yet.");
		}

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
		LocationLoader.setLocation(dataYaml, map.getRedSpawn(), RED_SPAWN_KEY);
		LocationLoader.setLocation(dataYaml, map.getBlueSpawn(), BLUE_SPAWN_KEY);

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

	// プレイヤーに表示するマップ名のキー
	private static final String MAP_NAME_KEY = "MapName";
	// ワールド名のキー
	private static final String WORLD_NAME_KEY = "World";
	// 赤スポーンのキー
	private static final String RED_SPAWN_KEY = "SpawnPoint.Red";
	// 青スポーンのキー
	private static final String BLUE_SPAWN_KEY = "SpawnPoint.Blue";
}

package net.azisaba.lgw.core.maps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.Preconditions;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.teams.BattleTeam;
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
		Preconditions.checkState(plugin != null, "\"plugin\" field is not initialized yet.");

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
			HashMap<BattleTeam, Location> spawnMap = new HashMap<>();

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

			// スポーン地点のキーを全て読み込む
			// 何も指定されていない場合はスキップ
			if (yamlData.getConfigurationSection(SPAWN_SECTION_KEY) != null) {

				for (String key : yamlData.getConfigurationSection(SPAWN_SECTION_KEY).getKeys(false)) {

					BattleTeam team;
					// キーがBattleTeamに含まれていない場合はcontinue
					try {
						team = BattleTeam.valueOf(key.toUpperCase());
					} catch (Exception e) {
						continue;
					}

					// 座標をパース
					Location loc = LocationLoader.getLocation(yamlData, SPAWN_SECTION_KEY + "." + key);

					// nullチェック
					if (loc == null) {
						continue;
					}

					// 追加
					spawnMap.put(team, loc);
				}
			}

			// 正常に読み込めていない値がある場合はcontinue
			if (!successLoad)
				continue;

			// GameMap作成
			GameMap data = new GameMap(mapName, world, spawnMap);
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
		Preconditions.checkState(plugin != null, "\"plugin\" field is not initialized yet.");

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

		for (BattleTeam team : BattleTeam.values()) {
			// 座標を取得してnullならcontinue
			if (map.getSpawnPoint(team) == null) {
				continue;
			}

			// 座標保存
			LocationLoader.setLocation(dataYaml, map.getSpawnPoint(team), SPAWN_SECTION_KEY + "." + team.name());
		}

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
	// スポーン地点のキー
	private static final String SPAWN_SECTION_KEY = "SpawnPoint";
}

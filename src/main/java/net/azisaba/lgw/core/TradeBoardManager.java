package net.azisaba.lgw.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import net.azisaba.lgw.core.util.SignData;

public class TradeBoardManager {

    // 看板の情報を保存するフォルダ
    private File dataFolder;
    // 座標に対応する看板データを保存するMap
    private final Map<Location, SignData> signs = new HashMap<>();

    /**
     * 保存されている看板の情報をファイルからロードします。
     */
    protected void init() {
        // dataFolderを取得
        dataFolder = new File(LeonGunWar.getPlugin().getDataFolder(), "Signs");

        // フォルダーが存在しない場合はマップデータが0なのでreturn
        if ( !dataFolder.exists() ) {
            return;
        }

        // ファイルであり最後が.ymlか.yamlで終わるファイルのみ読み込む
        Arrays.asList(dataFolder.listFiles()).stream()
                .filter(file -> file.isFile() && (file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")))
                .forEach(file -> {

                    // ファイル名から座標を読み込む
                    String locStr = file.getName().substring(0, file.getName().length() - 4);
                    Location loc = locationFromString(locStr);

                    // ロードできなかった場合はログを出してreturn
                    if ( loc == null ) {
                        Bukkit.getLogger().warning("Error trying parsing location \"" + file.getName() + "\"");
                        return;
                    }

                    // YamlConfigurationでロード
                    YamlConfiguration conf = YamlConfiguration.loadConfiguration(file);

                    // 各情報を取得
                    long expire = conf.getLong("Expire", 0L);
                    String playerName = conf.getString("PlayerName", null);
                    String uuidStr = conf.getString("UUID", null);
                    UUID uuid = null;
                    try {
                        uuid = UUID.fromString(uuidStr);
                    } catch ( Exception ex ) {
                        // pass
                    }

                    // 人に読みやすい形式に変更
                    locStr = loc.getWorld().getName() + " - " + loc.toVector().toBlockVector();

                    // uuidもplayerNameもnullの場合return
                    if ( uuid != null && playerName != null ) {
                        // インスタンス作成
                        SignData data = new SignData(loc, playerName, uuid, expire);
                        // signsに追加
                        signs.put(loc, data);

                        // ログを出力
                        LeonGunWar.getPlugin().getLogger().fine(locStr + " の看板をロードしました。");
                    } else {
                        // 失敗したログを出力
                        LeonGunWar.getPlugin().getLogger().warning(locStr + " の看板はロードされませんでした。");
                    }
                });

        LeonGunWar.getPlugin().getLogger().info(signs.size() + " 個の看板をロードしました。");
    }

    /**
     * 看板の情報を取得するメソッド
     * 
     * @param loc 取得したい座標の看板
     * @return 看板の情報。なければnullを返す
     */
    public SignData getSignData(Location loc) {
        return signs.getOrDefault(loc, null);
    }

    /**
     * 登録されている看板を削除します
     * 
     * @param loc 削除したい看板の座標
     */
    public void removeSignData(Location loc) {
        if ( signs.containsKey(loc) ) {
            signs.remove(loc);
        }
    }

    /**
     * 看板を追加するメソッド。 signsフィールドに追加され、Pluginのunload時にセーブされます。
     *
     * @param loc        看板の座標
     * @param authorName 看板を設置したプレイヤーの名前
     * @param authorUUID 作成したプレイヤーのUUID
     * @param breakAt    有効期限が切れるミリ秒
     *
     * @return 成功したらtrue、すでに存在している場合はfalse
     */
    public boolean addSignData(Location loc, String authorName, UUID authorUUID, long breakAt) {
        // すでに登録されていたらfalseを返す
        if ( signs.containsKey(loc) ) {
            return false;
        }

        // インスタンス作成
        SignData sign = new SignData(loc, authorName, authorUUID, breakAt);
        // 登録
        signs.put(loc, sign);
        return true;
    }

    /**
     * 全ての登録済みの看板を取得します
     * 
     * @return 登録済みの看板のList
     */
    public List<SignData> getAllSignData() {
        return new ArrayList<>(signs.values());
    }

    /**
     * 現在登録されている看板のデータをセーブします
     */
    protected void saveAll() {

        // フォルダが存在しない場合は作成
        if ( !dataFolder.exists() ) {
            dataFolder.mkdirs();
        }

        // 現在存在している.ymlファイルもしくは.yamlファイルを削除
        Arrays.asList(dataFolder.listFiles()).stream()
                .filter(file -> file.getName().endsWith(".yml") || file.getName().endsWith(".yaml")).forEach(file -> {
                    file.delete();
                });

        // 各看板データを保存
        signs.values().forEach(sign -> {

            // ファイル名ともなる座標を取得
            Location loc = sign.getLocation();

            // YamlConfiguration作成
            YamlConfiguration conf = new YamlConfiguration();

            // 看板の情報をセット
            conf.set("PlayerName", sign.getPlayerName());
            conf.set("UUID", sign.getAuthor().toString());
            conf.set("Expire", sign.getBreakAt());

            // ファイルを取得
            File file = locationToFile(loc);
            // セーブ
            try {
                conf.save(file);
            } catch ( IOException ex ) {
                // 失敗したらエラーを出力する
                ex.printStackTrace();
            }
        });
    }

    /**
     * String形式に変換された座標をLocaitonのインスタンスに戻します
     * 
     * @param str 戻したいString
     * @return 変換されたLocation
     */
    private Location locationFromString(String str) {
        String[] split = str.split(",");
        Location loc = null;
        try {
            World world = Bukkit.getWorld(split[0]);
            loc = new Location(world, Integer.parseInt(split[1]), Integer.parseInt(split[2]),
                    Integer.parseInt(split[3]));
        } catch ( Exception ex ) {
            return null;
        }

        return loc;
    }

    /**
     * 座標をカンマ(,)で区切ってStringに変換し、その名前のファイルを取得します
     * 
     * @param loc 変換したい座標
     * @return 変換されたFile
     */
    private File locationToFile(Location loc) {
        return new File(dataFolder,
                loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ()
                        + ".yml");
    }
}

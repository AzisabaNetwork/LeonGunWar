package net.azisaba.lgw.core.MySQL;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import org.bukkit.entity.Player;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.LevelingConfig;
import net.azisaba.lgw.core.util.PlayerStats;

import lombok.NonNull;

public class SQLPlayerStats {

    private final LeonGunWar plugin = LeonGunWar.getPlugin();
    private final LevelingConfig config = plugin.getLevelingConfig();

    public SQLPlayerStats(){
    }

    public void createTable(){

        PreparedStatement ps;
        try{

            ps = plugin.sql.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS PlayerStats "
                    + "(UUID VARCHAR(64) NOT NULL ,NAME VARCHAR(36) NOT NULL," +
                    "level INT DEFAULT 1, " +
                    "xps INT DEFAULT 0 ," +
                    "coins INT DEFAULT 0 ," +
                    "wins INT DEFAULT 0," +
                    "loses INT DEFAULT 0," +
                    "angelOfDeathLevel INT DEFAULT 1, " +
                    ")");

            ps.executeUpdate();

        }catch ( SQLException e){e.printStackTrace();}

    }

    public void create(@NonNull PlayerStats stats){

        if(exist(stats.getUUID()))
            return;

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("INSERT INTO PlayerStats SET (UUID ,NAME ,level ,xps ,coins ,wins ,loses,angelOfDeathLevel) VALUES (?,?,?,?,?,?,?,?)");
            ps.setString(1,stats.getUUID().toString());
            ps.setString(2,stats.getName());
            ps.setInt(3,stats.getLevel());
            ps.setInt(4,stats.getXps());
            ps.setInt(5,stats.getCoins());
            ps.setInt(6,stats.getWins());
            ps.setInt(7,stats.getLoses());
            ps.setInt(8,stats.getAngelOfDeathLevel());

            ps.executeUpdate();
            ps.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    public PlayerStats getStats(UUID uuid){

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT * FROM PlayerStats WHERE UUID=?");
            ps.setString(1,uuid.toString());

            ResultSet result = ps.executeQuery();

            if(result.next()){

                UUID uuid1 = UUID.fromString(result.getString("UUID"));
                String name = result.getString("NAME");
                int level = result.getInt("level");
                int xps = result.getInt("xps");
                int coins = result.getInt("coins");
                int wins = result.getInt("wins");
                int loses = result.getInt("loses");
                int angelOfDeathLevel = result.getInt("angelOfDeathLevel");

                return new PlayerStats(uuid1,name,level,xps,coins,wins,loses,angelOfDeathLevel);
            }

            return null;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;

    }

    public boolean exist(UUID uuid){

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT * FROM PlayerStats WHERE UUID=?");
            ps.setString(1,uuid.toString());

            ResultSet result = ps.executeQuery();

            if(result.next()){
                return true;
            }

            return false;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;

    }

    public int getLevel(@NonNull UUID uuid) {

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT level FROM PlayerStats WHERE UUID=?");
            ps.setString(1, uuid.toString());

            ResultSet result = ps.executeQuery();

            if(result.next()){
                return result.getInt(1);
            }

            ps.close();

            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;

    }

    public int getXp(@NonNull UUID uuid) {

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("SELECT xps FROM PlayerStats WHERE UUID=?");
            ps.setString(1, uuid.toString());

            ResultSet result = ps.executeQuery();

            if(result.next()){
                return result.getInt(1);
            }

            ps.close();

            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;

    }

    public boolean update(@NonNull PlayerStats stats){

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("UPDATE PlayerStats SET NAME=? ,level=? ,xps=? ,coins=? ,wins=? ,loses=? ,angelOfDeathLevel=? WHERE UUID=?");
            ps.setString(8,stats.getUUID().toString());
            ps.setString(1,stats.getName());
            ps.setInt(2,stats.getLevel());
            ps.setInt(3,stats.getXps());
            ps.setInt(4,stats.getCoins());
            ps.setInt(5,stats.getWins());
            ps.setInt(6,stats.getLoses());
            ps.setInt(7,stats.getAngelOfDeathLevel());

            ps.executeUpdate();
            ps.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

    }

    public int updateXP(@NonNull UUID uuid, int change_value) {
        try {
            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("UPDATE PlayerStats SET xps = xps + {?} WHERE UUID = {?}");
            ps.setInt(1, change_value);
            ps.setString(2, uuid.toString());

            ResultSet result = ps.executeQuery();

            if(result.next()){
                return result.getInt(1);
            }

            ps.close();

            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void updateKilled(@NonNull UUID uuid) { // TODO voidで良いのかなぁ
        int increment;
        if(true) { // TODO 経験値倍増ゲーム中の場合はTrue
            increment = ((int) config.configmap.get("feverXP")) * 2; // TODO ここにKD（今は例として2にしてる）による計算で得られるXP増加量
        } else { // 通常モード
            increment = (int) config.configmap.get("killXP");
        }
        updateXP(uuid, increment);
    }

    public void updateMatchWins(@NonNull UUID uuid) { // 試合で勝った時
        updateXP(uuid, (int) config.configmap.get("winXP"));
    }

    public void updateMVP(@NonNull UUID uuid) { // MVPの時
        updateXP(uuid, (int) config.configmap.get("mvpXP"));
    }

    // TODO ガチャ当たった時
    // TODO 釣りで魚釣れた時
    // TODO アスレクリアの時
}

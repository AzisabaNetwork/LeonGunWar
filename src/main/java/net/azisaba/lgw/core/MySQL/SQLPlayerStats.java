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

    public SQLPlayerStats(){ }

    public void createTable(){

        PreparedStatement ps;
        try{

            ps = plugin.sql.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS PlayerStats "
                    + "(UUID VARCHAR(64) NOT NULL ,NAME VARCHAR(36) NOT NULL," +
                    "level INT DEFAULT 1, " +
                    "xps INT DEFAULT 0 ," +
                    "coins INT DEFAULT 0 ," +
                    "shard INT DEFAULT 0 ," +
                    "crystal INT DEFAULT 0 ," +
                    "wins INT DEFAULT 0," +
                    "loses INT DEFAULT 0," +
                    "angelOfDeathLevel INT DEFAULT 0, " +
                    "yobi3 INT DEFAULT 0 ," +
                    ")");

            ps.executeUpdate();

        }catch ( SQLException e){e.printStackTrace();}

    }

    public void create(@NonNull PlayerStats stats){

        if(exist(stats.getUUID()))
            return;

        try {

            PreparedStatement ps = plugin.sql.getConnection().prepareStatement("INSERT INTO PlayerStats SET (UUID ,NAME ,level ,xps ,coins ,shard ,crystal ,wins ,loses,angelOfDeathLevel ,yobi3) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1,stats.getUUID().toString());
            ps.setString(2,stats.getName());
            ps.setInt(3,stats.getLevel());
            ps.setInt(4,stats.getXps());
            ps.setInt(5,stats.getCoins());
            ps.setInt(6,stats.getShard());
            ps.setInt(7,stats.getCrystal());
            ps.setInt(8,stats.getWins());
            ps.setInt(9,stats.getLoses());
            ps.setInt(10,stats.getAngelOfDeathLevel());
            ps.setInt(11,stats.getShard());//は？

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
                int shard = result.getInt("shard");
                int crystal = result.getInt("crystal");
                int wins = result.getInt("wins");
                int loses = result.getInt("loses");
                int angelOfDeathLevel = result.getInt("angelOfDeathLevel");
                int yobi3 = result.getInt("yobi3");

                return new PlayerStats(uuid1,name,level,xps,coins,shard,crystal,wins,loses,angelOfDeathLevel,yobi3);
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

    // TODO ガチャ当たった時
    // TODO 釣りで魚釣れた時
    // TODO アスレクリアの時
}

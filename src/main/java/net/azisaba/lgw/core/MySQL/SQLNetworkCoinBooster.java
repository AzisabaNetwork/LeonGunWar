package net.azisaba.lgw.core.MySQL;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.azisaba.lgw.core.LeonGunWar;

public class SQLNetworkCoinBooster {

    private final LeonGunWar plugin = LeonGunWar.getPlugin();

    public void createTable(){

        PreparedStatement ps;

        try{

            ps = plugin.sql.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS NetworkCoinBooster "
                    + "(id VARCHAR(64) NOT NULL ,UUID VARCHAR(64) NOT NULL ,NAME VARCHAR(32) NOT NULL," +
                    "type VARCHAR(32), " +
                    "start BIGINT DEFAULT -1, " +
                    "end BIGINT DEFAULT -1, " +
                    "enabled BOOLEAN DEFAULT false, " +
                    "bought BIGINT DEFAULT 0, " +
                    "note VARCHAR(128)" +
                    ")");

            ps.executeUpdate();

        }catch ( SQLException e){e.printStackTrace();}

    }

    public void update(){

        try {

            PreparedStatement ps1 = plugin.sql.getConnection().prepareStatement("SELECT id FROM PlayerStats WHERE enabled=true");

            ResultSet result1 = ps1.executeQuery();

            while (result1.next()){

                PreparedStatement ps = plugin.sql.getConnection().prepareStatement("UPDATE PlayerStats SET enabled=? WHERE id=?");

                ps.setBoolean(1,false);

            }

            PreparedStatement ps2 = plugin.sql.getConnection().prepareStatement("SELECT id FROM PlayerStats WHERE start=-1");

            ResultSet result2 = ps2.executeQuery();

            if(result2.next()){

            }

            ps2.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

}

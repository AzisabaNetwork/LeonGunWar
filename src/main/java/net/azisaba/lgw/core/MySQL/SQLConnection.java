package net.azisaba.lgw.core.MySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import net.azisaba.lgw.core.LeonGunWar;

public class SQLConnection {

    private static Connection connection;

    private final String host = LeonGunWar.getPlugin().getConfig().getString("host");
    private final String port = LeonGunWar.getPlugin().getConfig().getString("port");
    private final String database = LeonGunWar.getPlugin().getConfig().getString("database");
    private final String user = LeonGunWar.getPlugin().getConfig().getString("username");
    private final String password = LeonGunWar.getPlugin().getConfig().getString("password");

    public void connect() throws ClassNotFoundException , SQLException {

        if(!isConnected())
            connection = DriverManager.getConnection("jdbc:mysql://" + host +":"+ port + "/" + database + "?useSLL=false",user,password );

    }

    public boolean isConnected(){
        return (connection != null);
    }

    public void close(){

        if(isConnected()){

            try{

                connection.close();

            }catch (SQLException e){ e.printStackTrace();}

        }

    }

    public Connection getConnection(){ return connection; }

}

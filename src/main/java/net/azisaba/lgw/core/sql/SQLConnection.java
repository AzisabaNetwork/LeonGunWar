package net.azisaba.lgw.core.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.lgw.core.configs.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLConnection {

    private final boolean enabled;

    private static final int CURRENT_DATABASE_VERSION = 1;

    private static final String SELECT_DATABASE_VERSION = "SELECT value FROM settings WHERE tag='database_version'";
    private static final String INSERT_DATABASE_VERSION = "INSERT INTO settings (tag,value) VALUES ('database_version',?)";

    private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS settings (tag VARCHAR(64) NOT NULL, value INT DEFAULT 0)";
    private static final String CREATE_SYOGO_TABLE = "CREATE TABLE IF NOT EXISTS syogos (uuid VARCHAR(36) NOT NULL PRIMARY KEY, name VARCHAR(32) NOT NULL, syogo VARCHAR(64) NOT NULL)";

    private HikariDataSource dataSource;

    public SQLConnection(DatabaseConfig config){

        if(!config.isEnabled()){
            enabled = false;
            return;
        }

        HikariConfig hConfig = new HikariConfig();

        hConfig.setJdbcUrl("jdbc:mysql://" + config.getHost() + ":" + config.getPort() + "/" + config.getDatabase() + "?autoReconnect=true");

        hConfig.setUsername(config.getUser());
        hConfig.setPassword(config.getPassword());

        hConfig.addDataSourceProperty("cachePrepStmts", "true");
        hConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hConfig.addDataSourceProperty("useServerPrepStmts", "true");
        hConfig.addDataSourceProperty("useLocalSessionState", "true");
        hConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hConfig.addDataSourceProperty("cacheResultSetMetadata", "true");
        hConfig.addDataSourceProperty("cacheServerConfiguration", "true");
        hConfig.addDataSourceProperty("elideSetAutoCommits", "true");
        hConfig.addDataSourceProperty("maintainTimeStats", "false");

        this.dataSource = new HikariDataSource(hConfig);

        this.init();
        try {
            this.update();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        enabled = true;

    }

    public void init(){
        try (Connection con = dataSource.getConnection()) {

            //テーブルのバージョン
            try (PreparedStatement statement = con.prepareStatement(CREATE_VERSION_TABLE)) {
                statement.execute();
            }

            //そのた
            try (PreparedStatement statement = con.prepareStatement(CREATE_SYOGO_TABLE)) {
                statement.execute();
            }

        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public void update() throws SQLException {
        try(ResultSet s = this.executeQuery(SELECT_DATABASE_VERSION)){
            int version = s.next() ? s.getInt(1) : -1;
            if(version > CURRENT_DATABASE_VERSION){
                throw new IllegalStateException("Invalid database version");
            }
            if(version < CURRENT_DATABASE_VERSION){
                if(version == -1){
                    executeUpdate(INSERT_DATABASE_VERSION,CURRENT_DATABASE_VERSION);
                    //return;
                }
                //処理
            }
        }
    }

    public void onDisable(){
        this.dataSource.close();
    }

    public ResultSet executeQuery(String query, Object... args){
        try (Connection con = dataSource.getConnection()){
            PreparedStatement statement = con.prepareStatement(query);
            for (int index = 0; index < args.length; index++) {
                statement.setObject(index + 1,args[index]);
            }
            return statement.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean executeUpdate(String query, Object... args){
        try (Connection con = dataSource.getConnection()){
            try(PreparedStatement statement = con.prepareStatement(query)){
                for (int index = 0; index < args.length; index++) {
                    statement.setObject(index + 1,args[index]);
                }
                return statement.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}

package net.azisaba.lgw.core.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.lgw.core.api.config.LGWConfig;
import net.azisaba.lgw.core.api.emblem.db.EmblemDataRepository;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Database {
    protected final HikariDataSource hikariDataSource;
    protected final Jdbi jdbi;
    public final EmblemDataRepository emblemDataRepository;

    // Todo: implement this when support new config
    public Database(LGWConfig.DatabaseConfig databaseConfig) {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mariadb://%s:%d/%s", databaseConfig.host, databaseConfig.port, databaseConfig.database));
        hikariConfig.setUsername(databaseConfig.username);
        hikariConfig.setPassword(databaseConfig.password);
        hikariDataSource = new HikariDataSource(hikariConfig);

        jdbi = Jdbi.create(hikariDataSource);
        jdbi.installPlugin(new SqlObjectPlugin());
        emblemDataRepository = jdbi.onDemand(EmblemDataRepository.class);

        migrate();
    }

    public void shutdown() {
        hikariDataSource.close();
    }

    public void migrate() {
        jdbi.useHandle(handle ->
                handle.execute("""
                        CREATE TABLE IF NOT EXISTS syogos (
                            uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                            name VARCHAR(32) NOT NULL,
                            syogo VARCHAR(64) NOT NULL
                        )
                        """));
    }
}

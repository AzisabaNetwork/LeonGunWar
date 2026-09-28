package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.sql.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** The selection store is shared by lobby and match servers. Permissions remain in LuckPerms. */
public final class KillActionStore {
    public record Selection(String soundId, String logId, String particleId) {
        public static Selection empty() { return new Selection(null, null, null); }
        public String get(KillActionCatalog.Category category) {
            return switch (category) {
                case SOUND -> soundId;
                case LOG -> logId;
                case PARTICLE -> particleId;
            };
        }
        public Selection with(KillActionCatalog.Category category, String id) {
            return switch (category) {
                case SOUND -> new Selection(id, logId, particleId);
                case LOG -> new Selection(soundId, id, particleId);
                case PARTICLE -> new Selection(soundId, logId, id);
            };
        }
    }

    private static final String CREATE = "CREATE TABLE IF NOT EXISTS killaction_selections ("
            + "uuid CHAR(36) NOT NULL PRIMARY KEY, sound_id VARCHAR(64) NULL, "
            + "log_id VARCHAR(64) NULL, particle_id VARCHAR(64) NULL)";
    private static final String SELECT = "SELECT sound_id, log_id, particle_id FROM killaction_selections WHERE uuid=?";
    private final LeonGunWar plugin;
    private final SQLConnection sql;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "LGW-KillAction-DB");
        thread.setDaemon(true);
        return thread;
    });
    private boolean initialized;

    public KillActionStore(LeonGunWar plugin, SQLConnection sql) {
        this.plugin = plugin;
        this.sql = sql;
        if (sql.isEnabled()) executor.execute(() -> {
            try (Connection con = sql.openConnection()) { ensureTable(con); }
            catch (SQLException ex) {
                plugin.getLogger().warning("KillAction selection storage unavailable: " + ex.getMessage());
            }
        });
    }

    public CompletableFuture<Selection> load(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = sql.openConnection()) {
                ensureTable(con);
                return readSelection(con, uuid);
            } catch (SQLException ex) { throw new IllegalStateException("Could not load KillAction selection", ex); }
        }, executor);
    }

    public CompletableFuture<Selection> save(UUID uuid, KillActionCatalog.Category category, String id) {
        return CompletableFuture.supplyAsync(() -> {
            String column = switch (category) {
                case SOUND -> "sound_id";
                case LOG -> "log_id";
                case PARTICLE -> "particle_id";
            };
            String query = "INSERT INTO killaction_selections (uuid," + column + ") VALUES (?,?) "
                    + "ON DUPLICATE KEY UPDATE " + column + "=VALUES(" + column + ")";
            try (Connection con = sql.openConnection()) {
                ensureTable(con);
                try (PreparedStatement statement = con.prepareStatement(query)) {
                    statement.setString(1, uuid.toString());
                    if (id == null) statement.setNull(2, Types.VARCHAR);
                    else statement.setString(2, id);
                    statement.executeUpdate();
                }
                return readSelection(con, uuid);
            } catch (SQLException ex) { throw new IllegalStateException("Could not save KillAction selection", ex); }
        }, executor);
    }

    private void ensureTable(Connection con) throws SQLException {
        if (initialized) return;
        try (PreparedStatement statement = con.prepareStatement(CREATE)) { statement.execute(); }
        initialized = true;
    }

    private static Selection readSelection(Connection con, UUID uuid) throws SQLException {
        try (PreparedStatement statement = con.prepareStatement(SELECT)) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return new Selection(result.getString(1), result.getString(2), result.getString(3));
                return Selection.empty();
            }
        }
    }

    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow();
        } catch (InterruptedException ex) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

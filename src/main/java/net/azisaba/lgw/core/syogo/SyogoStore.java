package net.azisaba.lgw.core.syogo;

import net.azisaba.lgw.core.sql.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Shared ownership, equipped title and server-issued redemption tickets. */
public final class SyogoStore implements AutoCloseable {
    public record Snapshot(Set<String> owned, String selected) {
        public Snapshot { owned = Set.copyOf(owned); }
        public static Snapshot empty() { return new Snapshot(Set.of(), null); }
        public String activeId() { return owned.contains(selected == null ? "" : selected) ? selected : null; }
    }
    public enum Status { OK, ALREADY_OWNED, NOT_OWNED, INVALID_TICKET }
    public record Result(Status status, Snapshot snapshot) { }
    @FunctionalInterface interface Connections { Connection open() throws SQLException; }
    @FunctionalInterface private interface Work<T> { T run(Connection con) throws SQLException; }
    private final Connections connections;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "LGW-Syogo-DB");
        thread.setDaemon(true);
        return thread;
    });
    private boolean initialized;

    public SyogoStore(SQLConnection sql) { this(sql::openConnection); }
    SyogoStore(Connections connections) { this.connections = connections; }

    private void ensureTables(Connection con) throws SQLException {
        if (initialized) return;
        update(con, "CREATE TABLE IF NOT EXISTS syogo_profiles (uuid CHAR(36) NOT NULL PRIMARY KEY, "
                + "name VARCHAR(64) NOT NULL, selected_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL, migrated BOOLEAN NOT NULL DEFAULT FALSE) ENGINE=InnoDB");
        update(con, "CREATE TABLE IF NOT EXISTS syogo_owned (uuid CHAR(36) NOT NULL, "
                + "syogo_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, PRIMARY KEY(uuid,syogo_id)) ENGINE=InnoDB");
        update(con, "CREATE TABLE IF NOT EXISTS syogo_tickets (token CHAR(36) NOT NULL PRIMARY KEY, "
                + "syogo_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, remaining INT NOT NULL) ENGINE=InnoDB");
        initialized = true;
    }

    /** Locks one profile to serialize grants, selection and migration across servers. */
    private void profile(Connection con, UUID uuid, String name) throws SQLException {
        String key = uuid.toString();
        update(con, "INSERT INTO syogo_profiles(uuid,name) VALUES (?,?) ON DUPLICATE KEY UPDATE name=VALUES(name)", key, name);
        try (PreparedStatement statement = prepare(con, "SELECT migrated FROM syogo_profiles WHERE uuid=? FOR UPDATE", key);
             ResultSet result = statement.executeQuery()) {
            if (!result.next()) throw new SQLException("Missing title profile");
            if (result.getBoolean(1)) return;
        }
        String legacy = null;
        try (PreparedStatement statement = prepare(con, "SELECT syogo FROM syogos WHERE uuid=?", key);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) legacy = result.getString(1);
        }
        if (legacy != null && !legacy.isBlank()) {
            update(con, "INSERT IGNORE INTO syogo_owned(uuid,syogo_id) VALUES (?,?)", key, legacy);
        }
        update(con, "UPDATE syogo_profiles SET selected_id=?,migrated=TRUE WHERE uuid=?", legacy, key);
    }

    private <T> CompletableFuture<T> transaction(UUID uuid, String name, Work<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = connections.open()) {
                ensureTables(con);
                con.setAutoCommit(false);
                try {
                    if (uuid != null) profile(con, uuid, name);
                    T result = work.run(con);
                    con.commit();
                    return result;
                } catch (SQLException | RuntimeException ex) {
                    try { con.rollback(); } catch (SQLException rollback) { ex.addSuppressed(rollback); }
                    throw ex;
                }
            } catch (SQLException ex) { throw new IllegalStateException("Syogo database operation failed", ex); }
        }, executor);
    }

    public CompletableFuture<Snapshot> load(UUID uuid, String name) {
        return transaction(uuid, name, con -> snapshot(con, uuid));
    }

    public CompletableFuture<Result> grant(UUID uuid, String name, String id) {
        return transaction(uuid, name, con -> {
            int changed = update(con, "INSERT IGNORE INTO syogo_owned(uuid,syogo_id) VALUES (?,?)", uuid.toString(), id);
            return new Result(changed == 0 ? Status.ALREADY_OWNED : Status.OK, snapshot(con, uuid));
        });
    }

    public CompletableFuture<Result> select(UUID uuid, String name, String id) {
        return transaction(uuid, name, con -> {
            Snapshot before = snapshot(con, uuid);
            if (id != null && !before.owned().contains(id)) return new Result(Status.NOT_OWNED, before);
            update(con, "UPDATE syogo_profiles SET selected_id=? WHERE uuid=?", id, uuid.toString());
            return new Result(Status.OK, new Snapshot(before.owned(), id));
        });
    }

    public CompletableFuture<Result> revoke(UUID uuid, String name, String id) {
        return transaction(uuid, name, con -> {
            int changed = update(con, "DELETE FROM syogo_owned WHERE uuid=? AND syogo_id=?", uuid.toString(), id);
            update(con, "UPDATE syogo_profiles SET selected_id=NULL WHERE uuid=? AND selected_id=?", uuid.toString(), id);
            return new Result(changed == 0 ? Status.NOT_OWNED : Status.OK, snapshot(con, uuid));
        });
    }

    public CompletableFuture<UUID> issue(String id, int amount) {
        if (amount < 1 || amount > 64) return CompletableFuture.failedFuture(new IllegalArgumentException("amount must be 1..64"));
        return transaction(null, null, con -> {
            UUID token = UUID.randomUUID();
            update(con, "INSERT INTO syogo_tickets(token,syogo_id,remaining) VALUES (?,?,?)", token.toString(), id, amount);
            return token;
        });
    }

    public CompletableFuture<Void> discard(UUID token) {
        return transaction(null, null, con -> {
            update(con, "DELETE FROM syogo_tickets WHERE token=?", token.toString());
            return null;
        });
    }

    /** Redeeming a token and granting the title commit together, never separately. */
    public CompletableFuture<Result> redeem(UUID uuid, String name, UUID token, String id) {
        return transaction(uuid, name, con -> {
            Snapshot before = snapshot(con, uuid);
            if (before.owned().contains(id)) return new Result(Status.ALREADY_OWNED, before);
            try (PreparedStatement statement = prepare(con, "SELECT syogo_id,remaining FROM syogo_tickets WHERE token=? FOR UPDATE", token.toString());
                 ResultSet result = statement.executeQuery()) {
                if (!result.next() || !id.equals(result.getString(1)) || result.getInt(2) <= 0) {
                    return new Result(Status.INVALID_TICKET, before);
                }
            }
            update(con, "UPDATE syogo_tickets SET remaining=remaining-1 WHERE token=?", token.toString());
            update(con, "INSERT INTO syogo_owned(uuid,syogo_id) VALUES (?,?)", uuid.toString(), id);
            Set<String> owned = new HashSet<>(before.owned());
            owned.add(id);
            return new Result(Status.OK, new Snapshot(owned, before.selected()));
        });
    }

    private static Snapshot snapshot(Connection con, UUID uuid) throws SQLException {
        Set<String> owned = new HashSet<>();
        try (PreparedStatement statement = prepare(con, "SELECT syogo_id FROM syogo_owned WHERE uuid=?", uuid.toString());
             ResultSet result = statement.executeQuery()) {
            while (result.next()) owned.add(result.getString(1));
        }
        try (PreparedStatement statement = prepare(con, "SELECT selected_id FROM syogo_profiles WHERE uuid=?", uuid.toString());
             ResultSet result = statement.executeQuery()) {
            return new Snapshot(owned, result.next() ? result.getString(1) : null);
        }
    }

    private static PreparedStatement prepare(Connection con, String query, Object... values) throws SQLException {
        PreparedStatement statement = con.prepareStatement(query);
        try {
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        } catch (SQLException ex) { statement.close(); throw ex; }
    }

    private static int update(Connection con, String query, Object... values) throws SQLException {
        try (PreparedStatement statement = prepare(con, query, values)) { return statement.executeUpdate(); }
    }

    @Override public void close() {
        executor.shutdown();
        try { if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow(); }
        catch (InterruptedException ex) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
    }
}

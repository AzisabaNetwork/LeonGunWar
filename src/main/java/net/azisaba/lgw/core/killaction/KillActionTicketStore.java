package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.sql.SQLConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** Ticket quota and ownership are committed together in LGW's shared database. */
public final class KillActionTicketStore implements AutoCloseable {
    public enum Status { OK, ALREADY_OWNED, INVALID }
    public record Owned(KillActionCatalog.Category category, String id) { }
    public record Result(Status status, Set<Owned> owned) {
        public Result { owned = Set.copyOf(owned); }
    }
    @FunctionalInterface interface Connections { Connection open() throws SQLException; }
    @FunctionalInterface private interface Work<T> { T run(Connection connection) throws SQLException; }
    private final Connections connections;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(task -> {
        Thread thread = new Thread(task, "LGW-KillAction-Tickets-DB");
        thread.setDaemon(true);
        return thread;
    });
    private boolean initialized;

    public KillActionTicketStore(SQLConnection sql) { this(sql::openConnection); }
    KillActionTicketStore(Connections connections) { this.connections = connections; }

    private void ensureTables(Connection con) throws SQLException {
        if (initialized) return;
        update(con, "CREATE TABLE IF NOT EXISTS killaction_tickets (token CHAR(36) NOT NULL PRIMARY KEY, "
                + "category VARCHAR(16) NOT NULL, effect_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, "
                + "remaining INT NOT NULL) ENGINE=InnoDB");
        update(con, "CREATE TABLE IF NOT EXISTS killaction_owned (uuid CHAR(36) NOT NULL, category VARCHAR(16) NOT NULL, "
                + "effect_id VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL, "
                + "PRIMARY KEY(uuid,category,effect_id)) ENGINE=InnoDB");
        initialized = true;
    }

    private <T> CompletableFuture<T> transaction(Work<T> work) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection con = connections.open()) {
                ensureTables(con);
                con.setAutoCommit(false);
                try {
                    T result = work.run(con);
                    con.commit();
                    return result;
                } catch (SQLException | RuntimeException exception) {
                    try { con.rollback(); } catch (SQLException rollback) { exception.addSuppressed(rollback); }
                    throw exception;
                }
            } catch (SQLException exception) { throw new IllegalStateException("KillAction ticket database operation failed", exception); }
        }, executor);
    }

    public CompletableFuture<UUID> issue(KillActionCatalog.Entry entry, int amount) {
        if (amount < 1 || amount > 64) return CompletableFuture.failedFuture(new IllegalArgumentException("amount must be 1..64"));
        return transaction(con -> {
            UUID token = UUID.randomUUID();
            update(con, "INSERT INTO killaction_tickets(token,category,effect_id,remaining) VALUES (?,?,?,?)",
                    token.toString(), entry.category().name(), entry.id(), amount);
            return token;
        });
    }

    public CompletableFuture<Void> discard(UUID token) {
        return transaction(con -> {
            update(con, "DELETE FROM killaction_tickets WHERE token=?", token.toString());
            return null;
        });
    }

    public CompletableFuture<Set<Owned>> loadOwned(UUID uuid) {
        return transaction(con -> Set.copyOf(owned(con, uuid)));
    }

    public CompletableFuture<Result> redeem(UUID uuid, UUID token, KillActionCatalog.Entry entry) {
        return transaction(con -> {
            Set<Owned> owned = owned(con, uuid);
            Owned key = new Owned(entry.category(), entry.id());
            if (owned.contains(key)) return new Result(Status.ALREADY_OWNED, owned);
            // The row lock serializes quota use across all servers.
            try (PreparedStatement statement = prepare(con,
                    "SELECT category,effect_id,remaining FROM killaction_tickets WHERE token=? FOR UPDATE", token.toString());
                 ResultSet result = statement.executeQuery()) {
                if (!result.next() || !entry.category().name().equals(result.getString(1))
                        || !entry.id().equals(result.getString(2)) || result.getInt(3) <= 0) return new Result(Status.INVALID, owned);
            }
            // The unique key also handles simultaneous uses of DIFFERENT tickets by the same UUID.
            int added = update(con, "INSERT IGNORE INTO killaction_owned(uuid,category,effect_id) VALUES (?,?,?)",
                    uuid.toString(), entry.category().name(), entry.id());
            owned.add(key);
            if (added == 0) return new Result(Status.ALREADY_OWNED, owned);
            update(con, "UPDATE killaction_tickets SET remaining=remaining-1 WHERE token=?", token.toString());
            return new Result(Status.OK, owned);
        });
    }

    private static Set<Owned> owned(Connection con, UUID uuid) throws SQLException {
        Set<Owned> owned = new HashSet<>();
        try (PreparedStatement statement = prepare(con, "SELECT category,effect_id FROM killaction_owned WHERE uuid=?", uuid.toString());
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                try { owned.add(new Owned(KillActionCatalog.Category.valueOf(result.getString(1)), result.getString(2))); }
                catch (IllegalArgumentException ignored) { /* Ignore obsolete categories, preserving other ownership. */ }
            }
        }
        return owned;
    }

    private static PreparedStatement prepare(Connection con, String query, Object... values) throws SQLException {
        PreparedStatement statement = con.prepareStatement(query);
        try {
            for (int i = 0; i < values.length; i++) statement.setObject(i + 1, values[i]);
            return statement;
        } catch (SQLException exception) { statement.close(); throw exception; }
    }
    private static int update(Connection con, String query, Object... values) throws SQLException {
        try (PreparedStatement statement = prepare(con, query, values)) { return statement.executeUpdate(); }
    }

    @Override public void close() {
        executor.shutdown();
        try { if (!executor.awaitTermination(2, TimeUnit.SECONDS)) executor.shutdownNow(); }
        catch (InterruptedException exception) { executor.shutdownNow(); Thread.currentThread().interrupt(); }
    }
}

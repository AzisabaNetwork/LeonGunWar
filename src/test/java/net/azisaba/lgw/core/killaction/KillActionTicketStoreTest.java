package net.azisaba.lgw.core.killaction;

import org.junit.jupiter.api.Test;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KillActionTicketStoreTest {
    private static final UUID PLAYER = UUID.randomUUID(), TOKEN = UUID.randomUUID();
    private static final KillActionCatalog.Entry ENTRY = new KillActionCatalog.Entry(KillActionCatalog.Category.SOUND,
            "chime", "チャイム", null, List.of(), KillActionCatalog.Rarity.COMMON, "", "イベント券", null, List.of(), List.of());
    /** Stub JDBC outcomes to verify transaction boundaries; this is not a MySQL integration test. */
    private static final class Jdbc implements AutoCloseable {
        final Connection connection = mock(Connection.class);
        final Map<String, PreparedStatement> statements = new LinkedHashMap<>();
        final List<ResultSet> results = new ArrayList<>();
        final KillActionTicketStore store;
        Set<KillActionTicketStore.Owned> owned = Set.of();
        String category = "SOUND", id = "chime";
        int remaining = 1;
        boolean duplicate, failSpend;
        Jdbc() throws Exception {
            when(connection.prepareStatement(anyString())).thenAnswer(call -> {
                String query = call.getArgument(0);
                PreparedStatement statement = mock(PreparedStatement.class);
                statements.put(query, statement);
                when(statement.executeUpdate()).thenAnswer(ignored -> {
                    if (query.startsWith("UPDATE killaction_tickets") && failSpend) throw new SQLException("failed");
                    return query.startsWith("INSERT IGNORE INTO killaction_owned") && duplicate ? 0 : 1;
                });
                when(statement.executeQuery()).thenAnswer(ignored -> {
                    ResultSet result = mock(ResultSet.class); results.add(result);
                    if (query.startsWith("SELECT category,effect_id FROM killaction_owned")) {
                        var values = new ArrayList<>(owned); int[] index = {-1};
                        when(result.next()).thenAnswer(unused -> ++index[0] < values.size());
                        when(result.getString(1)).thenAnswer(unused -> values.get(index[0]).category().name());
                        when(result.getString(2)).thenAnswer(unused -> values.get(index[0]).id());
                    } else if (query.startsWith("SELECT category,effect_id,remaining")) {
                        when(result.next()).thenReturn(id != null, false);
                        when(result.getString(1)).thenReturn(category);
                        when(result.getString(2)).thenReturn(id);
                        when(result.getInt(3)).thenReturn(remaining);
                    } else throw new AssertionError(query);
                    return result;
                });
                return statement;
            });
            store = new KillActionTicketStore(() -> connection);
        }
        PreparedStatement statement(String prefix) {
            return statements.entrySet().stream().filter(e -> e.getKey().startsWith(prefix)).findFirst().orElseThrow().getValue();
        }
        boolean has(String prefix) { return statements.keySet().stream().anyMatch(q -> q.startsWith(prefix)); }
        KillActionTicketStore.Result redeem() throws Exception { return store.redeem(PLAYER, TOKEN, ENTRY).get(5, TimeUnit.SECONDS); }
        void closed() throws Exception {
            verify(connection).close();
            for (var statement : statements.values()) verify(statement).close();
            for (var result : results) verify(result).close();
        }
        @Override public void close() { store.close(); }
    }
    @Test void grantAndQuotaCommitTogetherWithoutChangingSelectionOrPermissions() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            var result = jdbc.redeem();
            assertEquals(KillActionTicketStore.Status.OK, result.status());
            assertTrue(result.owned().contains(new KillActionTicketStore.Owned(ENTRY.category(), ENTRY.id())));
            var ordered = inOrder(jdbc.connection, jdbc.statement("INSERT IGNORE INTO killaction_owned"), jdbc.statement("UPDATE killaction_tickets"));
            ordered.verify(jdbc.connection).setAutoCommit(false);
            ordered.verify(jdbc.statement("INSERT IGNORE INTO killaction_owned")).executeUpdate();
            ordered.verify(jdbc.statement("UPDATE killaction_tickets")).executeUpdate();
            ordered.verify(jdbc.connection).commit();
            verify(jdbc.statement("INSERT IGNORE INTO killaction_owned")).setObject(2, "SOUND");
            assertFalse(jdbc.has("UPDATE killaction_selections")); jdbc.closed();
        }
    }
    @Test void alreadyOwnedDoesNotSpend() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of(new KillActionTicketStore.Owned(ENTRY.category(), ENTRY.id()));
            assertEquals(KillActionTicketStore.Status.ALREADY_OWNED, jdbc.redeem().status());
            assertFalse(jdbc.has("INSERT IGNORE INTO killaction_owned"));
            assertFalse(jdbc.has("UPDATE killaction_tickets")); jdbc.closed();
        }
    }
    @Test void uniqueOwnershipRaceDoesNotSpendAnotherTicket() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.duplicate = true;
            var result = jdbc.redeem();
            assertEquals(KillActionTicketStore.Status.ALREADY_OWNED, result.status());
            assertTrue(result.owned().contains(new KillActionTicketStore.Owned(ENTRY.category(), ENTRY.id())));
            assertFalse(jdbc.has("UPDATE killaction_tickets")); jdbc.closed();
        }
    }
    @Test void sameIdInAnotherCategoryIsIndependentOwnership() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of(new KillActionTicketStore.Owned(KillActionCatalog.Category.LOG, ENTRY.id()));
            var result = jdbc.redeem();
            assertEquals(KillActionTicketStore.Status.OK, result.status()); assertEquals(2, result.owned().size()); jdbc.closed();
        }
    }
    @Test void missingExhaustedMismatchedCategoryOrIdDoesNotGrant() throws Exception {
        for (int failure = 0; failure < 4; failure++) try (Jdbc jdbc = new Jdbc()) {
            switch (failure) { case 0 -> jdbc.id = null; case 1 -> jdbc.remaining = 0;
                case 2 -> jdbc.category = "PARTICLE"; case 3 -> jdbc.id = "other"; }
            assertEquals(KillActionTicketStore.Status.INVALID, jdbc.redeem().status());
            assertFalse(jdbc.has("INSERT IGNORE INTO killaction_owned")); assertFalse(jdbc.has("UPDATE killaction_tickets")); jdbc.closed();
        }
    }
    @Test void spendFailureRollsBackOwnership() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.failSpend = true;
            assertThrows(ExecutionException.class, jdbc::redeem);
            verify(jdbc.connection).rollback(); verify(jdbc.connection, never()).commit(); jdbc.closed();
        }
    }
    @Test void ownershipLoadIsImmutableAndUsesUuid() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of(new KillActionTicketStore.Owned(ENTRY.category(), ENTRY.id()));
            var result = jdbc.store.loadOwned(PLAYER).get(5, TimeUnit.SECONDS);
            assertEquals(jdbc.owned, result); assertThrows(UnsupportedOperationException.class, result::clear);
            verify(jdbc.statement("SELECT category,effect_id FROM killaction_owned")).setObject(1, PLAYER.toString()); jdbc.closed();
        }
    }
    @Test void issuanceStoresQuotaAndCategoryWithoutPermission() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            assertNotNull(jdbc.store.issue(ENTRY, 16).get(5, TimeUnit.SECONDS));
            var statement = jdbc.statement("INSERT INTO killaction_tickets");
            verify(statement).setObject(2, "SOUND"); verify(statement).setObject(3, "chime"); verify(statement).setObject(4, 16);
            jdbc.closed();
        }
    }
}

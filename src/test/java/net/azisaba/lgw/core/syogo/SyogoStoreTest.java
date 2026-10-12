package net.azisaba.lgw.core.syogo;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SyogoStoreTest {
    private static final UUID PLAYER = UUID.fromString("8dcf21f1-4395-47c8-b4fa-7376ee9768fd");
    /** JDBC boundary fixtures: outcomes are supplied by each test, not a simulated SQL engine. */
    private static final class Jdbc implements AutoCloseable {
        final Connection connection = mock(Connection.class);
        final List<String> queries = new ArrayList<>();
        final List<PreparedStatement> statements = new ArrayList<>();
        final List<ResultSet> results = new ArrayList<>();
        final SyogoStore store;
        Set<String> owned = Set.of();
        String selected;
        boolean migrated = true;
        String legacy;
        String ticketId = "champion";
        int remaining = 1;
        boolean duplicate;
        boolean failGrant;
        Jdbc() throws Exception {
            when(connection.prepareStatement(anyString())).thenAnswer(invocation -> {
                String query = invocation.getArgument(0);
                queries.add(query);
                PreparedStatement statement = mock(PreparedStatement.class);
                statements.add(statement);
                when(statement.executeUpdate()).thenAnswer(ignored -> {
                    if (failGrant && query.startsWith("INSERT INTO syogo_owned")) throw new SQLException("storage failed");
                    return duplicate && query.startsWith("INSERT IGNORE INTO syogo_owned") ? 0 : 1;
                });
                when(statement.executeQuery()).thenAnswer(ignored -> {
                    ResultSet result = mock(ResultSet.class);
                    results.add(result);
                    if (query.startsWith("SELECT migrated")) {
                        when(result.next()).thenReturn(true, false);
                        when(result.getBoolean(1)).thenReturn(migrated);
                    } else if (query.startsWith("SELECT syogo_id FROM syogo_owned")) {
                        List<String> ids = owned.stream().sorted().toList();
                        int[] index = {-1};
                        when(result.next()).thenAnswer(unused -> ++index[0] < ids.size());
                        when(result.getString(1)).thenAnswer(unused -> ids.get(index[0]));
                    } else if (query.startsWith("SELECT selected_id")) {
                        when(result.next()).thenReturn(true, false);
                        when(result.getString(1)).thenReturn(selected);
                    } else if (query.startsWith("SELECT syogo FROM syogos")) {
                        when(result.next()).thenReturn(legacy != null, false);
                        when(result.getString(1)).thenReturn(legacy);
                    } else if (query.startsWith("SELECT syogo_id,remaining")) {
                        when(result.next()).thenReturn(ticketId != null, false);
                        when(result.getString(1)).thenReturn(ticketId);
                        when(result.getInt(2)).thenReturn(remaining);
                    } else throw new AssertionError(query);
                    return result;
                });
                return statement;
            });
            store = new SyogoStore(() -> connection);
        }
        PreparedStatement statement(String prefix) {
            for (int i = 0; i < queries.size(); i++) if (queries.get(i).startsWith(prefix)) return statements.get(i);
            fail("Missing query: " + prefix); return null;
        }
        boolean has(String prefix) { return queries.stream().anyMatch(query -> query.startsWith(prefix)); }
        void assertClosed() throws Exception {
            verify(connection, atLeastOnce()).close();
            for (var statement : statements) verify(statement).close();
            for (var result : results) verify(result).close();
        }
        @Override public void close() { store.close(); }
    }

    @Test void legacyTitleBecomesOwnedAndEquippedOnce() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.migrated = false;
            jdbc.legacy = "veteran";
            jdbc.owned = Set.of("veteran");
            jdbc.selected = "veteran";
            var snapshot = jdbc.store.load(PLAYER, "Player").get(5, TimeUnit.SECONDS);
            assertEquals("veteran", snapshot.activeId());
            verify(jdbc.statement("INSERT IGNORE INTO syogo_owned")).setObject(2, "veteran");
            verify(jdbc.statement("UPDATE syogo_profiles SET selected_id=?,migrated=TRUE")).setObject(1, "veteran");
            jdbc.migrated = true;
            jdbc.store.load(PLAYER, "Player").get(5, TimeUnit.SECONDS);
            assertEquals(1, jdbc.queries.stream().filter(q -> q.startsWith("SELECT syogo FROM syogos")).count());
            assertFalse(jdbc.has("DELETE FROM syogos"));
            jdbc.assertClosed();
        }
    }
    @Test void duplicateGrantDoesNotChangeEquippedTitle() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.duplicate = true; jdbc.owned = Set.of("rookie", "champion"); jdbc.selected = "rookie";
            var result = jdbc.store.grant(PLAYER, "Player", "champion").get(5, TimeUnit.SECONDS);
            assertEquals(SyogoStore.Status.ALREADY_OWNED, result.status());
            assertEquals("rookie", result.snapshot().activeId());
            assertFalse(jdbc.has("UPDATE syogo_profiles SET selected_id"));
            verify(jdbc.connection).commit(); jdbc.assertClosed();
        }
    }
    @Test void cannotSelectAnUnownedTitle() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of("rookie"); jdbc.selected = "rookie";
            var result = jdbc.store.select(PLAYER, "Player", "champion").get(5, TimeUnit.SECONDS);
            assertEquals(SyogoStore.Status.NOT_OWNED, result.status());
            assertEquals("rookie", result.snapshot().activeId());
            assertFalse(jdbc.has("UPDATE syogo_profiles SET selected_id")); jdbc.assertClosed();
        }
    }
    @Test void unequipKeepsOwnership() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of("rookie", "champion"); jdbc.selected = "rookie";
            var result = jdbc.store.select(PLAYER, "Player", null).get(5, TimeUnit.SECONDS);
            assertEquals(jdbc.owned, result.snapshot().owned()); assertNull(result.snapshot().activeId());
            verify(jdbc.statement("UPDATE syogo_profiles SET selected_id")).setObject(1, null);
            assertFalse(jdbc.has("DELETE FROM syogo_owned")); jdbc.assertClosed();
        }
    }
    @Test void redeemCommitsTicketAndOwnershipTogetherWithoutEquipping() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of("rookie"); jdbc.selected = "rookie";
            UUID ticket = UUID.randomUUID();
            var result = jdbc.store.redeem(PLAYER, "Player", ticket, "champion").get(5, TimeUnit.SECONDS);
            assertEquals(SyogoStore.Status.OK, result.status());
            assertEquals(Set.of("rookie", "champion"), result.snapshot().owned());
            assertEquals("rookie", result.snapshot().activeId());
            var ordered = inOrder(jdbc.connection, jdbc.statement("UPDATE syogo_tickets"), jdbc.statement("INSERT INTO syogo_owned"));
            ordered.verify(jdbc.connection).setAutoCommit(false);
            ordered.verify(jdbc.statement("UPDATE syogo_tickets")).executeUpdate();
            ordered.verify(jdbc.statement("INSERT INTO syogo_owned")).executeUpdate();
            ordered.verify(jdbc.connection).commit();
            verify(jdbc.statement("UPDATE syogo_tickets")).setObject(1, ticket.toString()); jdbc.assertClosed();
        }
    }
    @Test void alreadyOwnedDoesNotSpendTicket() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.owned = Set.of("champion");
            assertEquals(SyogoStore.Status.ALREADY_OWNED, jdbc.store.redeem(PLAYER, "Player", UUID.randomUUID(), "champion").get(5, TimeUnit.SECONDS).status());
            assertFalse(jdbc.has("UPDATE syogo_tickets")); assertFalse(jdbc.has("INSERT INTO syogo_owned")); jdbc.assertClosed();
        }
    }
    @Test void exhaustedOrMismatchedTicketsCannotGrant() throws Exception {
        for (boolean mismatch : List.of(false, true)) try (Jdbc jdbc = new Jdbc()) {
            if (mismatch) jdbc.ticketId = "another"; else jdbc.remaining = 0;
            assertEquals(SyogoStore.Status.INVALID_TICKET, jdbc.store.redeem(PLAYER, "Player", UUID.randomUUID(), "champion").get(5, TimeUnit.SECONDS).status());
            assertFalse(jdbc.has("UPDATE syogo_tickets")); assertFalse(jdbc.has("INSERT INTO syogo_owned")); jdbc.assertClosed();
        }
    }
    @Test void ownershipFailureRollsBackTicketSpend() throws Exception {
        try (Jdbc jdbc = new Jdbc()) {
            jdbc.failGrant = true;
            assertThrows(ExecutionException.class, () -> jdbc.store.redeem(PLAYER, "Player", UUID.randomUUID(), "champion").get(5, TimeUnit.SECONDS));
            assertTrue(jdbc.has("UPDATE syogo_tickets"));
            verify(jdbc.connection).rollback(); verify(jdbc.connection, never()).commit(); jdbc.assertClosed();
        }
    }
    @Test void forgedSelectedIdIsNeverActiveAndOwnershipIsImmutable() {
        var snapshot = new SyogoStore.Snapshot(Set.of("rookie"), "champion");
        assertNull(snapshot.activeId());
        assertThrows(UnsupportedOperationException.class, () -> snapshot.owned().add("champion"));
    }
}

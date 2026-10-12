package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.Sound;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import java.util.*;
import java.util.function.Consumer;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class KillActionTicketsTest {
    @BeforeAll @SuppressWarnings({"unchecked", "rawtypes"})
    static void initializePaperRegistryBoundary() {
        try (var staticAccess = mockStatic(RegistryAccess.class)) {
            RegistryAccess access = mock(RegistryAccess.class);
            staticAccess.when(RegistryAccess::registryAccess).thenReturn(access);
            when(access.getRegistry(any(Class.class))).thenAnswer(ignored -> mock(Registry.class));
            when(access.getRegistry(any(RegistryKey.class))).thenAnswer(ignored -> mock(Registry.class));
            var ignored = Sound.ENTITY_PLAYER_LEVELUP;
        }
    }
    private static final UUID UUID_PLAYER = UUID.randomUUID(), TOKEN = UUID.randomUUID();
    private static final class Fixture implements AutoCloseable {
        final LeonGunWar plugin = mock(LeonGunWar.class);
        final KillActionManager manager = mock(KillActionManager.class);
        final KillActionTicketStore store = mock(KillActionTicketStore.class);
        final KillActionCatalog catalog = mock(KillActionCatalog.class);
        final KillActionCatalog.Entry entry = mock(KillActionCatalog.Entry.class);
        final Player player = mock(Player.class);
        final PlayerInventory inventory = mock(PlayerInventory.class);
        final ItemStack ticket = mock(ItemStack.class);
        final ItemMeta meta = mock(ItemMeta.class);
        final PersistentDataContainer data = mock(PersistentDataContainer.class);
        final PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        final MainConfig config = mock(MainConfig.class);
        final org.mockito.MockedStatic<Bukkit> bukkit = mockStatic(Bukkit.class);
        final KillActionTickets tickets;
        Consumer<KillActionTicketStore.Result> completion;
        String token = TOKEN.toString(), category = "SOUND", id = "chime";
        @SuppressWarnings("unchecked") Fixture() {
            when(plugin.getName()).thenReturn("LeonGunWar");
            when(plugin.namespace()).thenReturn("leongunwar");
            when(plugin.getLogger()).thenReturn(Logger.getAnonymousLogger());
            config.isLobby = true; when(plugin.getMainConfig()).thenReturn(config);
            when(manager.catalog()).thenReturn(catalog);
            when(catalog.get(KillActionCatalog.Category.SOUND, "chime")).thenReturn(entry);
            when(entry.category()).thenReturn(KillActionCatalog.Category.SOUND);
            when(entry.id()).thenReturn("chime"); when(entry.displayName()).thenReturn("チャイム");
            when(player.getUniqueId()).thenReturn(UUID_PLAYER);
            when(player.getInventory()).thenReturn(inventory);
            when(inventory.getItemInMainHand()).thenReturn(ticket);
            when(inventory.getContents()).thenReturn(new ItemStack[]{ticket});
            when(ticket.hasItemMeta()).thenReturn(true); when(ticket.getItemMeta()).thenReturn(meta);
            when(ticket.getAmount()).thenReturn(2);
            when(meta.getPersistentDataContainer()).thenReturn(data);
            when(data.get(any(NamespacedKey.class), eq(PersistentDataType.STRING))).thenAnswer(call -> switch (((NamespacedKey) call.getArgument(0)).getKey()) {
                case "killaction_ticket" -> token; case "killaction_category" -> category; case "killaction_id" -> id; default -> null;
            });
            when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
            when(event.getHand()).thenReturn(EquipmentSlot.HAND);
            when(event.getPlayer()).thenReturn(player); when(event.getItem()).thenReturn(ticket);
            bukkit.when(() -> Bukkit.getPlayer(UUID_PLAYER)).thenReturn(player);
            doAnswer(call -> { completion = call.getArgument(3); return null; }).when(manager).redeem(eq(player), eq(TOKEN), eq(entry), any());
            tickets = new KillActionTickets(plugin, manager, store);
        }
        void assertNotConsumed() { verify(ticket, never()).setAmount(anyInt()); verify(inventory, never()).setItem(anyInt(), any()); }
        @Override public void close() { bukkit.close(); }
    }
    @Test void onlySuccessfulSaveConsumesOneAndPendingUseCannotRunTwice() {
        try (Fixture f = new Fixture()) {
            f.tickets.use(f.event); f.tickets.use(f.event);
            f.assertNotConsumed(); verify(f.manager, times(1)).redeem(eq(f.player), eq(TOKEN), eq(f.entry), any());
            var click = mock(InventoryClickEvent.class); when(click.getWhoClicked()).thenReturn(f.player);
            f.tickets.click(click); verify(click).setCancelled(true);
            f.completion.accept(new KillActionTicketStore.Result(KillActionTicketStore.Status.OK, Set.of()));
            verify(f.ticket).setAmount(1); verify(f.inventory).setItem(0, f.ticket);
        }
    }
    @Test void failedSaveAlreadyOwnedAndInvalidDoNotConsume() {
        for (var status : Arrays.asList(null, KillActionTicketStore.Status.ALREADY_OWNED, KillActionTicketStore.Status.INVALID)) {
            try (Fixture f = new Fixture()) {
                f.tickets.use(f.event);
                f.completion.accept(status == null ? null : new KillActionTicketStore.Result(status, Set.of()));
                f.assertNotConsumed();
            }
        }
    }
    @Test void matchServerCannotRedeem() {
        try (Fixture f = new Fixture()) {
            f.config.isLobby = false; f.tickets.use(f.event);
            verify(f.manager, never()).redeem(any(), any(), any(), any()); f.assertNotConsumed();
        }
    }
    @Test void removedDefinitionAndMalformedTokenCannotRedeem() {
        for (boolean missing : List.of(true, false)) try (Fixture f = new Fixture()) {
            if (missing) f.id = "removed"; else f.token = "invalid";
            f.tickets.use(f.event); verify(f.manager, never()).redeem(any(), any(), any(), any()); f.assertNotConsumed();
        }
    }
    @Test void loggedOutSessionIsNotMutatedAfterSave() {
        try (Fixture f = new Fixture()) {
            f.tickets.use(f.event); f.bukkit.when(() -> Bukkit.getPlayer(UUID_PLAYER)).thenReturn(null);
            f.completion.accept(new KillActionTicketStore.Result(KillActionTicketStore.Status.OK, Set.of())); f.assertNotConsumed();
        }
    }
    @Test void ordinaryPlayersCannotIssueOrBindTickets() {
        try (Fixture f = new Fixture()) {
            assertTrue(f.tickets.onCommand(f.player, null, "killaction", new String[]{"bind", "sound", "chime"}));
            verifyNoInteractions(f.store);
            assertEquals(List.of(), f.tickets.onTabComplete(f.player, null, "killaction", new String[]{""}));
        }
    }
    @Test void bindKeepsExistingNameAndLoreAndCoversHeldStack() {
        try (Fixture f = new Fixture()) {
            when(f.ticket.getType()).thenReturn(Material.PAPER);
            when(f.ticket.clone()).thenReturn(f.ticket);
            when(f.inventory.getItem(0)).thenReturn(f.ticket);
            when(f.meta.hasLore()).thenReturn(true); when(f.meta.getLore()).thenReturn(List.of("既存説明"));
            when(f.store.issue(f.entry, 2)).thenReturn(CompletableFuture.completedFuture(TOKEN));
            doAnswer(call -> { ((Runnable) call.getArgument(0)).run(); return null; }).when(f.manager).onMain(any());
            f.tickets.issue(f.player, f.player, f.entry, 1, true);
            verify(f.store).issue(f.entry, 2); verify(f.meta, never()).setDisplayName(anyString());
            verify(f.meta).setLore(argThat(lore -> lore.getFirst().equals("既存説明") && lore.size() == 4));
            verify(f.inventory).setItem(0, f.ticket);
            verify(f.data).set(new NamespacedKey(f.plugin, "killaction_ticket"), PersistentDataType.STRING, TOKEN.toString());
        }
    }
    @Test void bindAbortsIfHeldItemChangesDuringIssuance() {
        try (Fixture f = new Fixture()) {
            when(f.ticket.getType()).thenReturn(Material.PAPER); when(f.ticket.clone()).thenReturn(f.ticket);
            var issued = new CompletableFuture<UUID>(); when(f.store.issue(f.entry, 2)).thenReturn(issued);
            when(f.store.discard(TOKEN)).thenReturn(CompletableFuture.completedFuture(null));
            doAnswer(call -> { ((Runnable) call.getArgument(0)).run(); return null; }).when(f.manager).onMain(any());
            f.tickets.issue(f.player, f.player, f.entry, 1, true);
            f.assertNotConsumed(); issued.complete(TOKEN);
            verify(f.store).discard(TOKEN); verify(f.ticket, never()).setItemMeta(any()); f.assertNotConsumed();
        }
    }
}

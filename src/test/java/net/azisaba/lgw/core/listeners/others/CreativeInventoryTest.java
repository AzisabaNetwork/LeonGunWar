package net.azisaba.lgw.core.listeners.others;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import static org.mockito.Mockito.*;

class CreativeInventoryTest {
    @BeforeAll
    @SuppressWarnings({"unchecked", "rawtypes"})
    static void initializeInventoryTypes() {
        // Paper 1.21.11のInventoryTypeはMenuTypeレジストリを参照する。
        // ここではメニュー種別の比較だけを試すため、サーバー側のレジストリ境界をモックする。
        try (var staticAccess = mockStatic(RegistryAccess.class)) {
            RegistryAccess access = mock(RegistryAccess.class);
            staticAccess.when(RegistryAccess::registryAccess).thenReturn(access);
            when(access.getRegistry(any(Class.class))).thenAnswer(ignored -> mock(Registry.class));
            when(access.getRegistry(any(RegistryKey.class))).thenAnswer(ignored -> mock(Registry.class));
            InventoryType.values();
        }
    }

    private Player creativePlayer() {
        Player player = mock(Player.class);
        when(player.getGameMode()).thenReturn(GameMode.CREATIVE);
        return player;
    }

    @Test
    void closingCreativeInventoryLeavesOffhandUntouched() {
        Player player = creativePlayer();
        InventoryCloseEvent event = mock(InventoryCloseEvent.class);
        when(event.getPlayer()).thenReturn(player);
        new DisableOffhandListener().onInventoryClose(event);
        verify(player, never()).getInventory();
    }

    @Test
    void creativePersonalInventoryIsAllowed() {
        Player player = creativePlayer();
        InventoryOpenEvent event = mock(InventoryOpenEvent.class);
        Inventory inventory = mock(Inventory.class);
        when(inventory.getType()).thenReturn(InventoryType.CREATIVE);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getPlayer()).thenReturn(player);
        new DisableOpenInventoryListener().onInventoryOpen(event);
        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void anvilsWorkbenchesAndFurnacesStayBlockedInBothModes() {
        for (GameMode mode : new GameMode[]{GameMode.CREATIVE, GameMode.SURVIVAL}) {
            for (InventoryType type : new InventoryType[]{InventoryType.ANVIL, InventoryType.WORKBENCH, InventoryType.FURNACE}) {
                Player player = mock(Player.class);
                when(player.getGameMode()).thenReturn(mode);
                Inventory inventory = mock(Inventory.class);
                when(inventory.getType()).thenReturn(type);
                InventoryOpenEvent event = mock(InventoryOpenEvent.class);
                when(event.getInventory()).thenReturn(inventory);
                when(event.getPlayer()).thenReturn(player);
                new DisableOpenInventoryListener().onInventoryOpen(event);
                verify(event).setCancelled(true);
            }
        }
    }

    @Test
    void creativeCannotOpenPhysicalChest() {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getType()).thenReturn(InventoryType.CHEST);
        when(inventory.getLocation()).thenReturn(new Location(null, 1, 2, 3));
        Player player = creativePlayer();
        InventoryOpenEvent event = mock(InventoryOpenEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getInventory()).thenReturn(inventory);
        new DisableOpenInventoryListener().onInventoryOpen(event);
        verify(event).setCancelled(true);
    }

    @Test
    void creativeCanStillOpenVirtualPluginGui() {
        Inventory inventory = mock(Inventory.class);
        when(inventory.getType()).thenReturn(InventoryType.CHEST);
        Player player = creativePlayer();
        InventoryOpenEvent event = mock(InventoryOpenEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getInventory()).thenReturn(inventory);
        new DisableOpenInventoryListener().onInventoryOpen(event);
        verify(event, never()).setCancelled(anyBoolean());
    }

    @Test
    void creativeDoesNotBypassOrdinaryRecipeRestrictions() {
        Player player = creativePlayer();
        CraftItemEvent event = mock(CraftItemEvent.class);
        org.bukkit.inventory.CraftingInventory inventory = mock(org.bukkit.inventory.CraftingInventory.class);
        ItemStack result = mock(ItemStack.class);
        when(result.getType()).thenReturn(Material.STONE);
        when(result.clone()).thenReturn(result);
        when(inventory.getResult()).thenReturn(result);
        when(event.getInventory()).thenReturn(inventory);
        when(event.getWhoClicked()).thenReturn(player);
        new DisableRecipeListener().onCraftItem(event);
        verify(event).setCancelled(true);
    }
}

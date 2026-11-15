package net.azisaba.lgw.core.hijack;

import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HijackWeaponGuardTest {
    @Test void automaticClassificationExplicitIdsAndMatchScope() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true);
        var values = new HashMap<>(HijackRules.defaults().values());
        values.put("weapons.blocked-ids", List.of("Special"));
        when(controller.getActive()).thenReturn(new HijackRules(values));
        Map<String, String> settings = Map.of("GRE.Shooting.Projectile_Type", "grenade",
                "C4.Explosive_Devices.Enable", "true", "C4.Explosive_Devices.Device_Type", "remote",
                "ITEM.Explosive_Devices.Enable", "true", "ITEM.Explosive_Devices.Device_Type", "itembomb");
        var guard = new HijackWeaponGuard(controller, item -> null, settings::get);
        assertTrue(guard.blocked("GRE")); assertTrue(guard.blocked("C4")); assertTrue(guard.blocked("ITEM"));
        assertTrue(guard.blocked("special")); assertFalse(guard.blocked("Rifle")); assertFalse(guard.blocked(null));
        values.put("weapons.block-grenades", false); values.put("weapons.block-c4", false);
        when(controller.getActive()).thenReturn(new HijackRules(values));
        assertFalse(guard.blocked("GRE")); assertFalse(guard.blocked("C4"));
        when(controller.isRunning()).thenReturn(false); assertFalse(guard.blocked("Special"));
    }

    @Test void stopsConsumptionAndRemoteBypassHandlersAndRestoresOriginals() throws Exception {
        Plugin cs = mock(Plugin.class); when(cs.getName()).thenReturn("CrackShot"); when(cs.isEnabled()).thenReturn(true);
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true); when(controller.getActive()).thenReturn(HijackRules.defaults());
        Player player = mock(Player.class); when(controller.isParticipant(player)).thenReturn(true);
        PlayerInventory inventory = mock(PlayerInventory.class); when(player.getInventory()).thenReturn(inventory);
        ItemStack item = mock(ItemStack.class); when(inventory.getItemInMainHand()).thenReturn(item);
        PlayerInteractEvent interact = mock(PlayerInteractEvent.class);
        when(interact.getPlayer()).thenReturn(player); when(interact.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        BlockPlaceEvent place = mock(BlockPlaceEvent.class); when(place.getPlayer()).thenReturn(player); when(place.getItemInHand()).thenReturn(item);
        AtomicInteger consumed = new AtomicInteger();
        Listener listener = new Listener() { };
        var originalUse = new RegisteredListener(listener, (ignored, event) -> consumed.incrementAndGet(), EventPriority.NORMAL, cs, false);
        var originalPlace = new RegisteredListener(listener, (ignored, event) -> consumed.incrementAndGet(), EventPriority.HIGHEST, cs, false);
        PlayerInteractEvent.getHandlerList().register(originalUse); BlockPlaceEvent.getHandlerList().register(originalPlace);
        var guard = new HijackWeaponGuard(controller, ignored -> "C4", key -> key.endsWith("Enable") ? "true" : "remote");
        try {
            guard.install();
            var wrappedUse = find(PlayerInteractEvent.getHandlerList(), cs);
            var wrappedPlace = find(BlockPlaceEvent.getHandlerList(), cs);
            wrappedUse.callEvent(interact); wrappedPlace.callEvent(place);
            assertEquals(0, consumed.get()); verify(interact).setCancelled(true); verify(place).setCancelled(true);
            // A nonparticipant uses the original handler, including its original cancellation policy.
            when(controller.isParticipant(player)).thenReturn(false);
            wrappedUse.callEvent(interact); assertEquals(1, consumed.get());
            guard.close();
            assertSame(originalUse, find(PlayerInteractEvent.getHandlerList(), cs));
            assertSame(originalPlace, find(BlockPlaceEvent.getHandlerList(), cs));
        } finally { guard.close(); HandlerList.unregisterAll(cs); }
    }

    @Test void prepareAndBurstCallbacksCannotBypassRestriction() {
        HijackController controller = mock(HijackController.class);
        Player player = mock(Player.class);
        when(controller.isRunning()).thenReturn(true); when(controller.isParticipant(player)).thenReturn(true);
        when(controller.getActive()).thenReturn(HijackRules.defaults());
        var guard = new HijackWeaponGuard(controller, item -> null, key -> key.endsWith("Projectile_Type") ? "grenade" : null);
        var prepare = new WeaponPrepareShootEvent(player, "GRE");
        var burst = new WeaponPreShootEvent(player, "GRE", "", 0, false);
        guard.onPrepare(prepare); guard.onPreShoot(burst);
        assertTrue(prepare.isCancelled()); assertTrue(burst.isCancelled());
    }
    @Test void denyMemoizesWeaponTitleForNamedItems() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true); when(controller.getActive()).thenReturn(HijackRules.defaults());
        ItemStack item = namedItem(Material.IRON_INGOT, (short) 0, "Grenade");
        Player player = participantWithItem(item); when(controller.isParticipant(player)).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();
        var guard = new HijackWeaponGuard(controller, ignored -> { calls.incrementAndGet(); return "GRE"; },
                key -> key.endsWith("Projectile_Type") ? "grenade" : "false");
        PlayerInteractEvent event = interactFor(player);
        assertTrue(guard.deny(event));
        assertTrue(guard.deny(event));
        assertEquals(1, calls.get());
    }

    @Test void denySkipsIdentifyForUnnamedItems() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true); when(controller.getActive()).thenReturn(HijackRules.defaults());
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        when(item.hasItemMeta()).thenReturn(true); when(item.getItemMeta()).thenReturn(meta);
        when(meta.hasDisplayName()).thenReturn(false);
        Player player = participantWithItem(item); when(controller.isParticipant(player)).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();
        var guard = new HijackWeaponGuard(controller, ignored -> { calls.incrementAndGet(); return "GRE"; }, key -> null);
        assertFalse(guard.deny(interactFor(player)));
        assertEquals(0, calls.get());
    }

    @Test void denyDoesNotCacheAttachmentNames() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true); when(controller.getActive()).thenReturn(HijackRules.defaults());
        ItemStack item = namedItem(Material.IRON_INGOT, (short) 0, "Gun ◊ Scope");
        Player player = participantWithItem(item); when(controller.isParticipant(player)).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();
        var guard = new HijackWeaponGuard(controller, ignored -> { calls.incrementAndGet(); return "GRE"; },
                key -> key.endsWith("Projectile_Type") ? "grenade" : "false");
        PlayerInteractEvent event = interactFor(player);
        assertTrue(guard.deny(event));
        assertTrue(guard.deny(event));
        assertEquals(2, calls.get());
    }

    @Test void denySkipsIdentifyWhenNoWeaponRestrictions() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true);
        var values = new HashMap<>(HijackRules.defaults().values());
        values.put("weapons.block-grenades", false);
        values.put("weapons.block-c4", false);
        values.put("weapons.blocked-ids", List.of());
        when(controller.getActive()).thenReturn(new HijackRules(values));
        ItemStack item = mock(ItemStack.class);
        Player player = participantWithItem(item); when(controller.isParticipant(player)).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();
        var guard = new HijackWeaponGuard(controller, ignored -> { calls.incrementAndGet(); return "GRE"; }, key -> null);
        assertFalse(guard.deny(interactFor(player)));
        assertEquals(0, calls.get());
    }

    @Test void crackShotConfigReloadClearsTitleCacheFromPlayerCommand() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true); when(controller.getActive()).thenReturn(HijackRules.defaults());
        ItemStack item = namedItem(Material.IRON_INGOT, (short) 0, "Grenade");
        Player player = participantWithItem(item); when(controller.isParticipant(player)).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();
        var guard = new HijackWeaponGuard(controller, ignored -> { calls.incrementAndGet(); return "GRE"; },
                key -> key.endsWith("Projectile_Type") ? "grenade" : "false");
        PlayerInteractEvent event = interactFor(player);
        assertTrue(guard.deny(event));
        assertTrue(guard.deny(event));
        assertEquals(1, calls.get());
        PlayerCommandPreprocessEvent reload = mock(PlayerCommandPreprocessEvent.class);
        when(reload.getMessage()).thenReturn("/crackshot config reload");
        guard.onPlayerCommand(reload);
        assertTrue(guard.deny(event));
        assertEquals(2, calls.get());
    }

    @Test void crackShotConfigReloadClearsTitleCacheFromServerCommand() {
        HijackController controller = mock(HijackController.class);
        when(controller.isRunning()).thenReturn(true); when(controller.getActive()).thenReturn(HijackRules.defaults());
        ItemStack item = namedItem(Material.IRON_INGOT, (short) 0, "Grenade");
        Player player = participantWithItem(item); when(controller.isParticipant(player)).thenReturn(true);
        AtomicInteger calls = new AtomicInteger();
        var guard = new HijackWeaponGuard(controller, ignored -> { calls.incrementAndGet(); return "GRE"; },
                key -> key.endsWith("Projectile_Type") ? "grenade" : "false");
        PlayerInteractEvent event = interactFor(player);
        assertTrue(guard.deny(event));
        assertTrue(guard.deny(event));
        assertEquals(1, calls.get());
        ServerCommandEvent reload = mock(ServerCommandEvent.class);
        when(reload.getCommand()).thenReturn("shot config reload");
        guard.onServerCommand(reload);
        assertTrue(guard.deny(event));
        assertEquals(2, calls.get());
    }

    private ItemStack namedItem(Material material, short durability, String displayName) {
        ItemStack item = mock(ItemStack.class);
        ItemMeta meta = mock(ItemMeta.class);
        when(item.hasItemMeta()).thenReturn(true);
        when(item.getItemMeta()).thenReturn(meta);
        when(meta.hasDisplayName()).thenReturn(true);
        when(meta.getDisplayName()).thenReturn(displayName);
        when(item.getType()).thenReturn(material);
        when(item.getDurability()).thenReturn(durability);
        return item;
    }

    private Player participantWithItem(ItemStack item) {
        Player player = mock(Player.class);
        PlayerInventory inventory = mock(PlayerInventory.class);
        when(player.getInventory()).thenReturn(inventory);
        when(inventory.getItemInMainHand()).thenReturn(item);
        return player;
    }

    private PlayerInteractEvent interactFor(Player player) {
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        when(event.getPlayer()).thenReturn(player);
        when(event.getAction()).thenReturn(Action.RIGHT_CLICK_AIR);
        return event;
    }

    private RegisteredListener find(HandlerList list, Plugin plugin) {
        return java.util.Arrays.stream(list.getRegisteredListeners()).filter(listener -> listener.getPlugin() == plugin).findFirst().orElseThrow();
    }
}

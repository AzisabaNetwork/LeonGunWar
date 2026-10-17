package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.MapsConfig;
import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.GameMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HijackAreaEditorTest {
    LeonGunWar plugin;
    MapsConfig maps;
    World world;
    Player player;
    GameMap map;
    HijackAreaEditor editor;
    @BeforeEach void setup() {
        plugin = mock(LeonGunWar.class); when(plugin.getName()).thenReturn("LeonGunWar"); when(plugin.getLogger()).thenReturn(mock(Logger.class));
        maps = mock(MapsConfig.class);
        world = mock(World.class); when(world.getUID()).thenReturn(UUID.randomUUID()); when(world.getMaxHeight()).thenReturn(256);
        player = mock(Player.class); when(player.getUniqueId()).thenReturn(UUID.randomUUID()); when(player.getWorld()).thenReturn(world);
        when(player.hasPermission(anyString())).thenReturn(true);
        map = new GameMap("New Arena", world, Map.of(), new java.util.ArrayList<>());
        when(maps.getAllGameMap()).thenReturn(List.of(map));
        editor = new HijackAreaEditor(plugin, maps);
    }
    private void run(String... tail) {
        String[] args = new String[tail.length + 2]; args[0] = "hijack"; args[1] = "area";
        System.arraycopy(tail, 0, args, 2, tail.length); editor.execute(player, args);
    }
    @Test void preciseSelectionSavesToNamedMapAndClearsOnlyAfterSuccess() throws Exception {
        when(player.getLocation()).thenReturn(new Location(world, 10.25, 52, 10.75)); run("pos1");
        when(player.getLocation()).thenReturn(new Location(world, .125, 48, .5)); run("pos2");
        doThrow(new IOException("read only")).doNothing().when(maps).saveHijackArea(eq(map), eq("1"), any());
        run("save", "1", "New", "Arena"); run("save", "1", "New", "Arena");
        var capture = ArgumentCaptor.forClass(Area3D.class);
        verify(maps, times(2)).saveHijackArea(eq(map), eq("1"), capture.capture());
        assertEquals(new Vector(.125, 48, .5), capture.getValue().getMin());
        assertEquals(new Vector(10.25, 52, 10.75), capture.getValue().getMax());
        run("save", "1", "New", "Arena");
        verify(maps, times(2)).saveHijackArea(eq(map), eq("1"), any());
    }
    @Test void permissionsConsoleWorldMismatchAndQuitPreventSaving() throws Exception {
        when(player.hasPermission(anyString())).thenReturn(false); run("pos1"); verifyNoInteractions(maps);
        when(player.hasPermission(anyString())).thenReturn(true);
        when(player.getLocation()).thenReturn(new Location(world, 0, 48, 0)); run("pos1"); run("pos2");
        var other = mock(World.class); when(other.getUID()).thenReturn(UUID.randomUUID());
        when(player.getWorld()).thenReturn(other); run("save", "1", "New Arena");
        when(player.getWorld()).thenReturn(world);
        var quit = mock(PlayerQuitEvent.class); when(quit.getPlayer()).thenReturn(player);
        editor.onQuit(quit); run("save", "1", "New Arena");
        verify(maps, never()).saveHijackArea(any(), anyString(), any());
        var console = mock(CommandSender.class); when(console.hasPermission(anyString())).thenReturn(true);
        editor.execute(console, new String[]{"hijack", "area", "pos1"}); verify(console).sendMessage(contains("プレイヤー専用"));
    }
    @Test void blockSelectionIncludesBothWholeBlocksWithoutExpandingPreciseFeet() {
        Area3D blocks = HijackAreaEditor.selectedArea(new Vector(10, 52, 10), true, new Vector(0, 48, 0), true);
        assertEquals(new Vector(0, 48, 0), blocks.getMin()); assertEquals(new Vector(11, 53, 11), blocks.getMax());
        Area3D feet = HijackAreaEditor.selectedArea(new Vector(10, 52, 10), false, new Vector(0, 48, 0), false);
        assertEquals(new Vector(10, 52, 10), feet.getMax());
    }
    @Test void taggedWandSelectsWithoutBreakingBlocks() throws Exception {
        var item = mock(org.bukkit.inventory.ItemStack.class);
        var meta = mock(org.bukkit.inventory.meta.ItemMeta.class);
        var data = mock(org.bukkit.persistence.PersistentDataContainer.class);
        when(item.getType()).thenReturn(Material.WOODEN_AXE); when(item.hasItemMeta()).thenReturn(true); when(item.getItemMeta()).thenReturn(meta);
        when(meta.getPersistentDataContainer()).thenReturn(data);
        when(data.has(any(org.bukkit.NamespacedKey.class), eq(org.bukkit.persistence.PersistentDataType.BYTE))).thenReturn(true);
        Block block = mock(Block.class);
        when(block.getLocation()).thenReturn(new Location(world, 0, 48, 0), new Location(world, 10, 52, 10));
        for (var action : List.of(org.bukkit.event.block.Action.LEFT_CLICK_BLOCK, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)) {
            var event = mock(org.bukkit.event.player.PlayerInteractEvent.class);
            when(event.getHand()).thenReturn(org.bukkit.inventory.EquipmentSlot.HAND); when(event.getClickedBlock()).thenReturn(block);
            when(event.getItem()).thenReturn(item); when(event.getAction()).thenReturn(action); when(event.getPlayer()).thenReturn(player);
            editor.onInteract(event); verify(event).setCancelled(true);
        }
        run("save", "1", "New Arena");
        var capture = ArgumentCaptor.forClass(Area3D.class); verify(maps).saveHijackArea(eq(map), eq("1"), capture.capture());
        assertEquals(new Vector(11, 53, 11), capture.getValue().getMax());
        var inventory = mock(org.bukkit.inventory.PlayerInventory.class); when(player.getInventory()).thenReturn(inventory); when(inventory.getItemInMainHand()).thenReturn(item);
        var breakEvent = new org.bukkit.event.block.BlockBreakEvent(block, player); editor.onBreak(breakEvent); assertTrue(breakEvent.isCancelled());
    }
    private void ground(Material type) {
        Block air = mock(Block.class); when(air.isPassable()).thenReturn(true); when(air.getType()).thenReturn(mock(Material.class));
        // Paper 1.21's Material.isSolid reads the server registry; mock that boundary for the safe-floor case.
        Material floorType = type;
        if (type == Material.STONE) { floorType = mock(Material.class); when(floorType.isSolid()).thenReturn(true); }
        Block stone = mock(Block.class); when(stone.getType()).thenReturn(floorType);
        when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenAnswer(call -> (int) call.getArgument(1) <= 63 ? stone : air);
    }
    @Test void teleportUsesSafeAreaFeetAndRespectsCancellationAndHazardousGround() {
        Area3D area = new Area3D(new Vector(0, 64, 0), new Vector(10, 68, 10));
        when(maps.getHijackAreaEntries(map)).thenReturn(Map.of("1", area)); ground(Material.STONE);
        when(player.getLocation()).thenReturn(new Location(world, 0, 70, 0, 90, 12));
        when(player.teleport(any(Location.class))).thenReturn(false); run("tp", "1", "New Arena");
        verify(player).sendMessage(contains("キャンセル"));
        when(player.teleport(any(Location.class))).thenReturn(true); run("tp", "1", "New Arena");
        var capture = ArgumentCaptor.forClass(Location.class); verify(player, times(2)).teleport(capture.capture());
        assertTrue(area.isInArea(capture.getValue().toVector())); assertEquals(64, capture.getValue().getY()); assertEquals(90, capture.getValue().getYaw());
        ground(Material.MAGMA_BLOCK); assertNull(HijackAreaEditor.safeDestination(world, area));
    }
    @Test void completionSupportsSpaceSeparatedMapsAndHidesActionsWithoutPermission() {
        assertTrue(editor.complete(player, new String[]{"hijack", "area", ""}).contains("wand"));
        assertEquals(List.of("New Arena"), editor.complete(player, new String[]{"hijack", "area", "tp", "1", "N"}));
        assertEquals(List.of("Arena"), editor.complete(player, new String[]{"hijack", "area", "tp", "1", "New", "A"}));
        when(player.hasPermission(anyString())).thenReturn(false);
        assertTrue(editor.complete(player, new String[]{"hijack", "area", ""}).isEmpty());
    }
}

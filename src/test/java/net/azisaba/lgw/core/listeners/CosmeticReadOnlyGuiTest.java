package net.azisaba.lgw.core.listeners;

import net.azisaba.lgw.core.killaction.KillActionCatalog;
import net.azisaba.lgw.core.killaction.KillActionGui;
import net.azisaba.lgw.core.syogo.SyogoGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/** GUIイベント境界で、確認画面から保存やプレビューへ進まないことを検証する。 */
class CosmeticReadOnlyGuiTest {
    private InventoryHolder killActionMenu() throws Exception {
        Class<?> type = Class.forName(KillActionGui.class.getName() + "$Menu");
        var constructor = type.getDeclaredConstructor(KillActionCatalog.Category.class, int.class, boolean.class);
        constructor.setAccessible(true);
        Object menu = constructor.newInstance(KillActionCatalog.Category.SOUND, 0, true);
        var visible = type.getDeclaredField("visible");
        visible.setAccessible(true);
        visible.set(menu, List.of(mock(KillActionCatalog.Entry.class)));
        return (InventoryHolder) menu;
    }

    private InventoryHolder syogoMenu() throws Exception {
        Class<?> type = Class.forName(SyogoGui.class.getName() + "$Menu");
        var constructor = type.getDeclaredConstructor(List.class, int.class, boolean.class);
        constructor.setAccessible(true);
        return (InventoryHolder) constructor.newInstance(List.of("rookie"), 0, true);
    }

    private InventoryView view(InventoryHolder holder) {
        Inventory top = mock(Inventory.class);
        when(top.getHolder()).thenReturn(holder);
        when(top.getSize()).thenReturn(54);
        InventoryView view = mock(InventoryView.class);
        when(view.getTopInventory()).thenReturn(top);
        return view;
    }

    private InventoryClickEvent click(InventoryHolder holder, int slot, boolean rightClick) {
        InventoryView view = view(holder);
        Player player = mock(Player.class);
        when(player.hasPermission("leongunwar.command.syogo")).thenReturn(true);
        InventoryClickEvent event = mock(InventoryClickEvent.class);
        when(event.getView()).thenReturn(view);
        when(event.getWhoClicked()).thenReturn(player);
        when(event.getRawSlot()).thenReturn(slot);
        when(event.isLeftClick()).thenReturn(!rightClick);
        when(event.isRightClick()).thenReturn(rightClick);
        return event;
    }

    @Test
    void killActionReadOnlyBlocksSelectPreviewAndReset() throws Exception {
        // 保存/プレビューに進めばnull依存へのアクセスで失敗する。
        KillActionGui gui = new KillActionGui(null, null);
        InventoryHolder menu = killActionMenu();
        for (InventoryClickEvent event : List.of(click(menu, 0, false), click(menu, 0, true), click(menu, 47, false))) {
            assertDoesNotThrow(() -> gui.onClick(event));
            verify(event).setCancelled(true);
        }
    }

    @Test
    void syogoReadOnlyBlocksEquipAndUnequip() throws Exception {
        SyogoGui gui = new SyogoGui(null, null);
        InventoryHolder menu = syogoMenu();
        for (InventoryClickEvent event : List.of(click(menu, 0, false), click(menu, 49, false))) {
            assertDoesNotThrow(() -> gui.click(event));
            verify(event).setCancelled(true);
        }
    }

    @Test
    void readOnlyKillActionStillCancelsDraggingItemsIntoGui() throws Exception {
        InventoryView view = view(killActionMenu());
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getView()).thenReturn(view);
        new KillActionGui(null, null).onDrag(event);
        verify(event).setCancelled(true);
    }

    @Test
    void readOnlySyogoStillCancelsDraggingItemsIntoGui() throws Exception {
        InventoryView view = view(syogoMenu());
        InventoryDragEvent event = mock(InventoryDragEvent.class);
        when(event.getView()).thenReturn(view);
        new SyogoGui(null, null).drag(event);
        verify(event).setCancelled(true);
    }
}

package net.azisaba.lgw.core.killaction;

import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class KillActionOwnershipTest {
    private final UUID uuid = UUID.randomUUID();
    private final Player player = mock(Player.class);
    private final KillActionManager manager = new KillActionManager(null, null, null, null);
    private KillActionCatalog.Entry entry(KillActionCatalog.Category category, String permission) {
        return new KillActionCatalog.Entry(category, "reward", "報酬", null, List.of(), KillActionCatalog.Rarity.COMMON,
                permission, "イベント報酬", null, List.of(), List.of());
    }
    @Test @SuppressWarnings("unchecked") void dbOwnershipUnlocksWithoutLuckPermsAndDoesNotLeakToOtherCategories() throws Exception {
        when(player.getUniqueId()).thenReturn(uuid);
        var field = KillActionManager.class.getDeclaredField("owned"); field.setAccessible(true);
        ((Map<UUID, Set<KillActionTicketStore.Owned>>) field.get(manager)).put(uuid,
                Set.of(new KillActionTicketStore.Owned(KillActionCatalog.Category.SOUND, "reward")));
        assertTrue(manager.isUnlocked(player, entry(KillActionCatalog.Category.SOUND, "")));
        assertFalse(manager.isUnlocked(player, entry(KillActionCatalog.Category.LOG, "")));
        verify(player, never()).hasPermission(anyString());
    }
    @Test void existingPermissionAccessRemainsCompatible() {
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.hasPermission("legacy.reward")).thenReturn(true);
        assertTrue(manager.isUnlocked(player, entry(KillActionCatalog.Category.SOUND, "legacy.reward")));
        assertFalse(manager.isUnlocked(player, entry(KillActionCatalog.Category.SOUND, "another")));
    }
    @Test void opCanStillUseEveryEffect() {
        when(player.isOp()).thenReturn(true);
        for (var category : KillActionCatalog.Category.values()) assertTrue(manager.isUnlocked(player, entry(category, "")));
    }
}

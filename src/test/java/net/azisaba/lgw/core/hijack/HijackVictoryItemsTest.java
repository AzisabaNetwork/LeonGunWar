package net.azisaba.lgw.core.hijack;

import com.shampaggon.crackshot.CSUtility;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.configs.ItemsConfig;
import net.azisaba.lgw.core.configs.SpawnsConfig;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.BroadcastUtils;
import net.azisaba.lgw.core.util.CustomItem;
import net.azisaba.lgw.core.util.KillDeathCounter;
import net.azisaba.lgw.core.util.MatchMode;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class HijackVictoryItemsTest {
    private LeonGunWar plugin;
    private MatchManager manager;
    private KillDeathCounter counter;
    private ItemsConfig items;
    private HijackController controller;
    private Player first, second;
    private ItemStack reward;

    @BeforeEach void setup() {
        plugin = mock(LeonGunWar.class); manager = mock(MatchManager.class);
        counter = mock(KillDeathCounter.class); items = mock(ItemsConfig.class);
        controller = mock(HijackController.class); reward = mock(ItemStack.class);
        when(plugin.getManager()).thenReturn(manager);
        when(plugin.getItemsConfig()).thenReturn(items);
        when(items.getVictoryItemCrackShotIds()).thenReturn(null);
        when(plugin.getSpawnsConfig()).thenReturn(mock(SpawnsConfig.class));
        when(plugin.getHijackController()).thenReturn(controller);
        when(manager.getKillDeathCounter()).thenReturn(counter);
        when(counter.getMVPPlayer()).thenReturn(List.of());
        when(manager.getMatchMode()).thenReturn(MatchMode.HIJACK);
        when(manager.isMatching()).thenReturn(false); // Existing LOW finish handler already ran.
        first = player(); second = player();
        LeonGunWar.matchJoin.clear();
    }
    @AfterEach void cleanup() { LeonGunWar.matchJoin.clear(); }
    private Player player() {
        Player p = mock(Player.class);
        when(p.getUniqueId()).thenReturn(UUID.randomUUID());
        when(p.getInventory()).thenReturn(mock(PlayerInventory.class));
        var attribute = mock(AttributeInstance.class); when(attribute.getDefaultValue()).thenReturn(20.0);
        when(p.getAttribute(any())).thenReturn(attribute);
        return p;
    }
    private void rules(String mode, int seconds, int kills) {
        var values = new HashMap<>(HijackRules.defaults().values());
        values.put("victory-item.mode", mode);
        values.put("victory-item.minimum-participation-seconds", seconds);
        values.put("victory-item.minimum-kills", kills);
        when(controller.getActive()).thenReturn(new HijackRules(values));
    }
    private void finish() {
        try (var current = mockStatic(LeonGunWar.class); var broadcasts = mockStatic(BroadcastUtils.class);
             var custom = mockStatic(CustomItem.class);
             var utility = mockConstruction(CSUtility.class, (instance, context) -> when(instance.generateWeapon(anyString())).thenReturn(reward))) {
            current.when(LeonGunWar::getPlugin).thenReturn(plugin);
            custom.when(CustomItem::getWonItem).thenReturn(reward);
            new MatchControlListener().onMatchFinished(new MatchFinishedEvent(null, List.of(BattleTeam.RED),
                    Map.of(BattleTeam.RED, List.of(first, second), BattleTeam.BLUE, List.of())));
        }
    }

    @Test void noRequirementAwardsBothWinnersEvenWithoutKillsOrJoinTimes() {
        rules("none", 360, 20);
        finish();
        verify(first.getInventory()).addItem(reward);
        verify(second.getInventory()).addItem(reward);
    }
    @Test void timeRequirementUsesBothWinnersJoinTimesBeforeMatchFinalization() {
        rules("time", 10, 20);
        long joined = System.currentTimeMillis() - 20_000;
        LeonGunWar.matchJoin.put(first.getUniqueId(), joined);
        LeonGunWar.matchJoin.put(second.getUniqueId(), joined);
        finish();
        verify(first.getInventory()).addItem(reward);
        verify(second.getInventory()).addItem(reward);
        assertEquals(2, LeonGunWar.matchJoin.size());
    }
    @Test void configuredCrackShotItemsUseTheSameHijackEligibility() {
        rules("kills", 360, 3);
        when(items.getVictoryItemCrackShotIds()).thenReturn(List.of("WinToken"));
        when(counter.getKills(first)).thenReturn(2); when(counter.getKills(second)).thenReturn(3);
        finish();
        verify(first.getInventory(), never()).addItem(any(ItemStack[].class));
        verify(second.getInventory()).addItem(reward);
        verify(first).sendMessage(contains("3キル以上"));
    }
    @Test void otherModesRetainTheirExistingDefaultAndCustomItemBehavior() {
        rules("none", 0, 0);
        when(manager.getMatchMode()).thenReturn(MatchMode.TEAM_DEATH_MATCH);
        when(counter.getKills(second)).thenReturn(20);
        finish();
        verify(first.getInventory(), never()).addItem(any(ItemStack[].class));
        verify(second.getInventory()).addItem(reward);
        clearInvocations(first.getInventory(), second.getInventory());
        when(items.getVictoryItemCrackShotIds()).thenReturn(List.of("WinToken"));
        finish();
        verify(first.getInventory()).addItem(reward);
        verify(second.getInventory()).addItem(reward);
    }
}

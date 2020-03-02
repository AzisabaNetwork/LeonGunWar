package net.azisaba.lgw.core.listeners.others;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import com.google.common.collect.Sets;
import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.tasks.AllowEditInventoryTask;
import net.azisaba.lgw.core.utils.Chat;
import net.azisaba.lgw.core.utils.SecondOfDay;

/**
 * ホットバーの変更した武器の数だけクールダウンを設ける
 *
 * @author siloneco, YukiLeafX
 *
 */
public class DisableChangeItemListener implements Listener {

    public static ItemStack[] getHotbar(PlayerInventory inventory) {
        return IntStream.range(0, 9)
                .mapToObj(inventory::getItem)
                .toArray(ItemStack[]::new);
    }

    private static final int MULTIPLE_SECONDS = 10;

    private final Map<Player, ItemStack[]> hotbars = new HashMap<>();

    private final Map<Player, Instant> remainTimes = new HashMap<>();
    private final Map<Player, BukkitTask> taskMap = new HashMap<>();
    private final Map<Player, BossBar> bossBars = new HashMap<>();

    private final CSDirector cs = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");
    private final CSUtility csUtil = new CSUtility();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();

        if ( inventory == null || inventory.getType() != InventoryType.PLAYER ) {
            return;
        }

        if ( !(event.getWhoClicked() instanceof Player) ) {
            return;
        }

        Player player = (Player) event.getWhoClicked();

        if ( !LeonGunWar.getPlugin().getManager().isPlayerMatching(player) ) {
            return;
        }

        if ( Instant.now().isBefore(remainTimes.getOrDefault(player, Instant.now())) ) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, .8f);
            player.sendMessage(Chat.f("{0}&c現在アイテム整理はクールダウン中です！", LeonGunWar.GAME_PREFIX));
            return;
        }

        ItemStack[] befores = getHotbar((PlayerInventory) inventory);
        hotbars.putIfAbsent(player, befores);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if ( inventory == null || inventory.getType() != InventoryType.CRAFTING ) {
            return;
        }

        if ( !(inventory.getHolder() instanceof Player) ) {
            return;
        }

        Player holder = (Player) inventory.getHolder();
        inventory = holder.getInventory();

        if ( inventory == null || inventory.getType() != InventoryType.PLAYER ) {
            return;
        }

        if ( !LeonGunWar.getPlugin().getManager().isPlayerMatching(holder) ) {
            return;
        }

        if ( !hotbars.containsKey(holder) ) {
            return;
        }

        ItemStack[] befores = hotbars.get(holder);
        ItemStack[] afters = getHotbar((PlayerInventory) inventory);

        hotbars.remove(holder);

        int checked = 0;
        boolean valid = true;

        for ( ItemStack after : afters ) {
            String weapon = csUtil.getWeaponTitle(after);
            String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

            if ( ctrl == null ) {
                continue;
            }

            String[] groups = ctrl.replaceAll(" ", "").split(",");

            Map<String, String> restore = Arrays.stream(groups)
                    .flatMap(group -> Stream.of(group + ".Message_Exceeded", group + ".Sounds_Exceeded"))
                    .collect(Collectors.toMap(group -> group, CSDirector.strings::remove));
            valid &= cs.validHotbar(holder, weapon);
            checked++;
            CSDirector.strings.putAll(restore);
        }

        if ( checked == 0 ) {
            return;
        }

        if ( !valid ) {
            holder.sendMessage(Chat.f("{0}&eそんな装備で大丈夫か？", LeonGunWar.GAME_PREFIX));
            return;
        }

        int changed = Sets.difference(Sets.newHashSet(befores), Sets.newHashSet(afters)).size();

        if ( changed == 0 ) {
            return;
        }

        int cooldown = changed * MULTIPLE_SECONDS;

        remainTimes.put(holder, Instant.now().plusSeconds(cooldown));
        taskMap.compute(holder, (a, task) -> {
            // タスク終了
            if ( task != null ) {
                task.cancel();
            }

            // ボスバー初期化
            bossBars.computeIfPresent(holder, (b, bossBar) -> {
                bossBar.removePlayer(holder);
                return null;
            });

            // タスク開始
            return new AllowEditInventoryTask(holder, remainTimes, cooldown, bossBars).runTaskTimer(LeonGunWar.getPlugin(), 0, 20);
        });

        holder.sendMessage(Chat.f("{0}&a{1}個 &cのホットバーにあるアイテムの変更を検出しました。", LeonGunWar.GAME_PREFIX, changed));
        holder.sendMessage(Chat.f("{0}&b{1} &cのクールダウンを開始します。(ﾉ∀`)ｱﾁｬｰ", LeonGunWar.GAME_PREFIX, SecondOfDay.f(cooldown)));
    }
}

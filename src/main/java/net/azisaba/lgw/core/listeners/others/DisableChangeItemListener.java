package net.azisaba.lgw.core.listeners.others;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import com.shampaggon.crackshot.events.WeaponPreShootEvent;
import com.shampaggon.crackshot.events.WeaponShootEvent;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

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

        if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE) {
            return;
        }

        if (inventory == null || inventory.getType() != InventoryType.PLAYER) {
            return;
        }

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        if (LeonGunWar.getPlugin().getManager().getCurrentGameMap().getSpawnPoint(LeonGunWar.getPlugin().getManager().getBattleTeam((Player) event.getWhoClicked()))!=null){
            Location spawnPoint = LeonGunWar.getPlugin().getManager().getCurrentGameMap().getSpawnPoint(LeonGunWar.getPlugin().getManager().getBattleTeam((Player) event.getWhoClicked()));

            if (spawnPoint.distance(event.getWhoClicked().getLocation()) <= 10) {
                return;
            }
        }

        Player player = (Player) event.getWhoClicked();

        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(player)) {
            return;
        }

        if (!LeonGunWar.getPlugin().getManager().getItemChangeValidator()
            .isAllowedToChangeItem(player)) {
            event.setCancelled(true);
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, .8f);
            player.sendMessage(Chat.f("{0}&cスポーン地点以外でアイテムの変更はできません!", LeonGunWar.GAME_PREFIX));
            return;
        }

        ItemStack[] befores = getHotbar((PlayerInventory) inventory);
        hotbars.putIfAbsent(player, befores);
    }

    @EventHandler
    public void disableShootOnInvalidHotbar(WeaponPreShootEvent e) {
        Player p = e.getPlayer();

        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(p)) {
            return;
        }

        Player holder = (Player) p.getInventory().getHolder();
        ItemStack[] hotbar = getHotbar(p.getInventory());

        boolean valid = true;

        for (ItemStack item : hotbar) {
            String weapon = csUtil.getWeaponTitle(item);
            String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

            if (ctrl == null) {
                continue;
            }
            valid &= cs.validHotbar(holder, weapon);
        }

        if (!valid) {
            e.setCancelled(true);
            p.sendMessage(Chat.f("{0}&c無効なアイテム欄であるため銃を打てません！", LeonGunWar.GAME_PREFIX));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();

        if (inventory == null || inventory.getType() != InventoryType.CRAFTING) {
            return;
        }

        if (!(inventory.getHolder() instanceof Player)) {
            return;
        }

        Player holder = (Player) inventory.getHolder();
        inventory = holder.getInventory();

        if (inventory == null || inventory.getType() != InventoryType.PLAYER) {
            return;
        }

        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(holder)) {
            return;
        }

        if (!hotbars.containsKey(holder)) {
            return;
        }

        ItemStack[] befores = hotbars.get(holder);
        ItemStack[] afters = getHotbar((PlayerInventory) inventory);

        hotbars.remove(holder);

        int checked = 0;
        boolean valid = true;

        for (ItemStack after : afters) {
            String weapon = csUtil.getWeaponTitle(after);
            String ctrl = cs.getString(weapon + ".Item_Information.Inventory_Control");

            if (ctrl == null) {
                continue;
            }

            String[] groups = ctrl.replaceAll(" ", "").split(",");

            Map<String, String> restore = Arrays.stream(groups)
                .flatMap(
                    group -> Stream.of(group + ".Message_Exceeded", group + ".Sounds_Exceeded"))
                .filter(group -> CSDirector.strings.containsKey(group))
                .collect(Collectors.toMap(group -> group, CSDirector.strings::remove));
            valid &= cs.validHotbar(holder, weapon);
            checked++;
            CSDirector.strings.putAll(restore);
        }

        if (checked == 0) {
            return;
        }

        if (!valid) {
            holder.sendMessage(Chat.f("{0}&eそんな装備で大丈夫か？", LeonGunWar.GAME_PREFIX));
            return;
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(p)) {
            return;
        }
        LeonGunWar.getPlugin().getManager().getItemChangeValidator().respawned(p);
    }

    @EventHandler
    public void onShoot(WeaponShootEvent e) {
        Player p = e.getPlayer();
        if (!LeonGunWar.getPlugin().getManager().isPlayerMatching(p)) {
            return;
        }
        LeonGunWar.getPlugin().getManager().getItemChangeValidator().shot(p);
    }
}

package net.azisaba.lgw.core;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.azisaba.lgw.core.util.Args;
import net.azisaba.lgw.core.util.Chat;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/** WorldGuard の region を使ったロビー向け PvP エリアの選択と管理。 */
public class PvPArenaManager implements Listener {

    private static final String REGION_PREFIX = "lgw_pvparena_";
    private static final int REGION_PRIORITY = 100;
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9_-]{1,32}");

    private final NamespacedKey wandKey;
    private final Map<UUID, Selection> selections = new HashMap<>();

    public PvPArenaManager(LeonGunWar plugin) {
        wandKey = new NamespacedKey(plugin, "pvparena_wand");
    }

    public void handleCommand(CommandSender sender, String[] args) {
        if (!sender.hasPermission("leongunwar.command.leongunwaradmin")) {
            sender.sendMessage(Chat.f("&c権限がありません。"));
            return;
        }
        if (args.length < 2) {
            sendHelp(sender);
            return;
        }

        switch (args[1].toLowerCase(Locale.ROOT)) {
            case "wand" -> giveWand(sender);
            case "create" -> create(sender, args);
            case "delete" -> delete(sender, args);
            case "list" -> list(sender);
            case "info" -> info(sender, args);
            case "cancel" -> cancel(sender);
            default -> sendHelp(sender);
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            return Args.complete(args, 1, "wand", "create", "delete", "list", "info", "cancel");
        }
        if (args.length == 3 && sender instanceof Player player
                && (Args.check(args, 1, "delete") || Args.check(args, 1, "info"))) {
            RegionManager regions = getRegions(player.getWorld());
            if (regions == null) return List.of();
            String[] names = regions.getRegions().keySet().stream()
                    .filter(id -> id.startsWith(REGION_PREFIX))
                    .map(id -> id.substring(REGION_PREFIX.length()))
                    .toArray(String[]::new);
            return Args.complete(args, 2, names);
        }
        return List.of();
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Chat.f("&e/lgwadmin pvparena wand &7- 選択ツールを受け取る"));
        sender.sendMessage(Chat.f("&e/lgwadmin pvparena create <名前> &7- 選択範囲を登録"));
        sender.sendMessage(Chat.f("&e/lgwadmin pvparena delete <名前> &7- 現在のワールドから削除"));
        sender.sendMessage(Chat.f("&e/lgwadmin pvparena list|info <名前>|cancel"));
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player player) {
            return player;
        }
        sender.sendMessage(Chat.f("&cこの操作はプレイヤーのみ実行できます。"));
        return null;
    }

    private void giveWand(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return;

        ItemStack wand = new ItemStack(Material.WOODEN_AXE);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(Chat.f("&6PvP Arena 選択ツール"));
        meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
        wand.setItemMeta(meta);
        if (!player.getInventory().addItem(wand).isEmpty()) {
            player.sendMessage(Chat.f("&cインベントリに空きがありません。"));
            return;
        }
        selections.put(player.getUniqueId(), new Selection());
        player.sendMessage(Chat.f("&a左クリックで pos1、右クリックで pos2 を選択してください。"));
    }

    private void create(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        if (args.length != 3) {
            sender.sendMessage(Chat.f("&c使用方法: /lgwadmin pvparena create <名前>"));
            return;
        }
        String name = args[2].toLowerCase(Locale.ROOT);
        if (!NAME_PATTERN.matcher(name).matches()) {
            sender.sendMessage(Chat.f("&c名前は英小文字・数字・_・- の1～32文字にしてください。"));
            return;
        }
        Selection selection = selections.get(player.getUniqueId());
        if (selection == null || selection.first == null || selection.second == null) {
            sender.sendMessage(Chat.f("&c選択ツールで pos1 と pos2 を指定してください。"));
            return;
        }
        RegionManager regions = getRegions(selection.world);
        if (regions == null) {
            sender.sendMessage(Chat.f("&cこのワールドの WorldGuard region を読み込めません。"));
            return;
        }
        String regionId = REGION_PREFIX + name;
        if (regions.hasRegion(regionId)) {
            sender.sendMessage(Chat.f("&c同名の PvP Arena が既にあります。"));
            return;
        }

        ProtectedCuboidRegion region = new ProtectedCuboidRegion(regionId, selection.first, selection.second);
        region.setPriority(REGION_PRIORITY);
        region.setFlag(Flags.PVP, StateFlag.State.ALLOW);
        region.setFlag(Flags.PASSTHROUGH, StateFlag.State.ALLOW);
        regions.addRegion(region);
        try {
            regions.saveChanges();
        } catch (StorageException exception) {
            regions.removeRegion(regionId);
            LeonGunWar.getPlugin().getLogger().warning("PvP Arena の保存に失敗しました: " + exception.getMessage());
            sender.sendMessage(Chat.f("&cWorldGuard region の保存に失敗しました。"));
            return;
        }
        selections.put(player.getUniqueId(), new Selection());
        sender.sendMessage(Chat.f("&aPvP Arena {0} を {1} に作成しました。", name, selection.world.getName()));
    }

    private void delete(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        if (args.length != 3) {
            sender.sendMessage(Chat.f("&c使用方法: /lgwadmin pvparena delete <名前>"));
            return;
        }
        RegionManager regions = getRegions(player.getWorld());
        if (regions == null) {
            sender.sendMessage(Chat.f("&cこのワールドの WorldGuard region を読み込めません。"));
            return;
        }
        String regionId = REGION_PREFIX + args[2].toLowerCase(Locale.ROOT);
        ProtectedRegion region = regions.getRegion(regionId);
        if (region == null) {
            sender.sendMessage(Chat.f("&c現在のワールドにその PvP Arena はありません。"));
            return;
        }
        regions.removeRegion(regionId);
        try {
            regions.saveChanges();
        } catch (StorageException exception) {
            regions.addRegion(region);
            LeonGunWar.getPlugin().getLogger().warning("PvP Arena の削除保存に失敗しました: " + exception.getMessage());
            sender.sendMessage(Chat.f("&cWorldGuard region の保存に失敗しました。"));
            return;
        }
        sender.sendMessage(Chat.f("&aPvP Arena {0} を削除しました。", args[2]));
    }

    private void list(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        RegionManager regions = getRegions(player.getWorld());
        if (regions == null) {
            sender.sendMessage(Chat.f("&cこのワールドの WorldGuard region を読み込めません。"));
            return;
        }
        List<String> names = new ArrayList<>();
        for (String regionId : regions.getRegions().keySet()) {
            if (regionId.startsWith(REGION_PREFIX)) {
                names.add(regionId.substring(REGION_PREFIX.length()));
            }
        }
        names.sort(String::compareTo);
        sender.sendMessage(Chat.f("&e{0} の PvP Arena: &f{1}", player.getWorld().getName(),
                names.isEmpty() ? "なし" : String.join(", ", names)));
    }

    private void info(CommandSender sender, String[] args) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        if (args.length != 3) {
            sender.sendMessage(Chat.f("&c使用方法: /lgwadmin pvparena info <名前>"));
            return;
        }
        RegionManager regions = getRegions(player.getWorld());
        ProtectedRegion region = regions == null ? null : regions.getRegion(REGION_PREFIX + args[2].toLowerCase(Locale.ROOT));
        if (region == null) {
            sender.sendMessage(Chat.f("&c現在のワールドにその PvP Arena はありません。"));
            return;
        }
        sender.sendMessage(Chat.f("&e{0}: &f{1} ～ {2} &7(優先度 {3})", args[2],
                region.getMinimumPoint(), region.getMaximumPoint(), region.getPriority()));
    }

    private void cancel(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) return;
        selections.remove(player.getUniqueId());
        sender.sendMessage(Chat.f("&ePvP Arena の選択を終了しました。"));
    }

    private RegionManager getRegions(World world) {
        return WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    private boolean isWand(ItemStack item) {
        return item != null && item.getType() == Material.WOODEN_AXE && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getClickedBlock() == null || !isWand(event.getItem())) {
            return;
        }
        Action action = event.getAction();
        if (action != Action.LEFT_CLICK_BLOCK && action != Action.RIGHT_CLICK_BLOCK) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (!player.hasPermission("leongunwar.command.leongunwaradmin")) return;
        Selection selection = selections.get(player.getUniqueId());
        if (selection == null) {
            player.sendMessage(Chat.f("&c/lgwadmin pvparena wand で選択を開始してください。"));
            return;
        }
        Block block = event.getClickedBlock();
        if (selection.world != null && selection.world != block.getWorld()) {
            selection.first = null;
            selection.second = null;
            player.sendMessage(Chat.f("&eワールドが変わったため、選択をやり直します。"));
        }
        selection.world = block.getWorld();
        BlockVector3 point = BlockVector3.at(block.getX(), block.getY(), block.getZ());
        if (action == Action.LEFT_CLICK_BLOCK) {
            selection.first = point;
            player.sendMessage(Chat.f("&apos1: {0}, {1}, {2}", point.x(), point.y(), point.z()));
        } else {
            selection.second = point;
            player.sendMessage(Chat.f("&bpos2: {0}, {1}, {2}", point.x(), point.y(), point.z()));
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isWand(event.getPlayer().getInventory().getItemInMainHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        selections.remove(event.getPlayer().getUniqueId());
    }

    private static class Selection {
        private World world;
        private BlockVector3 first;
        private BlockVector3 second;
    }
}

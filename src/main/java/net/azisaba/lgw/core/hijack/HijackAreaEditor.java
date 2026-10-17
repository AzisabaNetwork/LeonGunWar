package net.azisaba.lgw.core.hijack;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.MapsConfig;
import net.azisaba.lgw.core.util.Area3D;
import net.azisaba.lgw.core.util.GameMap;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.Set;
import java.util.logging.Level;

/** Admin-only selection; saved geometry also updates the matching live objective, without editing blocks. */
public final class HijackAreaEditor implements Listener {
    private static final String PERMISSION = "leongunwar.command.leongunwaradmin";
    private static final Set<Material> HAZARDS = Set.of(Material.MAGMA_BLOCK, Material.CACTUS, Material.CAMPFIRE,
            Material.SOUL_CAMPFIRE, Material.FIRE, Material.SOUL_FIRE, Material.SWEET_BERRY_BUSH, Material.WITHER_ROSE);
    private final LeonGunWar plugin;
    private final MapsConfig maps;
    private final NamespacedKey wandKey;
    private final Map<UUID, Selection> selections = new HashMap<>();
    private static final class Selection {
        World world;
        Point first;
        Point second;
    }
    private record Point(Vector position, boolean block) {}

    public HijackAreaEditor(LeonGunWar plugin, MapsConfig maps) {
        this.plugin = plugin;
        this.maps = maps;
        wandKey = new NamespacedKey(plugin, "hijack_area_wand");
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) { sender.sendMessage("§c/lgwadmin の権限が必要です。"); return true; }
        String action = args.length > 2 ? args[2].toLowerCase(Locale.ROOT) : "help";
        try {
            switch (action) {
                case "wand" -> {
                    Player player = player(sender);
                    var wand = new ItemStack(Material.WOODEN_AXE);
                    var meta = wand.getItemMeta();
                    meta.setDisplayName("§bHIJACKエリア選択");
                    meta.setLore(List.of("§7左クリック: 第1点 / 右クリック: 第2点", "§7選択ブロック全体を含む直方体を保存"));
                    meta.getPersistentDataContainer().set(wandKey, PersistentDataType.BYTE, (byte) 1);
                    wand.setItemMeta(meta);
                    if (!player.getInventory().addItem(wand).isEmpty()) throw new IllegalArgumentException("インベントリに空きを作ってください。");
                    selections.remove(player.getUniqueId());
                    sender.sendMessage("§a左/右クリックで選択後、/lgwadmin hijack area save <ID> <マップ名> で保存してください。");
                }
                case "pos1", "pos2" -> {
                    Player player = player(sender);
                    select(player, player.getLocation(), action.equals("pos1"), false);
                }
                case "cancel" -> {
                    selections.remove(player(sender).getUniqueId());
                    sender.sendMessage("§aHIJACKエリアの選択を解除しました。");
                }
                case "save", "delete", "tp" -> {
                    if (args.length < 5) throw new IllegalArgumentException("/lgwadmin hijack area " + action + " <ID> <マップ名>");
                    String id = args[3];
                    GameMap map = resolve(String.join(" ", Arrays.copyOfRange(args, 4, args.length)));
                    if (action.equals("save")) {
                        Player player = player(sender);
                        Selection selection = selections.get(player.getUniqueId());
                        if (selection == null || selection.first == null || selection.second == null) throw new IllegalArgumentException("第1点と第2点を選択してください。wand または pos1/pos2 を使えます。");
                        if (!selection.world.getUID().equals(map.getWorld().getUID()) || !player.getWorld().getUID().equals(selection.world.getUID())) throw new IllegalArgumentException("選択した範囲・実行者・マップのワールドを一致させてください。");
                        Area3D area = selectedArea(selection.first.position, selection.first.block, selection.second.position, selection.second.block);
                        maps.saveHijackArea(map, id, area);
                        selections.remove(player.getUniqueId());
                        sender.sendMessage("§a保存しました: " + map.getMapName() + " / " + id + " " + area.getMin() + " ～ " + area.getMax() + "§7（同じIDは上書き・同じマップの試合中なら即反映）");
                    } else if (action.equals("delete")) {
                        maps.saveHijackArea(map, id, null);
                        sender.sendMessage("§a削除しました: " + map.getMapName() + " / " + id + "§7（同じマップの試合中なら即反映）");
                    } else {
                        Player player = player(sender);
                        Area3D area = maps.getHijackAreaEntries(map).get(id);
                        if (area == null) throw new IllegalArgumentException("そのエリアIDは登録されていません。area list <マップ名> で確認してください。");
                        Location target = safeDestination(map.getWorld(), area);
                        if (target == null) throw new IllegalArgumentException("エリア中央付近に安全な足場・空間が見つかりません。地形を確認してください。");
                        target.setYaw(player.getLocation().getYaw());
                        target.setPitch(player.getLocation().getPitch());
                        if (!player.teleport(target)) throw new IllegalArgumentException("テレポートがキャンセルされました。");
                        sender.sendMessage("§a" + map.getMapName() + " / " + id + " の占領範囲へTPしました（保存済み範囲）。");
                    }
                }
                case "list" -> {
                    if (args.length < 4) throw new IllegalArgumentException("/lgwadmin hijack area list <マップ名>");
                    GameMap map = resolve(String.join(" ", Arrays.copyOfRange(args, 3, args.length)));
                    var entries = maps.getHijackAreaEntries(map);
                    sender.sendMessage("§b" + map.getMapName() + " のHIJACKエリア（保存済み）: " + entries.size());
                    entries.forEach((id, area) -> sender.sendMessage("§e" + id + " §f" + area.getMin() + " ～ " + area.getMax()));
                }
                default -> {
                    sender.sendMessage("§e/lgwadmin hijack area wand|pos1|pos2|cancel");
                    sender.sendMessage("§e/lgwadmin hijack area save|delete|tp <ID> <マップ名>");
                    sender.sendMessage("§e/lgwadmin hijack area list <マップ名>");
                    sender.sendMessage("§7pos1/pos2は足元の正確な座標。wandは選択したブロック全体を含みます。保存はmaps.ymlへ反映されます。");
                }
            }
        } catch (IllegalArgumentException exception) {
            sender.sendMessage("§c" + exception.getMessage());
        } catch (Exception exception) {
            plugin.getLogger().log(Level.WARNING, "HIJACKエリアの操作に失敗しました。", exception);
            sender.sendMessage("§cエリアを保存/取得できませんでした。サーバーログを確認してください。");
        }
        return true;
    }

    public List<String> complete(CommandSender sender, String[] args) {
        if (!sender.hasPermission(PERMISSION)) return List.of();
        List<String> choices = List.of();
        if (args.length == 3) choices = List.of("wand", "pos1", "pos2", "save", "delete", "tp", "list", "cancel");
        else if (args.length == 4 && List.of("save", "delete", "tp").contains(args[2].toLowerCase(Locale.ROOT))) choices = List.of("1");
        else if ((args.length >= 4 && args[2].equalsIgnoreCase("list")) || (args.length >= 5 && List.of("save", "delete", "tp").contains(args[2].toLowerCase(Locale.ROOT)))) {
            int start = args[2].equalsIgnoreCase("list") ? 3 : 4;
            String prefix = String.join(" ", Arrays.copyOfRange(args, start, args.length)).toLowerCase(Locale.ROOT);
            int consumed = String.join(" ", Arrays.copyOfRange(args, start, args.length - 1)).length() + (args.length - start > 1 ? 1 : 0);
            return maps.getAllGameMap().stream().map(GameMap::getMapName).filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .map(name -> name.substring(consumed)).sorted().toList();
        }
        String prefix = args[args.length - 1].toLowerCase(Locale.ROOT);
        return choices.stream().filter(value -> value.startsWith(prefix)).sorted().toList();
    }

    private GameMap resolve(String name) {
        var found = maps.getAllGameMap().stream().filter(map -> map.getMapName().equalsIgnoreCase(name) || map.getMapName().equalsIgnoreCase(name.replace(' ', '_'))).toList();
        if (found.size() != 1) throw new IllegalArgumentException("マップ名を1つに特定できません: " + name);
        if (found.get(0).getWorld() == null) throw new IllegalArgumentException("マップのワールドをロードして /lgwadmin reload してください。");
        return found.get(0);
    }

    private static Player player(CommandSender sender) {
        if (!(sender instanceof Player player)) throw new IllegalArgumentException("この操作はプレイヤー専用です。");
        return player;
    }

    private boolean isWand(ItemStack item) {
        return item != null && item.getType() == Material.WOODEN_AXE && item.hasItemMeta()
                && item.getItemMeta().getPersistentDataContainer().has(wandKey, PersistentDataType.BYTE);
    }

    @EventHandler public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || event.getClickedBlock() == null || !isWand(event.getItem())) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        event.setCancelled(true);
        if (event.getPlayer().hasPermission(PERMISSION)) select(event.getPlayer(), event.getClickedBlock().getLocation(), event.getAction() == Action.LEFT_CLICK_BLOCK, true);
    }

    private void select(Player player, Location location, boolean first, boolean block) {
        Selection selection = selections.computeIfAbsent(player.getUniqueId(), ignored -> new Selection());
        if (selection.world != null && !selection.world.getUID().equals(location.getWorld().getUID())) {
            selection.first = null; selection.second = null;
            player.sendMessage("§eワールドが変わったため選択をリセットしました。");
        }
        selection.world = location.getWorld();
        Point point = new Point(location.toVector(), block);
        if (first) selection.first = point; else selection.second = point;
        player.sendMessage("§a" + (first ? "第1点" : "第2点") + ": " + point.position + (block ? "（ブロック）" : "（足元）"));
    }

    @EventHandler public void onBreak(BlockBreakEvent event) {
        if (isWand(event.getPlayer().getInventory().getItemInMainHand())) event.setCancelled(true);
    }
    @EventHandler public void onQuit(PlayerQuitEvent event) { selections.remove(event.getPlayer().getUniqueId()); }
    public void clear() { selections.clear(); }

    static Area3D selectedArea(Vector first, boolean firstBlock, Vector second, boolean secondBlock) {
        Vector min = Vector.getMinimum(first, second);
        Vector max = Vector.getMaximum(first.clone().add(new Vector(firstBlock ? 1 : 0, firstBlock ? 1 : 0, firstBlock ? 1 : 0)),
                second.clone().add(new Vector(secondBlock ? 1 : 0, secondBlock ? 1 : 0, secondBlock ? 1 : 0)));
        return new Area3D(min, max);
    }

    static Location safeDestination(World world, Area3D area) {
        Vector min = area.getMin(), max = area.getMax();
        int centerX = (int) Math.floor(min.getX() / 2 + max.getX() / 2);
        int centerZ = (int) Math.floor(min.getZ() / 2 + max.getZ() / 2);
        int bottom = Math.max(minimumHeight(world) + 1, (int) Math.ceil(min.getY()));
        int top = Math.min(Math.min(world.getMaxHeight() - 2, (int) Math.floor(max.getY())), bottom + 127);
        for (int radius = 0; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) for (int dz = -radius; dz <= radius; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) continue;
                double x = centerX + dx + .5, z = centerZ + dz + .5;
                if (x < min.getX() || x > max.getX() || z < min.getZ() || z > max.getZ()) continue;
                for (int y = top; y >= bottom; y--) {
                    var feet = world.getBlockAt(centerX + dx, y, centerZ + dz);
                    var head = world.getBlockAt(centerX + dx, y + 1, centerZ + dz);
                    var floor = world.getBlockAt(centerX + dx, y - 1, centerZ + dz);
                    if (feet.isPassable() && head.isPassable() && !feet.isLiquid() && !head.isLiquid()
                            && !HAZARDS.contains(floor.getType()) && !HAZARDS.contains(feet.getType()) && !HAZARDS.contains(head.getType())
                            && floor.getType().isSolid()) return new Location(world, x, y, z);
                }
            }
        }
        return null;
    }
    private static int minimumHeight(World world) { return 0; }
}

package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class KillActionGui implements CommandExecutor, Listener {
    private static final int PAGE_SIZE = 45;
    private final LeonGunWar plugin;
    private final KillActionManager manager;
    private final Set<UUID> saving = new HashSet<>();

    public KillActionGui(LeonGunWar plugin, KillActionManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    private static final class Menu implements InventoryHolder {
        final KillActionCatalog.Category category;
        final int page;
        List<KillActionCatalog.Entry> visible = List.of();
        Inventory inventory;

        Menu(KillActionCatalog.Category category, int page) { this.category = category; this.page = page; }
        @Override public Inventory getInventory() { return inventory; }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) { sender.sendMessage("プレイヤー専用コマンドです。"); return true; }
        if (!plugin.getMainConfig().isLobby) {
            player.sendMessage(color("&cキル演出の変更はロビー鯖で行ってください。"));
            return true;
        }
        manager.load(player, success -> {
            if (!success) player.sendMessage(color("&c選択データを読み込めません。MySQLの接続を確認してください。"));
            else openMain(player);
        });
        return true;
    }

    private void openMain(Player player) {
        Menu menu = new Menu(null, 0);
        menu.inventory = Bukkit.createInventory(menu, 27, "キル演出");
        menu.inventory.setItem(11, item(Material.NAME_TAG, "&eキルログ", List.of("&7ログの文面を選択")));
        menu.inventory.setItem(13, item(Material.NOTE_BLOCK, "&bキル音", List.of("&7キル時の音を選択")));
        menu.inventory.setItem(15, item(Material.FIREWORK_STAR, "&dパーティクル", List.of("&7キル時の粒子を選択")));
        player.openInventory(menu.inventory);
    }

    private List<KillActionCatalog.Entry> sorted(Player player, KillActionCatalog.Category category) {
        List<KillActionCatalog.Entry> list = new ArrayList<>(manager.catalog().entries(category).values());
        list.sort(Comparator.<KillActionCatalog.Entry>comparingInt(entry -> player.hasPermission(entry.permission()) ? 0 : 1)
                .thenComparingInt(entry -> entry.rarity().ordinal()).thenComparing(KillActionCatalog.Entry::id));
        return list;
    }

    private void openCategory(Player player, KillActionCatalog.Category category, int requestedPage) {
        List<KillActionCatalog.Entry> entries = sorted(player, category);
        int lastPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, lastPage));
        Menu menu = new Menu(category, page);
        menu.inventory = Bukkit.createInventory(menu, 54, switch (category) {
            case SOUND -> "キル音";
            case LOG -> "キルログ";
            case PARTICLE -> "キルパーティクル";
        });
        int first = page * PAGE_SIZE;
        menu.visible = List.copyOf(entries.subList(first, Math.min(first + PAGE_SIZE, entries.size())));
        for (int i = first; i < Math.min(first + PAGE_SIZE, entries.size()); i++) {
            KillActionCatalog.Entry entry = entries.get(i);
            boolean unlocked = player.hasPermission(entry.permission());
            List<String> lore = new ArrayList<>(entry.lore());
            lore.add("&7レア度: " + rarityColor(entry.rarity()) + entry.rarity().name().toLowerCase());
            if (unlocked) {
                lore.add("&e左クリック: 選択 / 右クリック: プレビュー");
                if (entry.id().equals(manager.selectedId(player, category))) lore.add("&a選択中");
            } else {
                lore.add("&c未開放");
                lore.addAll(List.of(entry.unlockDescription().split("\\|", -1)));
            }
            menu.inventory.setItem(i - first, item(unlocked ? entry.icon() : Material.BARRIER, entry.displayName(), lore));
        }
        if (page > 0) menu.inventory.setItem(45, item(Material.ARROW, "&e前のページ", List.of()));
        menu.inventory.setItem(47, item(Material.BARRIER, "&7標準に戻す", manager.selectedId(player, category) == null
                ? List.of("&7選択を解除して既存の演出を使用", "&a選択中")
                : List.of("&7選択を解除して既存の演出を使用")));
        menu.inventory.setItem(49, item(Material.OAK_DOOR, "&eカテゴリへ戻る", List.of()));
        if (page < lastPage) menu.inventory.setItem(53, item(Material.ARROW, "&e次のページ", List.of()));
        player.openInventory(menu.inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0
                || event.getRawSlot() >= event.getView().getTopInventory().getSize()) return;
        int slot = event.getRawSlot();
        if (menu.category == null) {
            switch (slot) {
                case 11 -> openCategory(player, KillActionCatalog.Category.LOG, 0);
                case 13 -> openCategory(player, KillActionCatalog.Category.SOUND, 0);
                case 15 -> openCategory(player, KillActionCatalog.Category.PARTICLE, 0);
            }
            return;
        }
        if (slot == 45 && menu.page > 0) { openCategory(player, menu.category, menu.page - 1); return; }
        if (slot == 49) { openMain(player); return; }
        if (slot == 53) { openCategory(player, menu.category, menu.page + 1); return; }
        if (slot == 47) {
            if (event.isLeftClick()) save(player, menu, null);
            return;
        }
        if (slot >= PAGE_SIZE || saving.contains(player.getUniqueId())) return;
        if (slot >= menu.visible.size()) return;
        KillActionCatalog.Entry shown = menu.visible.get(slot);
        KillActionCatalog.Entry entry = manager.catalog().get(menu.category, shown.id());
        if (entry == null) { player.sendMessage(color("&cこの演出は設定から削除されました。")); return; }
        if (!player.hasPermission(entry.permission())) {
            player.sendMessage(color("&c未開放: " + entry.unlockDescription()));
            return;
        }
        if (event.isRightClick()) manager.preview(player, entry);
        else if (event.isLeftClick()) save(player, menu, entry.id());
    }

    private void save(Player player, Menu menu, String id) {
        if (!saving.add(player.getUniqueId())) return;
        manager.select(player, menu.category, id, success -> {
            saving.remove(player.getUniqueId());
            if (!player.isOnline()) return;
            if (!success) player.sendMessage(color("&c演出を保存できませんでした。権限とMySQLを確認してください。"));
            else {
                player.sendMessage(color("&aキル演出を保存しました。"));
                openCategory(player, menu.category, menu.page);
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }

    private static ItemStack item(Material material, String name, List<String> loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(loreLines.stream().map(KillActionGui::color).toList());
        item.setItemMeta(meta);
        return item;
    }

    private static String rarityColor(KillActionCatalog.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "&7"; case UNCOMMON -> "&a"; case RARE -> "&9";
            case EPIC -> "&5"; case LEGENDARY -> "&6"; case SPECIAL -> "&c";
        };
    }

    private static String color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }
}

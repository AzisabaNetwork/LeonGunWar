package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.TabExecutor;
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

public final class KillActionGui implements TabExecutor, Listener {
    private static final int PAGE_SIZE = 45;
    private final LeonGunWar plugin;
    private final KillActionManager manager;
    private final KillActionTickets tickets;
    private final Set<UUID> saving = new HashSet<>();

    public KillActionGui(LeonGunWar plugin, KillActionManager manager) {
        this(plugin, manager, null);
    }

    public KillActionGui(LeonGunWar plugin, KillActionManager manager, KillActionTickets tickets) {
        this.plugin = plugin;
        this.manager = manager;
        this.tickets = tickets;
    }

    private static final class Menu implements InventoryHolder {
        final KillActionCatalog.Category category;
        final int page;
        final boolean readOnly;
        List<KillActionCatalog.Entry> visible = List.of();
        Inventory inventory;

        Menu(KillActionCatalog.Category category, int page, boolean readOnly) {
            this.category = category; this.page = page; this.readOnly = readOnly;
        }
        @Override public Inventory getInventory() { return inventory; }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) return tickets != null && tickets.onCommand(sender, command, label, args);
        if (!(sender instanceof Player player)) { sender.sendMessage("プレイヤー専用コマンドです。"); return true; }
        manager.load(player, success -> {
            if (!success) player.sendMessage(color("&c選択データを読み込めません。MySQLの接続を確認してください。"));
            else {
                clickSound(player);
                openMain(player);
            }
        });
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tickets == null ? List.of() : tickets.onTabComplete(sender, command, alias, args);
    }

    private void openMain(Player player) {
        Menu menu = new Menu(null, 0, !plugin.getMainConfig().isLobby);
        menu.inventory = Bukkit.createInventory(menu, 27, color("&5✦ &dキル演出 &5✦" + (menu.readOnly ? " &7確認専用" : "")));
        fill(menu.inventory, 0, 27, Material.BLACK_STAINED_GLASS_PANE);
        fill(menu.inventory, 0, 9, Material.PURPLE_STAINED_GLASS_PANE);
        menu.inventory.setItem(4, item(Material.NETHER_STAR, "&d&lキル演出コレクション",
                List.of(menu.readOnly ? "&7カテゴリを選んで現在の演出を確認" : "&7カテゴリを選んで演出を設定",
                        "&8ロビーで選択した演出は試合でも使用されます")));
        menu.inventory.setItem(11, categoryItem(player, KillActionCatalog.Category.LOG,
                Material.NAME_TAG, "&e&lキルログ", menu.readOnly ? "&7キル時のメッセージを確認" : "&7キル時のメッセージを変更"));
        menu.inventory.setItem(13, categoryItem(player, KillActionCatalog.Category.SOUND,
                Material.NOTE_BLOCK, "&b&lキル音", menu.readOnly ? "&7キル時のサウンドを確認" : "&7キル時のサウンドを変更"));
        menu.inventory.setItem(15, categoryItem(player, KillActionCatalog.Category.PARTICLE,
                Material.FIREWORK_STAR, "&d&lパーティクル", menu.readOnly ? "&7キル時のエフェクトを確認" : "&7キル時のエフェクトを変更"));
        menu.inventory.setItem(22, item(Material.BOOK, "&f操作ガイド",
                menu.readOnly ? List.of("&7試合鯖では確認のみできます", "&7変更・解除・プレビューはロビーで行ってね")
                        : List.of("&e左クリック &7候補を選択", "&b右クリック &7本人だけプレビュー",
                        "&7未解放の候補はバリアで表示")));
        player.openInventory(menu.inventory);
    }

    private ItemStack categoryItem(Player player, KillActionCatalog.Category category,
                                   Material icon, String name, String description) {
        long unlocked = manager.catalog().entries(category).values().stream()
                .filter(entry -> manager.isUnlocked(player, entry)).count();
        String selectedId = manager.selectedId(player, category);
        KillActionCatalog.Entry selected = manager.catalog().get(category, selectedId);
        String selectedName = selected == null || !manager.isUnlocked(player, selected)
                ? "&7標準" : selected.displayName();
        return item(icon, name, List.of(description, "&8────────────", "&7解放済み: &a" + unlocked
                + " &7/ &f" + manager.catalog().entries(category).size(),
                "&7現在の設定: " + selectedName, "&8保存ID: " + (selectedId == null ? "標準" : selectedId),
                "&eクリックして開く"));
    }

    private List<KillActionCatalog.Entry> sorted(Player player, KillActionCatalog.Category category) {
        List<KillActionCatalog.Entry> list = new ArrayList<>(manager.catalog().entries(category).values());
        list.sort(Comparator.<KillActionCatalog.Entry>comparingInt(entry -> manager.isUnlocked(player, entry) ? 0 : 1)
                .thenComparingInt(entry -> entry.rarity().ordinal()).thenComparing(KillActionCatalog.Entry::id));
        return list;
    }

    private void openCategory(Player player, KillActionCatalog.Category category, int requestedPage) {
        List<KillActionCatalog.Entry> entries = sorted(player, category);
        int lastPage = Math.max(0, (entries.size() - 1) / PAGE_SIZE);
        int page = Math.max(0, Math.min(requestedPage, lastPage));
        Menu menu = new Menu(category, page, !plugin.getMainConfig().isLobby);
        menu.inventory = Bukkit.createInventory(menu, 54, color("&5✦ &d" + switch (category) {
            case SOUND -> "キル音";
            case LOG -> "キルログ";
            case PARTICLE -> "パーティクル";
        } + " &5✦" + (menu.readOnly ? " &7確認専用" : "")));
        fill(menu.inventory, 0, PAGE_SIZE, Material.GRAY_STAINED_GLASS_PANE);
        fill(menu.inventory, PAGE_SIZE, 54, Material.PURPLE_STAINED_GLASS_PANE);
        int first = page * PAGE_SIZE;
        menu.visible = List.copyOf(entries.subList(first, Math.min(first + PAGE_SIZE, entries.size())));
        for (int i = first; i < Math.min(first + PAGE_SIZE, entries.size()); i++) {
            KillActionCatalog.Entry entry = entries.get(i);
            boolean unlocked = manager.isUnlocked(player, entry);
            boolean selected = entry.id().equals(manager.selectedId(player, category));
            List<String> lore = new ArrayList<>(entry.lore());
            lore.add("&8────────────");
            lore.add("&7レア度: " + rarityColor(entry.rarity()) + entry.rarity().name().toLowerCase());
            if (unlocked) {
                if (selected) lore.add("&a✔ 選択中");
                if (menu.readOnly) lore.add("&8確認専用・変更はロビーで行ってね");
                else {
                    lore.add("&e左クリック &7選択");
                    lore.add("&b右クリック &7プレビュー");
                }
            } else {
                lore.add("&c未開放");
                lore.addAll(List.of(entry.unlockDescription().split("\\|", -1)));
            }
            menu.inventory.setItem(i - first, item(unlocked ? entry.icon() : Material.BARRIER,
                    rarityColor(entry.rarity()) + "✦ &r" + entry.displayName(), lore, unlocked && selected));
        }
        if (page > 0) menu.inventory.setItem(45, item(Material.ARROW, "&e◀ 前のページ", List.of("&7クリックして移動")));
        menu.inventory.setItem(47, menu.readOnly
                ? item(Material.BOOK, "&7確認専用", List.of("&7演出の変更・解除はロビーで行ってね"))
                : item(Material.BARRIER, "&c標準に戻す", manager.selectedId(player, category) == null
                ? List.of("&7選択を解除して既存の演出を使用", "&a✔ 選択中")
                : List.of("&7選択を解除して既存の演出を使用", "&e左クリックで設定")));
        menu.inventory.setItem(49, item(Material.OAK_DOOR, "&eカテゴリへ戻る", List.of("&7クリックしてカテゴリ一覧へ")));
        menu.inventory.setItem(51, item(Material.BOOK, "&dページ " + (page + 1) + " / " + (lastPage + 1),
                List.of("&7候補: &f" + entries.size(), menu.readOnly ? "&7現在の設定と解放状況を確認できます"
                        : "&7解放済みの候補を左クリックで選択")));
        if (page < lastPage) menu.inventory.setItem(53, item(Material.ARROW, "&e次のページ ▶", List.of("&7クリックして移動")));
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
                case 11 -> { clickSound(player); openCategory(player, KillActionCatalog.Category.LOG, 0); }
                case 13 -> { clickSound(player); openCategory(player, KillActionCatalog.Category.SOUND, 0); }
                case 15 -> { clickSound(player); openCategory(player, KillActionCatalog.Category.PARTICLE, 0); }
            }
            return;
        }
        if (slot == 45 && menu.page > 0) { clickSound(player); openCategory(player, menu.category, menu.page - 1); return; }
        if (slot == 49) { clickSound(player); openMain(player); return; }
        if (slot == 53 && menu.page < Math.max(0,
                (manager.catalog().entries(menu.category).size() - 1) / PAGE_SIZE)) {
            clickSound(player);
            openCategory(player, menu.category, menu.page + 1);
            return;
        }
        if (menu.readOnly || !plugin.getMainConfig().isLobby) return;
        if (slot == 47) {
            if (event.isLeftClick()) { clickSound(player); save(player, menu, null); }
            return;
        }
        if (slot >= PAGE_SIZE || saving.contains(player.getUniqueId())) return;
        if (slot >= menu.visible.size()) return;
        KillActionCatalog.Entry shown = menu.visible.get(slot);
        KillActionCatalog.Entry entry = manager.catalog().get(menu.category, shown.id());
        if (entry == null) { deniedSound(player); player.sendMessage(color("&cこの演出は設定から削除されました。")); return; }
        if (!manager.isUnlocked(player, entry)) {
            deniedSound(player);
            player.sendMessage(color("&c未開放: " + entry.unlockDescription()));
            return;
        }
        if (event.isRightClick()) {
            if (menu.category != KillActionCatalog.Category.SOUND) clickSound(player);
            manager.preview(player, entry);
        } else if (event.isLeftClick()) {
            clickSound(player);
            save(player, menu, entry.id());
        }
    }

    private void save(Player player, Menu menu, String id) {
        if (menu.readOnly || !plugin.getMainConfig().isLobby) return;
        if (!saving.add(player.getUniqueId())) return;
        manager.select(player, menu.category, id, success -> {
            saving.remove(player.getUniqueId());
            if (Bukkit.getPlayer(player.getUniqueId()) != player) return;
            if (!success) {
                deniedSound(player);
                player.sendMessage(color("&c演出を保存できませんでした。権限とMySQLを確認してください。"));
            }
            else {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
                player.sendMessage(color("&aキル演出を保存しました。"));
                if (player.getOpenInventory().getTopInventory() == menu.inventory && plugin.getMainConfig().isLobby)
                    openCategory(player, menu.category, menu.page);
            }
        });
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }

    private static ItemStack item(Material material, String name, List<String> loreLines) {
        return item(material, name, loreLines, false);
    }

    private static ItemStack item(Material material, String name, List<String> loreLines, boolean glint) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(loreLines.stream().map(KillActionGui::color).toList());
        if (glint) meta.setEnchantmentGlintOverride(true);
        item.setItemMeta(meta);
        return item;
    }

    private static void fill(Inventory inventory, int first, int end, Material material) {
        ItemStack background = item(material, " ", List.of());
        for (int slot = first; slot < end; slot++) inventory.setItem(slot, background);
    }

    private static void clickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 1.2f);
    }

    private static void deniedSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7f, 0.6f);
    }

    private static String rarityColor(KillActionCatalog.Rarity rarity) {
        return switch (rarity) {
            case COMMON -> "&7"; case UNCOMMON -> "&a"; case RARE -> "&9";
            case EPIC -> "&5"; case LEGENDARY -> "&6"; case SPECIAL -> "&c";
        };
    }

    private static String color(String text) { return ChatColor.translateAlternateColorCodes('&', text); }
}

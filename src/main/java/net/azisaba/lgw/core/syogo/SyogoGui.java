package net.azisaba.lgw.core.syogo;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.configs.SyogoConfig;
import org.bukkit.Bukkit;
import net.azisaba.lgw.core.util.LegacyColors;
import org.bukkit.Material;
import org.bukkit.Sound;
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
import java.util.List;

public final class SyogoGui implements Listener {
    private final LeonGunWar plugin;
    private final SyogoManager manager;
    public SyogoGui(LeonGunWar plugin, SyogoManager manager) { this.plugin = plugin; this.manager = manager; }
    private static final class Menu implements InventoryHolder {
        final List<String> ids;
        final int page;
        final boolean readOnly;
        Inventory inventory;
        boolean saving;
        Menu(List<String> ids, int page, boolean readOnly) {
            this.ids = List.copyOf(ids); this.page = page; this.readOnly = readOnly;
        }
        @Override public Inventory getInventory() { return inventory; }
    }
    public void open(Player player) {
        player.sendMessage(color("&7称号データを読み込み中…"));
        manager.load(player, success -> {
            if (success) show(player, 0);
            else player.sendMessage(color("&c称号を読み込めませんでした。しばらくしてから再試行してね。"));
        });
    }
    private void show(Player player, int requestedPage) {
        var snapshot = manager.snapshot(player);
        List<SyogoConfig.Entry> entries = plugin.getSyogoConfig().entries().values().stream()
                .filter(entry -> snapshot.owned().contains(entry.id())).sorted(java.util.Comparator.comparing(SyogoConfig.Entry::id)).toList();
        int page = Math.max(0, Math.min(requestedPage, Math.max(0, (entries.size() - 1) / 45)));
        List<SyogoConfig.Entry> visible = entries.stream().skip(page * 45L).limit(45).toList();
        Menu menu = new Menu(visible.stream().map(SyogoConfig.Entry::id).toList(), page, !plugin.getMainConfig().isLobby);
        menu.inventory = Bukkit.createInventory(menu, 54, color("&8✦ &6称号コレクション &8✦ &7" + (page + 1)
                + (menu.readOnly ? " 確認専用" : "")));
        for (int i = 45; i < 54; i++) menu.inventory.setItem(i, item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of(), false));
        for (int i = 0; i < visible.size(); i++) {
            SyogoConfig.Entry entry = visible.get(i);
            boolean selected = entry.id().equals(snapshot.activeId());
            List<String> lore = new ArrayList<>(entry.lore());
            lore.add("");
            lore.add(selected ? "&a✔ 装備中" : menu.readOnly ? "&7所持済み" : "&eクリックして装備");
            if (menu.readOnly) lore.add("&8確認専用・変更はロビーで行ってね");
            menu.inventory.setItem(i, item(entry.icon(), entry.display(), lore, selected));
        }
        if (entries.isEmpty()) menu.inventory.setItem(22, item(Material.NAME_TAG, "&7所持している称号はありません", List.of("&7称号獲得券を右クリックして集めよう！"), false));
        if (page > 0) menu.inventory.setItem(45, item(Material.ARROW, "&e前のページ", List.of(), false));
        menu.inventory.setItem(49, menu.readOnly
                ? item(Material.NAME_TAG, "&e現在の称号: " + (snapshot.activeId() == null ? "&7なし" : manager.display(player)),
                        List.of("&8保存ID: " + (snapshot.activeId() == null ? "なし" : snapshot.activeId()),
                                "&7付け替え・解除はロビーで行ってね"), false)
                : item(Material.BARRIER, "&c称号を外す", List.of("&7所持している称号はなくなりません"), false));
        if ((page + 1) * 45 < entries.size()) menu.inventory.setItem(53, item(Material.ARROW, "&e次のページ", List.of(), false));
        player.openInventory(menu.inventory);
    }
    @EventHandler public void click(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu menu)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player) || event.getRawSlot() < 0 || event.getRawSlot() >= 54 || menu.saving) return;
        if (!player.hasPermission("leongunwar.command.syogo")) { player.closeInventory(); return; }
        int slot = event.getRawSlot();
        if (slot == 45 && menu.page > 0) { clickSound(player); show(player, menu.page - 1); return; }
        if (slot == 53 && event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.ARROW) { clickSound(player); show(player, menu.page + 1); return; }
        if (menu.readOnly || !plugin.getMainConfig().isLobby) return;
        clickSound(player);
        if (slot != 49 && slot >= menu.ids.size()) return;
        if (slot == 49 && !player.hasPermission("leongunwar.syogo.remove")) return;
        String id = slot == 49 ? null : menu.ids.get(slot);
        menu.saving = true;
        manager.select(player, id, result -> {
            menu.saving = false;
            if (!player.isOnline()) return;
            boolean success = result != null && result.status() == SyogoStore.Status.OK;
            player.sendMessage(color(success ? "&a称号を保存しました！" : "&c保存できませんでした。再度GUIを開いてね。"));
            player.playSound(player.getLocation(), success ? Sound.ENTITY_EXPERIENCE_ORB_PICKUP : Sound.BLOCK_NOTE_BLOCK_BASS, 0.6f, 1f);
            if (player.getOpenInventory().getTopInventory() == menu.inventory && plugin.getMainConfig().isLobby) show(player, menu.page);
        });
    }
    @EventHandler public void drag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof Menu) event.setCancelled(true);
    }
    private static void clickSound(Player player) {
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }
    private static ItemStack item(Material material, String name, List<String> lore, boolean glint) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(color(name));
        meta.setLore(lore.stream().map(SyogoGui::color).toList());
        meta.setEnchantmentGlintOverride(glint);
        item.setItemMeta(meta);
        return item;
    }
    public static String color(String text) { return LegacyColors.color(text); }
}

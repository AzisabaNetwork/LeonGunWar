package net.azisaba.lgw.core.killaction;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import static net.azisaba.lgw.core.util.LegacyColors.color;

/** A ticket records an issued token and an effect, never a console command or LP mutation. */
public final class KillActionTickets implements Listener, TabExecutor {
    private static final String PERMISSION = "leongunwar.killaction.item";
    private final LeonGunWar plugin;
    private final KillActionManager manager;
    private final KillActionTicketStore store;
    private final NamespacedKey tokenKey, categoryKey, idKey, syogoKey;
    private final Set<UUID> pending = new HashSet<>();

    public KillActionTickets(LeonGunWar plugin, KillActionManager manager, KillActionTicketStore store) {
        this.plugin = plugin; this.manager = manager; this.store = store;
        tokenKey = new NamespacedKey(plugin, "killaction_ticket");
        categoryKey = new NamespacedKey(plugin, "killaction_category");
        idKey = new NamespacedKey(plugin, "killaction_id");
        syogoKey = new NamespacedKey(plugin, "syogo_ticket");
    }

    static KillActionCatalog.Category category(String input) {
        try { return KillActionCatalog.Category.valueOf(input.toUpperCase(Locale.ROOT)); }
        catch (IllegalArgumentException exception) { return null; }
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission(PERMISSION)) { sender.sendMessage(color("&c権限がありません。")); return true; }
        String sub = args[0].toLowerCase(Locale.ROOT);
        boolean bind = sub.equals("bind");
        if (!bind && !sub.equals("ticket") && !sub.equals("item")) return false;
        if (bind ? args.length != 3 : args.length < 4 || args.length > 5) return false;
        Player target = bind ? (sender instanceof Player p ? p : null) : Bukkit.getPlayerExact(args[1]);
        if (target == null) { sender.sendMessage(color("&cオンラインのプレイヤーを指定してね。bindはプレイヤー専用です。")); return true; }
        KillActionCatalog.Category category = category(args[bind ? 1 : 2]);
        KillActionCatalog.Entry entry = category == null ? null : manager.catalog().get(category, args[bind ? 2 : 3]);
        if (entry == null) { sender.sendMessage(color("&cカテゴリまたは演出IDが見つかりません。")); return true; }
        int amount;
        try { amount = !bind && args.length == 5 ? Integer.parseInt(args[4]) : 1; }
        catch (NumberFormatException exception) { sender.sendMessage(color("&c個数は1〜64の整数にしてね。")); return true; }
        issue(sender, target, entry, amount, bind);
        return true;
    }

    public void issue(CommandSender sender, Player target, KillActionCatalog.Entry entry, int amount, boolean bind) {
        if (amount < 1 || amount > 64) { sender.sendMessage(color("&c個数は1〜64にしてね。")); return; }
        ItemStack original = bind ? target.getInventory().getItemInMainHand().clone() : new ItemStack(Material.PAPER, amount);
        if (original.getType().isAir() || original.getAmount() > 64 || original.hasItemMeta()
                && (original.getItemMeta().getPersistentDataContainer().has(tokenKey)
                || original.getItemMeta().getPersistentDataContainer().has(syogoKey))) {
            sender.sendMessage(color("&c獲得券が設定されていないアイテムを持ってね（最大64個）。")); return;
        }
        if (!pending.add(target.getUniqueId())) { sender.sendMessage(color("&c処理中です。少し待ってね。")); return; }
        int slot = target.getInventory().getHeldItemSlot();
        store.issue(entry, original.getAmount()).whenComplete((token, error) -> manager.onMain(() -> {
            pending.remove(target.getUniqueId());
            if (error != null) {
                plugin.getLogger().warning("Cannot issue KillAction ticket: " + error);
                sender.sendMessage(color("&c獲得券を発行できませんでした。DB設定を確認してね。")); return;
            }
            if (Bukkit.getPlayer(target.getUniqueId()) != target || bind && !original.equals(target.getInventory().getItem(slot))) {
                store.discard(token).exceptionally(failure -> { plugin.getLogger().warning("Cannot discard KillAction ticket: " + failure); return null; });
                sender.sendMessage(color("&c退出またはアイテム変更のため、発行を中止しました。")); return;
            }
            ItemStack ticket = original.clone();
            ItemMeta meta = ticket.getItemMeta();
            if (!bind) meta.setDisplayName(color("&d✦ &fキル演出獲得券 &7» " + entry.displayName()));
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : List.of());
            lore.addAll(List.of(color("&7獲得演出: " + entry.category().name().toLowerCase(Locale.ROOT) + " / " + entry.displayName()),
                    color("&eロビーで右クリックして獲得（1個消費）"), color("&7取得済みの場合は消費しません")));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.STRING, token.toString());
            meta.getPersistentDataContainer().set(categoryKey, PersistentDataType.STRING, entry.category().name());
            meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, entry.id());
            ticket.setItemMeta(meta);
            if (bind) target.getInventory().setItem(slot, ticket);
            else target.getInventory().addItem(ticket).values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
            sender.sendMessage(color("&aキル演出獲得券を発行しました: " + entry.id() + " ×" + ticket.getAmount()));
        }));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void use(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || event.getHand() == null) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        var data = item.getItemMeta().getPersistentDataContainer();
        String rawToken = data.get(tokenKey, PersistentDataType.STRING);
        if (rawToken == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        ItemStack main = player.getInventory().getItemInMainHand();
        if (event.getHand() == EquipmentSlot.OFF_HAND && main.hasItemMeta()
                && (main.getItemMeta().getPersistentDataContainer().has(tokenKey)
                || main.getItemMeta().getPersistentDataContainer().has(syogoKey))) return;
        if (!plugin.getMainConfig().isLobby) { player.sendMessage(color("&c獲得券はロビーで使ってね。")); return; }
        String rawCategory = data.get(categoryKey, PersistentDataType.STRING);
        var category = rawCategory == null ? null : category(rawCategory);
        var entry = category == null ? null : manager.catalog().get(category, data.get(idKey, PersistentDataType.STRING));
        if (entry == null) { player.sendMessage(color("&cこの演出は現在設定されていません。運営に確認してね。")); return; }
        UUID token;
        try { token = UUID.fromString(rawToken); }
        catch (IllegalArgumentException exception) { player.sendMessage(color("&c獲得券のデータが正しくありません。")); return; }
        if (!pending.add(player.getUniqueId())) return;
        manager.redeem(player, token, entry, result -> {
            pending.remove(player.getUniqueId());
            if (Bukkit.getPlayer(player.getUniqueId()) != player) return;
            if (result == null) { player.sendMessage(color("&c取得できませんでした。券は消費していません。再試行してね。")); return; }
            switch (result.status()) {
                case OK -> {
                    consume(player, rawToken);
                    player.sendMessage(color("&aキル演出を獲得しました！ &e/killaction &aで選択できるよ。"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.3f);
                }
                case ALREADY_OWNED -> player.sendMessage(color("&eこの演出は取得済みです。券は消費しません。"));
                case INVALID -> player.sendMessage(color("&cこの獲得券は無効か、発行分を使い切っています。"));
            }
        });
    }

    private void consume(Player player, String token) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta() || !token.equals(item.getItemMeta().getPersistentDataContainer().get(tokenKey, PersistentDataType.STRING))) continue;
            if (item.getAmount() <= 1) player.getInventory().setItem(i, null);
            else { item.setAmount(item.getAmount() - 1); player.getInventory().setItem(i, item); }
            return;
        }
        plugin.getLogger().warning("KillAction granted but ticket left inventory before consumption: " + player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST) public void click(InventoryClickEvent event) {
        if (pending.contains(event.getWhoClicked().getUniqueId())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST) public void drag(InventoryDragEvent event) {
        if (pending.contains(event.getWhoClicked().getUniqueId())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST) public void drop(PlayerDropItemEvent event) {
        if (pending.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }
    @EventHandler(priority = EventPriority.HIGHEST) public void swap(PlayerSwapHandItemsEvent event) {
        if (pending.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @Override public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission(PERMISSION)) return List.of();
        List<String> values = new ArrayList<>();
        String sub = args.length == 0 ? "" : args[0].toLowerCase(Locale.ROOT);
        boolean bind = sub.equals("bind"), ticket = sub.equals("ticket") || sub.equals("item");
        if (args.length == 1) values.addAll(List.of("ticket", "bind"));
        if (ticket && args.length == 2) Bukkit.getOnlinePlayers().forEach(player -> values.add(player.getName()));
        if (bind && args.length == 2 || ticket && args.length == 3) values.addAll(List.of("sound", "log", "particle"));
        if (bind && args.length == 3 || ticket && args.length == 4) {
            var category = category(args[bind ? 1 : 2]);
            if (category != null) values.addAll(manager.catalog().entries(category).keySet());
        }
        if (ticket && args.length == 5) values.addAll(List.of("1", "16", "64"));
        String prefix = args.length == 0 ? "" : args[args.length - 1].toLowerCase(Locale.ROOT);
        return values.stream().filter(value -> value.toLowerCase(Locale.ROOT).startsWith(prefix)).sorted().toList();
    }
}

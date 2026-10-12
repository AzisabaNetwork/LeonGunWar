package net.azisaba.lgw.core.syogo;

import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
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
import java.util.Set;
import java.util.UUID;

import static net.azisaba.lgw.core.syogo.SyogoGui.color;

/** Tickets carry a constrained title ID and a database-issued token. */
public final class SyogoTickets implements Listener {
    private final LeonGunWar plugin;
    private final SyogoManager manager;
    private final NamespacedKey tokenKey;
    private final NamespacedKey idKey;
    private final Set<UUID> pending = new HashSet<>();

    public SyogoTickets(LeonGunWar plugin, SyogoManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        tokenKey = new NamespacedKey(plugin, "syogo_ticket");
        idKey = new NamespacedKey(plugin, "syogo_id");
    }

    public void issue(CommandSender sender, Player target, String id, int amount, boolean bind) {
        if (!plugin.getSyogoConfig().entries().containsKey(id) || amount < 1 || amount > 64) {
            sender.sendMessage(color("&c称号IDまたは個数が正しくありません（1〜64個）。")); return;
        }
        ItemStack original = bind ? target.getInventory().getItemInMainHand().clone() : new ItemStack(Material.PAPER, amount);
        if (original.getType().isAir() || original.hasItemMeta() && (original.getItemMeta().getPersistentDataContainer().has(tokenKey)
                || original.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "killaction_ticket")))) {
            sender.sendMessage(color("&c未設定のアイテムをメインハンドに持ってね。")); return;
        }
        if (!pending.add(target.getUniqueId())) { sender.sendMessage(color("&c処理中です。少し待ってね。")); return; }
        int slot = target.getInventory().getHeldItemSlot();
        String display = plugin.getSyogoConfig().entries().get(id).display();
        manager.store().issue(id, bind ? original.getAmount() : amount).whenComplete((token, error) -> manager.onMain(() -> {
            pending.remove(target.getUniqueId());
            if (error != null) {
                plugin.getLogger().warning("Cannot issue title ticket: " + error);
                sender.sendMessage(color("&c獲得券を発行できませんでした。DB設定を確認してね。")); return;
            }
            if (org.bukkit.Bukkit.getPlayer(target.getUniqueId()) != target
                    || bind && !original.equals(target.getInventory().getItem(slot))) {
                manager.store().discard(token).exceptionally(failure -> { plugin.getLogger().warning("Cannot discard title ticket: " + failure); return null; });
                sender.sendMessage(color("&cプレイヤーが退出したかアイテムが変わったため、発行を中止しました。")); return;
            }
            ItemStack ticket = original.clone();
            ItemMeta meta = ticket.getItemMeta();
            if (!bind) meta.setDisplayName(color("&6✦ &f称号獲得券 &7» " + display));
            List<String> lore = new ArrayList<>(meta.hasLore() ? meta.getLore() : List.of());
            lore.addAll(List.of(color("&7獲得称号: " + display), color("&e右クリックで獲得（1個消費）"), color("&7取得済みの場合は消費しません")));
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(tokenKey, PersistentDataType.STRING, token.toString());
            meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, id);
            ticket.setItemMeta(meta);
            if (bind) target.getInventory().setItem(slot, ticket);
            else target.getInventory().addItem(ticket).values().forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
            sender.sendMessage(color("&a称号獲得券を発行しました: " + id + " ×" + ticket.getAmount()));
        }));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void use(PlayerInteractEvent event) {
        if (!event.getAction().isRightClick() || event.getHand() == null) return;
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;
        String rawToken = item.getItemMeta().getPersistentDataContainer().get(tokenKey, PersistentDataType.STRING);
        if (rawToken == null) return;
        event.setCancelled(true);
        Player player = event.getPlayer();
        if (event.getHand() == EquipmentSlot.OFF_HAND && player.getInventory().getItemInMainHand().hasItemMeta()
                && (player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(tokenKey)
                || player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "killaction_ticket")))) return;
        if (!plugin.getMainConfig().isLobby) { player.sendMessage(color("&c称号獲得券はロビーで使ってね。")); return; }
        if (pending.contains(player.getUniqueId())) return;
        String id = item.getItemMeta().getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        if (id == null || !plugin.getSyogoConfig().entries().containsKey(id)) {
            player.sendMessage(color("&cこの称号は現在設定されていません。運営に確認してね。")); return;
        }
        UUID token;
        try { token = UUID.fromString(rawToken); }
        catch (IllegalArgumentException ex) { player.sendMessage(color("&c獲得券のデータが正しくありません。")); return; }
        pending.add(player.getUniqueId());
        manager.change(player.getUniqueId(), () -> manager.store().redeem(player.getUniqueId(), player.getName(), token, id), result -> {
            pending.remove(player.getUniqueId());
            if (org.bukkit.Bukkit.getPlayer(player.getUniqueId()) != player) return;
            if (result == null) { player.sendMessage(color("&c取得できませんでした。獲得券は消費していません。再試行してね。")); return; }
            switch (result.status()) {
                case OK -> {
                    consume(player, rawToken);
                    player.sendMessage(color("&a称号を獲得しました！ &e/lsyogo &aで装備できるよ。"));
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.3f);
                }
                case ALREADY_OWNED -> player.sendMessage(color("&eこの称号は取得済みです。獲得券は消費しません。"));
                default -> player.sendMessage(color("&cこの獲得券は無効か、発行分を使い切っています。"));
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
        plugin.getLogger().warning("Title granted but ticket moved out of inventory before consumption: " + player.getUniqueId());
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
}

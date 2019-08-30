package net.azisaba.lgw.core.listeners.signs;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.base.Strings;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.distributors.DefaultTeamDistributor;
import net.azisaba.lgw.core.distributors.KDTeamDistributor;
import net.azisaba.lgw.core.distributors.TeamDistributor;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;

/**
 *
 * 次に実行する試合の種類を指定する看板 ACTIVEとINACTIVEを切り替えるときはスニークをしながら右クリックで可能
 * 破壊するときはスニークしながら左クリックで可能
 *
 * @author siloneco
 *
 */
public class MatchModeSignListener implements Listener {

    private final ItemStack defaultItem, kdItem;

    public MatchModeSignListener() {
        defaultItem = create(Material.EMERALD_BLOCK, Chat.f("&e通常のチーム分け&aで開始！"));
        kdItem = create(Material.DIAMOND_BLOCK, Chat.f("&cK/Dのチーム分け&aで開始！"));
    }

    /**
     * エントリー看板をクリックしたことを検知し、プレイヤーを追加するリスナー
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onClickSign(PlayerInteractEvent e) {
        // ブロックをクリックしていなければreturn
        if ( e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK ) {
            return;
        }

        // プレイヤー / ブロック取得
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        // 権限を持っており スニーク + クリックならreturn
        if ( p.hasPermission("leongunwar.entrysign.changestate") && p.isSneaking() ) {
            return;
        }

        // ブロックが看板でなければreturn
        if ( clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [mode] でなければreturn
        if ( !Chat.r(sign.getLine(0)).equalsIgnoreCase("[mode]") ) {
            return;
        }

        // 4行目が[ACTIVE]でなければreturn
        if ( !sign.getLine(3).equals(LeonGunWar.SIGN_ACTIVE) ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // 2行目を取得し、MatchModeに変換
        String line2 = Chat.r(sign.getLine(1));
        MatchMode mode = MatchMode.getFromString(line2);

        // modeがnullの場合return
        if ( mode == null ) {
            return;
        }

        // モードを指定
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != null ) {
            p.sendMessage(Chat.f("{0}&7すでに設定されているためモード変更ができません！", LeonGunWar.GAME_PREFIX));
            return;
        }

        p.openInventory(getDistributeSelectInventory(mode));
    }

    @EventHandler
    public void changeSignState(PlayerInteractEvent e) {
        // ブロックをシフト + 右クリックしていなければreturn
        if ( e.getAction() != Action.RIGHT_CLICK_BLOCK || !e.getPlayer().isSneaking() ) {
            return;
        }

        // プレイヤー / ブロック取得
        Player p = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        // ブロックが看板でなければreturn
        if ( clickedBlock.getType() != Material.SIGN_POST && clickedBlock.getType() != Material.WALL_SIGN ) {
            return;
        }

        // Signにキャスト
        Sign sign = (Sign) clickedBlock.getState();

        // 1行目が [entry] または [leave] でなければreturn
        if ( !Chat.r(sign.getLine(0)).equalsIgnoreCase("[mode]") ) {
            return;
        }

        // 権限がなければreturn
        if ( !p.hasPermission("leongunwar.entrysign.changestate") ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // 元メッセージ
        String line4 = sign.getLine(3);
        // 編集先メッセージ
        String edit = "";

        // 4行目の編集
        if ( line4.equals(LeonGunWar.SIGN_INACTIVE) ) { // [INACTIVE] の場合
            edit = LeonGunWar.SIGN_ACTIVE;
        } else { // それ以外の場合は [INACITVE]に変更
            edit = LeonGunWar.SIGN_INACTIVE;
        }

        // 変更
        sign.setLine(3, edit);
        // 更新
        sign.update();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        Inventory openingInv = e.getInventory();

        if ( !Chat.r(openingInv.getTitle()).startsWith("Distribute Selector - ") ) {
            return;
        }

        e.setCancelled(true);

        ItemStack clicked = e.getCurrentItem();
        if ( clicked == null || clicked.getType() == Material.AIR ) {
            return;
        }

        // モードを指定
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != null ) {
            p.sendMessage(Chat.f("{0}&7すでに設定されているためモード変更ができません！", LeonGunWar.GAME_PREFIX));
            p.closeInventory();
            return;
        }

        MatchMode mode = MatchMode.getFromString(openingInv.getTitle().substring(openingInv.getTitle().indexOf(Chat.f("&e")) + 2));
        if ( mode == null ) {
            Bukkit.getLogger().info(openingInv.getTitle().substring(openingInv.getTitle().indexOf(Chat.f("&e")) + 2));
            return;
        }

        TeamDistributor distributor = null;
        if ( clicked.isSimilar(defaultItem) ) {
            distributor = new DefaultTeamDistributor();
        } else if ( clicked.isSimilar(kdItem) ) {
            distributor = new KDTeamDistributor();
        }

        if ( distributor == null ) {
            return;
        }

        LeonGunWar.getPlugin().getManager().setMatchMode(mode);
        LeonGunWar.getPlugin().getManager().setTeamDistributor(distributor);
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));
        Bukkit.broadcastMessage(Chat.f("{0}&7モード   {1}", LeonGunWar.GAME_PREFIX, mode.getModeName()));
        Bukkit.broadcastMessage(Chat.f("{0}&7振り分け  {1}", LeonGunWar.GAME_PREFIX, distributor.getDistributorName()));
        Bukkit.broadcastMessage(Chat.f("{0}&7人数が集まり次第開始します", LeonGunWar.GAME_PREFIX));
        Bukkit.broadcastMessage(Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));

        // 音を鳴らす
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1);
        });

        // 全プレイヤーにQuickメッセージを送信
        LeonGunWar.getQuickBar().send(Bukkit.getOnlinePlayers().stream().toArray(Player[]::new));

        p.closeInventory();
    }

    private Inventory getDistributeSelectInventory(MatchMode mode) {
        String shortModeName = "";
        for ( String s : mode.name().split("_") ) {
            shortModeName += s.substring(0, 1);
        }
        Inventory inv = Bukkit.createInventory(null, 9, Chat.f("&cDistribute Selector - &e{0}", shortModeName));
        inv.setItem(3, defaultItem);
        inv.setItem(5, kdItem);
        return inv;
    }

    private ItemStack create(Material type, String title, String... lore) {
        ItemStack item = new ItemStack(type);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(title);
        if ( lore.length > 0 ) {
            meta.setLore(Arrays.asList(lore));
        }
        item.setItemMeta(meta);
        return item;
    }
}

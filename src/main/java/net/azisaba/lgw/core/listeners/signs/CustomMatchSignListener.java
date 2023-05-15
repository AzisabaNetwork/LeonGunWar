package net.azisaba.lgw.core.listeners.signs;

import com.google.common.base.Strings;
import java.util.Arrays;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.distributors.DefaultTeamDistributor;
import net.azisaba.lgw.core.distributors.KDTeamDistributor;
import net.azisaba.lgw.core.distributors.TeamDistributor;
import net.azisaba.lgw.core.listeners.modes.CustomTDMListener;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.BroadcastUtils;
import net.azisaba.lgw.core.utils.Chat;
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
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * 次に実行するカスタム試合のあれこれを指定する看板&GUI ACTIVEとINACTIVEを切り替えるときはスニークをしながら右クリックで可能
 * 破壊するときはスニークしながら左クリックで可能
 *
 * @author Mr_IK Thanks: siloneco
 */
public class CustomMatchSignListener implements Listener {

    private final ItemStack no_limit, matchpoint, main_limit, sub_limit, granade_limit, defaultItem, kdItem;

    public CustomMatchSignListener() {
        no_limit = create(Material.WATCH, Chat.f("&eモード : &6NO LIMIT"));
        matchpoint = create(Material.EMERALD, Chat.f("&eマッチ終了ポイント : &a50P"));
        main_limit = create(Material.SUGAR_CANE, Chat.f("&eメイン武器射撃 : &a可能"));
        sub_limit = create(Material.GOLD_HOE, Chat.f("&eサブ武器射撃 : &a可能"));
        granade_limit = create(Material.SLIME_BALL, Chat.f("&eグレネード投擲 : &a可能"));
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
        if ( !Chat.r(sign.getLine(0)).equalsIgnoreCase("[custom]") ) {
            return;
        }

        // 4行目が[ACTIVE]でなければreturn
        if ( !sign.getLine(3).equals(LeonGunWar.SIGN_ACTIVE) ) {
            return;
        }

        // イベントをキャンセル
        e.setCancelled(true);

        // モードを指定
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != null ) {
            p.sendMessage(Chat.f("{0}&7すでに設定されているためモード変更ができません！", LeonGunWar.GAME_PREFIX));
            return;
        }

        p.openInventory(getCustomSettingInventory());
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
        if ( !Chat.r(sign.getLine(0)).equalsIgnoreCase("[custom]") ) {
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
        String edit;

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
    public void onSign(SignChangeEvent e) {
        // プレイヤー / ブロック取得
        Player p = e.getPlayer();

        // 1行目が [entry] または [leave] でなければreturn
        if ( !Chat.r(e.getLine(0)).equalsIgnoreCase("[custom]") ) {
            return;
        }

        // 権限がなければreturn
        if ( !p.hasPermission("leongunwar.entrysign.changestate") ) {
            return;
        }

        e.setLine(0, Chat.f("&b&l[&3&lCUSTOM&b&l]"));
        e.setLine(1, Chat.f("&3&lCUSTOM &5&lDEATH"));
        e.setLine(2, Chat.f("&4&lMATCH"));
        e.setLine(3, Chat.f(LeonGunWar.SIGN_ACTIVE));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if ( !(e.getWhoClicked() instanceof Player) ) {
            return;
        }

        Player p = (Player) e.getWhoClicked();
        Inventory openingInv = e.getInventory();

        if ( !Chat.r(openingInv.getTitle()).startsWith("Custom-TDM Setting") ) {
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

        MatchMode mode = MatchMode.getFromString("cdm");
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

        if ( distributor != null ) {
            String itemname = e.getClickedInventory().getItem(0).getItemMeta().getDisplayName();
            if ( Chat.r(itemname).equalsIgnoreCase("モード : NO LIMIT") ) {
                CustomTDMListener.setMatchtype(CustomTDMListener.TDMType.no_limit);
            } else if ( Chat.r(itemname).equalsIgnoreCase("モード : POINT") ) {
                CustomTDMListener.setMatchtype(CustomTDMListener.TDMType.point);
            } else if ( Chat.r(itemname).equalsIgnoreCase("モード : LEADER") ) {
                CustomTDMListener.setMatchtype(CustomTDMListener.TDMType.leader);
            }
            itemname = e.getClickedInventory().getItem(2).getItemMeta().getDisplayName();
            if ( Chat.r(itemname).equalsIgnoreCase("マッチ終了ポイント : 50P") ) {
                CustomTDMListener.setMatchpoint(50);
            } else if ( Chat.r(itemname).equalsIgnoreCase("マッチ終了ポイント : 100P") ) {
                CustomTDMListener.setMatchpoint(100);
            }
            itemname = e.getClickedInventory().getItem(4).getItemMeta().getDisplayName();
            if ( Chat.r(itemname).equalsIgnoreCase("メイン武器射撃 : 可能") ) {
                CustomTDMListener.customLimit.put(CustomTDMListener.MAIN_WEAPON, 1);
            } else if ( Chat.r(itemname).equalsIgnoreCase("メイン武器射撃 : 不可能") ) {
                CustomTDMListener.customLimit.put(CustomTDMListener.MAIN_WEAPON, 0);
            }
            itemname = e.getClickedInventory().getItem(6).getItemMeta().getDisplayName();
            if ( Chat.r(itemname).equalsIgnoreCase("サブ武器射撃 : 可能") ) {
                CustomTDMListener.customLimit.put(CustomTDMListener.SUB_WEAPON, 2);
            } else if ( Chat.r(itemname).equalsIgnoreCase("サブ武器射撃 : 不可能") ) {
                CustomTDMListener.customLimit.put(CustomTDMListener.SUB_WEAPON, 0);
            }
            itemname = e.getClickedInventory().getItem(8).getItemMeta().getDisplayName();
            if ( Chat.r(itemname).equalsIgnoreCase("グレネード投擲 : 可能") ) {
                CustomTDMListener.customLimit.put(CustomTDMListener.GRENADE, 1);
            } else if ( Chat.r(itemname).equalsIgnoreCase("グレネード投擲 : 不可能") ) {
                CustomTDMListener.customLimit.put(CustomTDMListener.GRENADE, 0);
            }
            // 最終確認 メイン・サブ・グレネード すべてが禁止の場合…
            if (CustomTDMListener.customLimit.get(CustomTDMListener.MAIN_WEAPON) == 0
                && CustomTDMListener.customLimit.get(CustomTDMListener.SUB_WEAPON) == 0
                && CustomTDMListener.customLimit.get(CustomTDMListener.GRENADE) == 0) {
                // 申し訳ないがなにも使えないのでNG
                p.sendMessage(Chat.f("&4すべて使用不可にすることはできません！1種類は残してください！"));
                return;
            }

            LeonGunWar.getPlugin().getManager().setMatchMode(mode);
            LeonGunWar.getPlugin().getManager().setTeamDistributor(distributor);
            BroadcastUtils.broadcast(
                Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));
            BroadcastUtils.broadcast(
                Chat.f("{0}&7モード   {1}", LeonGunWar.GAME_PREFIX, mode.getModeName()));
            BroadcastUtils.broadcast(Chat.f("{0}&7振り分け  {1}", LeonGunWar.GAME_PREFIX,
                distributor.getDistributorName()));
            BroadcastUtils.broadcast(Chat.f("{0}&7勝利条件  {1}", LeonGunWar.GAME_PREFIX,
                CustomTDMListener.getWinCase()));
            BroadcastUtils.broadcast(
                Chat.f("{0}&7武器制限  {1}", LeonGunWar.GAME_PREFIX, CustomTDMListener.getExtra()));
            BroadcastUtils.broadcast(Chat.f("{0}&7人数が集まり次第開始します", LeonGunWar.GAME_PREFIX));
            BroadcastUtils.broadcast(
                Chat.f("{0}&c注意！カスタムデスマッチのため一切報酬はもらえません！", LeonGunWar.GAME_PREFIX));
            BroadcastUtils.broadcast(
                Chat.f("{0}&7{1}", LeonGunWar.GAME_PREFIX, Strings.repeat("=", 40)));

            // 音を鳴らす
            BroadcastUtils.getOnlinePlayers()
                .forEach(player -> player.playSound(p.getLocation(), Sound.BLOCK_NOTE_PLING, 1, 1));

            // 全プレイヤーにQuickメッセージを送信
            LeonGunWar.getQuickBar().send(BroadcastUtils.getOnlinePlayers().toArray(new Player[0]));

            p.closeInventory();
            return;
        }

        // プレイヤーのクリックしたインベントリチェック
        if ( e.getClickedInventory().getType() != InventoryType.PLAYER ) {
            // クリック音
            p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            // クリックした場所が…なら
            String itemname = clicked.getItemMeta().getDisplayName();
            if ( e.getSlot() == 0 ) {
                if ( Chat.r(itemname).equalsIgnoreCase("モード : NO LIMIT") ) {
                    e.getClickedInventory().setItem(0, create(Material.WATCH, Chat.f("&eモード : &cLEADER")));
                } else if ( Chat.r(itemname).equalsIgnoreCase("モード : LEADER") ) {
                    e.getClickedInventory().setItem(0, create(Material.WATCH, Chat.f("&eモード : &aPOINT")));
                } else if ( Chat.r(itemname).equalsIgnoreCase("モード : POINT") ) {
                    e.getClickedInventory().setItem(0, no_limit);
                }
            } else if ( e.getSlot() == 2 ) {
                if ( Chat.r(itemname).equalsIgnoreCase("マッチ終了ポイント : 50P") ) {
                    e.getClickedInventory().setItem(2, create(Material.EMERALD, Chat.f("&eマッチ終了ポイント : &a100P")));
                } else if ( Chat.r(itemname).equalsIgnoreCase("マッチ終了ポイント : 100P") ) {
                    e.getClickedInventory().setItem(2, matchpoint);
                }
            } else if ( e.getSlot() == 4 ) {
                if ( Chat.r(itemname).equalsIgnoreCase("メイン武器射撃 : 可能") ) {
                    e.getClickedInventory().setItem(4, create(Material.SUGAR_CANE, Chat.f("&eメイン武器射撃 : &c不可能")));
                } else if ( Chat.r(itemname).equalsIgnoreCase("メイン武器射撃 : 不可能") ) {
                    e.getClickedInventory().setItem(4, main_limit);
                }
            } else if ( e.getSlot() == 6 ) {
                if ( Chat.r(itemname).equalsIgnoreCase("サブ武器射撃 : 可能") ) {
                    e.getClickedInventory().setItem(6, create(Material.GOLD_HOE, Chat.f("&eサブ武器射撃 : &c不可能")));
                } else if ( Chat.r(itemname).equalsIgnoreCase("サブ武器射撃 : 不可能") ) {
                    e.getClickedInventory().setItem(6, sub_limit);
                }
            } else if ( e.getSlot() == 8 ) {
                if ( Chat.r(itemname).equalsIgnoreCase("グレネード投擲 : 可能") ) {
                    e.getClickedInventory().setItem(8, create(Material.SLIME_BALL, Chat.f("&eグレネード投擲 : &c不可能")));
                } else if ( Chat.r(itemname).equalsIgnoreCase("グレネード投擲 : 不可能") ) {
                    e.getClickedInventory().setItem(8, granade_limit);
                }
            }
        }
    }

    private Inventory getCustomSettingInventory() {
        Inventory inv = Bukkit.createInventory(null, 18, Chat.f("&cCustom-TDM &eSetting"));
        inv.setItem(0, no_limit);
        inv.setItem(2, matchpoint);
        inv.setItem(4, main_limit);
        inv.setItem(6, sub_limit);
        inv.setItem(8, granade_limit);
        // 2行目
        inv.setItem(12, defaultItem);
        inv.setItem(14, kdItem);
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

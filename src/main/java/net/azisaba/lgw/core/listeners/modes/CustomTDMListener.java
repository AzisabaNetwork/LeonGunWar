package net.azisaba.lgw.core.listeners.modes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.TeamPointIncreasedEvent;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * CUSTOM-TDMの処理をするListener
 *
 * @author Mr_IK
 *
 */
public class CustomTDMListener implements Listener {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum TDMType {
        no_limit("&6上限なしチームデスマッチ"),
        point("&9チームデスマッチ"),
        leader("&dリーダーデスマッチ");

        private final String name;

        public String getName() {
            return name;
        }
    }

    // プレイヤーが変更可能の設定
    private static int matchpoint = 50;

    public static void setMatchpoint(int matchpoint) {
        CustomTDMListener.matchpoint = matchpoint;
    }

    public static int getMatchpoint() {
        return matchpoint;
    }

    private static TDMType matchtype = TDMType.point;

    public static TDMType getMatchType() {
        return matchtype;
    }

    public static void setMatchtype(TDMType no_limit) {
        CustomTDMListener.matchtype = no_limit;
    }

    public static String getWinCase() {
        if ( getMatchType() == TDMType.no_limit ) {
            return Chat.f("&7終了時に &cキル数が多いチーム &7が勝利");
        } else if ( getMatchType() == TDMType.leader ) {
            return Chat.f("&7相手チームの &dリーダー &7を倒して勝利");
        } else {
            return Chat.f("&7先に &a{0}キル &7で勝利", getMatchpoint());
        }
    }

    public static String getExtra() {
        if ( customLimit.size() == 0 ) {
            return Chat.f("&7制限なし");
        } else {
            int main = customLimit.get(MAIN_WEAPON);
            int sub = customLimit.get(SUB_WEAPON);
            int gre = customLimit.get(GRENADE);

            if ( main == 1 && sub == 2 && gre == 1 ) {
                return Chat.f("&7制限なし");
            }

            String mains = Chat.f("&7制限なし");
            if ( main != 1 ) {
                mains = Chat.f("&c発射不可");
            }
            String subs = Chat.f("&7制限なし");
            if ( sub != 2 ) {
                subs = Chat.f("&c発射不可");
            }
            String gres = Chat.f("&7制限なし");
            if ( gre != 1 ) {
                gres = Chat.f("&c投擲不可");
            }

            return Chat.f("&6Main: {0} &6Sub: {1} &6Gre: {2}", mains, subs, gres);
        }
    }

    public final static HashMap<String, Integer> customLimit = new HashMap<>();

    // CrackShotAPI
    private final static CSDirector director = JavaPlugin.getPlugin(CSDirector.class);

    // customLimitで開発者が役に立つString
    public final static String MAIN_WEAPON = "primary_weapons";
    public final static String SUB_WEAPON = "sub_weapons";
    public final static String GRENADE = "grenade_weapons";

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamPointAdded(TeamPointIncreasedEvent e) {

        // CDMではない場合return
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != MatchMode.CUSTOM_DEATH_MATCH ) {
            return;
        }

        // POINT制ではないならreturn
        if ( getMatchType() != TDMType.point ) {
            return;
        }

        // 試合終了チェック
        if ( e.getCurrentPoint() >= matchpoint ) {

            MatchManager manager = LeonGunWar.getPlugin().getManager();
            // 試合終了
            MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), Collections.singletonList(e.getTeam()), manager.getTeamPlayers());
            Bukkit.getPluginManager().callEvent(event);

            // メッセージ表示
        } else if ( e.getCurrentPoint() % 50 == 0 || matchpoint - e.getCurrentPoint() == 10 || matchpoint - e.getCurrentPoint() == 5 ) {
            // 50区切り/残り10/残り5ならメッセージを表示
            Bukkit.broadcastMessage(Chat.f("{0}&7残り &e{1}キル &7で &r{2} &7が勝利！", LeonGunWar.GAME_PREFIX,
                    matchpoint - e.getCurrentPoint(), e.getTeam().getTeamName()));
        }
    }

    @EventHandler
    public void onLeaderKilledDetector(PlayerDeathEvent e) {
        MatchManager manager = LeonGunWar.getPlugin().getManager();

        // CDMではない場合return
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != MatchMode.CUSTOM_DEATH_MATCH ) {
            return;
        }

        // リーダー制ではないならreturn
        if ( getMatchType() != TDMType.leader ) {
            return;
        }

        // 死んだプレイヤー
        Player death = e.getEntity();

        // 死んだプレイヤーと殺したプレイヤーが同じ (またはnull) ならreturn
        if ( death.getKiller() == null || death == death.getKiller() ) {
            return;
        }

        // 各チームのリーダーを取得
        Map<BattleTeam, Player> leaders = manager.getLDMLeaderMap();

        // 死んだプレイヤーがリーダーだった場合、試合を終了する
        for ( BattleTeam team : leaders.keySet() ) {

            // リーダーではない場合continue
            if ( leaders.get(team) != death ) {
                continue;
            }

            // その他のチームを取得
            List<BattleTeam> teams = new ArrayList<>(leaders.keySet());
            // 殺されたリーダーのチームを削除
            teams.remove(team);

            // このイベントの後にイベント作成、呼び出し
            // 遅らせる理由は最後のキルが表示されないため
            Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> {
                MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), teams,
                        manager.getTeamPlayers());
                Bukkit.getPluginManager().callEvent(event);
            }, 0L);
            break;
        }
    }

    // 銃の打てる制限
    @EventHandler
    public void onPreWeaponShoot(WeaponPrepareShootEvent e) {

        // CDMではない場合return
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != MatchMode.CUSTOM_DEATH_MATCH ) {
            return;
        }
        Player p = e.getPlayer();
        // 試合に参加していない場合はreturn
        if ( !LeonGunWar.getPlugin().getManager().isPlayerMatching(p) ) {
            return;
        }

        String group = director.returnParentNode(p);
        if ( group == null ) {
            return;
        }
        String groups = director.getString(group + ".Item_Information.Inventory_Control");
        if ( groups == null ) {
            return;
        }
        if ( !validHotbar(p, groups) ) {
            e.setCancelled(true);
        }
    }

    // 本物のCSからコピって、少し改造したものなのでスペースやら説明はご愛敬
    public static boolean validHotbar(Player shooter, String invCtrl) {
        boolean retVal = true;
        Inventory playerInv = shooter.getInventory();
        String[] groupList = invCtrl.replaceAll(" ", "").split(",");
        String[] var10 = groupList;
        int var9 = groupList.length;

        for ( int var8 = 0; var8 < var9; ++var8 ) {
            String invGroup = var10[var8];
            int groupLimit = director.getInt(invGroup + ".Limit");
            if ( customLimit.containsKey(invGroup) ) {
                groupLimit = customLimit.get(invGroup);
            }
            int groupCount = 0;

            for ( int i = 0; i < 9; ++i ) {
                ItemStack checkItem = playerInv.getItem(i);
                if ( checkItem != null && director.itemIsSafe(checkItem) ) {
                    String[] checkParent = director.itemParentNode(checkItem, shooter);
                    if ( checkParent != null ) {
                        String groupCheck = director.getString(checkParent[0] + ".Item_Information.Inventory_Control");
                        if ( groupCheck != null && groupCheck.contains(invGroup) ) {
                            ++groupCount;
                        }
                    }
                }
            }

            if ( groupCount > groupLimit ) {
                director.sendPlayerMessage(shooter, invGroup, ".Message_Exceeded", "<shooter>", "<victim>", "<flight>", "<damage>");
                director.playSoundEffects(shooter, invGroup, ".Sounds_Exceeded", false, null);
                retVal = false;
            }
        }

        return retVal;
    }
}

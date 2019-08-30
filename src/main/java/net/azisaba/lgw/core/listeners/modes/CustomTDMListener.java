package net.azisaba.lgw.core.listeners.modes;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.events.WeaponPrepareShootEvent;
import lombok.Data;
import lombok.Getter;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.TeamPointIncreasedEvent;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.utils.Chat;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;


/**
 *
 * CUSTOM-TDMの処理をするListener
 *
 * @author Mr_IK
 *
 */
public class CustomTDMListener implements Listener {

    // プレイヤーが変更可能の設定
    private static int matchpoint = 50;

    public static void setMatchpoint(int matchpoint) {
        CustomTDMListener.matchpoint = matchpoint;
    }

    public static int getMatchpoint() {
        return matchpoint;
    }

    private static boolean no_limit = false;

    public static boolean isNo_limit() {
        return no_limit;
    }

    public static void setNo_limit(boolean no_limit) {
        CustomTDMListener.no_limit = no_limit;
    }

    public static String getWinCase(){
        if (isNo_limit()) {
            return Chat.f("&7終了時に &cキル数が多いチーム &7が勝利");
        }else{
            return Chat.f("&7先に &a{0}キル &7で勝利" , getMatchpoint());
        }
    }

    public static String getExtra(){
        if(customLimit.size()==0){
            return Chat.f("&7制限なし");
        }else{
            int main = customLimit.get(MAIN_WEAPON);
            int sub = customLimit.get(SUB_WEAPON);
            int gre = customLimit.get(GRENADE);

            if( main == 1 && sub == 2 && gre == 1 ){
                return Chat.f("&7制限なし");
            }

            String mains = Chat.f("&7制限なし");
            if(main!=1){
                mains = Chat.f("&c発射不可");
            }
            String subs = Chat.f("&7制限なし");
            if(sub!=2){
                subs = Chat.f("&c発射不可");
            }
            String gres = Chat.f("&7制限なし");
            if(gre!=1){
                gres = Chat.f("&c投擲不可");
            }

            return Chat.f("&6Main: {0} &6Sub: {1} &6Gre: {2}" , mains , subs , gres );
        }
    }


    public final static HashMap<String,Integer> customLimit = new HashMap<>();

    // CrackShotAPI
    private final static CSDirector director = JavaPlugin.getPlugin(CSDirector.class);

    // customLimitで開発者が役に立つString
    public final static String MAIN_WEAPON = "primary_weapons";
    public final static String SUB_WEAPON = "sub_weapons";
    public final static String GRENADE = "grenade_weapons";

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamPointAdded(TeamPointIncreasedEvent e) {

        // CDMではない場合return
        if ( LeonGunWar.getPlugin().getManager().getMatchMode() != MatchMode.CUSTOM_DEATH_MATCH) {
            return;
        }

        // NO_LIMITならreturn
        if (isNo_limit()) {
            return;
        }

        // 50区切り/残り10/残り5ならメッセージを表示
        if ( e.getCurrentPoint()%50 == 0 || matchpoint - e.getCurrentPoint() == 10 || matchpoint - e.getCurrentPoint() == 5 ) {
            Bukkit.broadcastMessage(Chat.f("{0}&7残り &e{1}キル &7で &r{2} &7が勝利！", LeonGunWar.GAME_PREFIX,
                    matchpoint - e.getCurrentPoint(), e.getTeam().getTeamName()));
        } else if ( e.getCurrentPoint() >= matchpoint ) {
            MatchManager manager = LeonGunWar.getPlugin().getManager();

            // 試合終了
            MatchFinishedEvent event = new MatchFinishedEvent(manager.getCurrentGameMap(), Arrays.asList(e.getTeam()),
                    manager.getTeamPlayers());
            Bukkit.getPluginManager().callEvent(event);
        }
    }


    //銃の打てる制限
    @EventHandler
    public void onPreWeaponShoot(WeaponPrepareShootEvent e){
        Player p = e.getPlayer();
        String group = director.returnParentNode(p);
        if(group==null){
            return;
        }
        String groups = director.getString(group + ".Item_Information.Inventory_Control");
        if(!validHotbar(p,groups)){
            e.setCancelled(true);
        }
    }

    // 本物のCSからコピって、少し改造したものなのでスペースやら説明はご愛敬
    public static boolean validHotbar(Player shooter,String invCtrl) {
        boolean retVal = true;
        Inventory playerInv = shooter.getInventory();
        String[] groupList = invCtrl.replaceAll(" ", "").split(",");
        String[] var10 = groupList;
        int var9 = groupList.length;

        for(int var8 = 0; var8 < var9; ++var8) {
            String invGroup = var10[var8];
            int groupLimit = director.getInt(invGroup + ".Limit");
            if(customLimit.containsKey(invGroup)){
                groupLimit = customLimit.get(invGroup);
            }
            int groupCount = 0;

            for(int i = 0; i < 9; ++i) {
                ItemStack checkItem = playerInv.getItem(i);
                if (checkItem != null && director.itemIsSafe(checkItem)) {
                    String[] checkParent = director.itemParentNode(checkItem, shooter);
                    if (checkParent != null) {
                        String groupCheck = director.getString(checkParent[0] + ".Item_Information.Inventory_Control");
                        if (groupCheck != null && groupCheck.contains(invGroup)) {
                            ++groupCount;
                        }
                    }
                }
            }

            if (groupCount > groupLimit) {
                director.sendPlayerMessage(shooter, invGroup, ".Message_Exceeded", "<shooter>", "<victim>", "<flight>", "<damage>");
                director.playSoundEffects(shooter, invGroup, ".Sounds_Exceeded", false, (Location)null);
                retVal = false;
            }
        }

        return retVal;
    }
}

package net.azisaba.lgw.core.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.azisaba.lgw.core.MatchManager;
import net.azisaba.lgw.core.events.PlayerKillEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.shampaggon.crackshot.CSDirector;
import com.shampaggon.crackshot.CSUtility;
import com.shampaggon.crackshot.events.WeaponDamageEntityEvent;

import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.utils.Chat;

public class DamageListener implements Listener {

    private final CSUtility crackShot = new CSUtility();

    // 最初のHashMapはダメージを受けた側のプレイヤーであり、そのValueとなるHashMapにはどのプレイヤーが何秒にそのプレイヤーを攻撃したか
    // アシストの判定に使用される
    private final Map<Player, Map<Player, Long>> lastDamaged = new HashMap<>();

    /**
     * プレイヤーを殺したことを検知するリスナー 死亡したプレイヤーの処理は他のリスナーで行います
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onKill(PlayerDeathEvent e) {
        MatchManager manager = LeonGunWar.getPlugin().getManager();
        // 試合中でなければreturn
        if ( !manager.isMatching() ) {
            return;
        }

        // 殺したプレイヤーを取得
        Player killer = e.getEntity().getKiller();

        // 殺したプレイヤーがいない場合はreturn
        if ( killer == null ) {
            return;
        }

        // チームを取得
        BattleTeam killerTeam = LeonGunWar.getPlugin().getManager().getBattleTeam(killer);

        // killerTeamがnullの場合return
        if ( killerTeam == null ) {
            return;
        }

        // 個人キルを追加
        LeonGunWar.getPlugin().getManager().getKillDeathCounter().addKill(killer);

        //キルがポイント追加のゲームモードのみポイントを追加
        if(manager.getMatchMode().getKillingPlayerGetPoint()) {
            // ポイントを追加
            LeonGunWar.getPlugin().getManager().addTeamPoint(killerTeam);
        }
        // タイトルを表示
        killer.sendTitle("", Chat.f("&c+1 &7Kill"), 0, 10, 10);
        int streaks = LeonGunWar.getPlugin().getKillStreaks().get(killer).get();
        Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> killer.sendTitle("", Chat.f("&b{0} &7Kill Streaks", streaks), 0, 20, 20), 20);
        // 音を鳴らす
        killer.playSound(killer.getLocation(), Sound.BLOCK_ANVIL_LAND, 1f, 1f);
    }

    /**
     * 試合中のプレイヤーが死亡した場合、死亡カウントを増加させます
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent e) {
        Player deader = e.getEntity();

        // チームを取得
        BattleTeam deaderTeam = LeonGunWar.getPlugin().getManager().getBattleTeam(deader);

        // deaderTeamがnullの場合return
        if ( deaderTeam == null ) {
            return;
        }

        // 死亡数を追加
        LeonGunWar.getPlugin().getManager().getKillDeathCounter().addDeath(deader);

        // 殺したプレイヤーを取得
        Player killer = deader.getKiller();

        // アシスト判定になるキーを取得 (過去10秒以内に攻撃したプレイヤー)
        // プレイヤーがkillしたプレイヤーならcontinue
        lastDamaged.getOrDefault(deader, new HashMap<>()).entrySet().stream()
                .filter(entry -> entry.getValue() + 10 * 1000 > System.currentTimeMillis())
                .map(Map.Entry::getKey)
                .filter(Objects::nonNull)
                .filter(assist -> assist != killer)
                .forEach(assist -> {
                    // アシスト追加
                    LeonGunWar.getPlugin().getManager().getKillDeathCounter().addAssist(assist);

                    // タイトルを表示
                    assist.sendTitle("", Chat.f("&e+1 &7Assist"), 0, 10, 10);
                    int streaks = LeonGunWar.getPlugin().getAssistStreaks().get(assist).get();
                    Bukkit.getScheduler().runTaskLater(LeonGunWar.getPlugin(), () -> assist.sendTitle("", Chat.f("&a{0} &7Assist Streaks", streaks), 0, 20, 20), 20);
                    // 音を鳴らす
                    assist.playSound(assist.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1f);
                });

        // lastDamagedを初期化
        lastDamaged.remove(deader);

        // 連続キルを停止
        LeonGunWar.getPlugin().getKillStreaks().removedBy(deader, killer);
        // 連続アシストを停止
        LeonGunWar.getPlugin().getAssistStreaks().removedBy(deader, killer);
    }

    /**
     * プレイヤーが他のプレイヤーに攻撃したときにミリ秒を記録します この秒数はアシスト判定に使用されます
     *
     * @param e 処理するイベント
     */
    @EventHandler
    public void onAttackPlayer(WeaponDamageEntityEvent e) {
        Player attacker = e.getPlayer();

        // ダメージを受けたEntityがPlayerでなければreturn
        if ( !(e.getVictim() instanceof Player) ) {
            return;
        }

        Player victim = (Player) e.getVictim();

        // 同じプレイヤーならreturn
        if ( attacker == victim ) {
            return;
        }

        // 同じチームならreturn
        if ( LeonGunWar.getPlugin().getManager().isSameBattleTeam(attacker, victim) ) {
            return;
        }

        // ミリ秒を指定
        Map<Player, Long> damagedMap = lastDamaged.getOrDefault(victim, new HashMap<>());
        damagedMap.put(attacker, System.currentTimeMillis());

        lastDamaged.put(victim, damagedMap);
    }

    /**
     * キルログを変更するListener
     */
    @EventHandler
    public void deathMessageChanger(PlayerDeathEvent e) {
        Player p = e.getEntity();

        // 試合中ではない場合はreturn
        if ( !LeonGunWar.getPlugin().getManager().isMatching() ) {
            return;
        }

        // 試合中のワールドではない場合はreturn
        if ( p.getWorld() != LeonGunWar.getPlugin().getManager().getCurrentGameMap().getWorld() ) {
            return;
        }

        // 殺したEntityが居ない場合か、同じプレイヤーの場合自滅とする
        if ( p.getKiller() == null || p.getKiller() == p ) {

            // メッセージ削除
            e.deathMessage(null);

            // メッセージを作成
            String msg = Chat.f("{0}{1} &7は自滅した！", LeonGunWar.GAME_PREFIX, p.getPlayerListName());
            // メッセージ送信
            p.getWorld().getPlayers().forEach(player -> player.sendMessage(msg));

            // コンソールに出力
            Bukkit.getConsoleSender().sendMessage(msg);
            return;
        }

        Player killer = e.getEntity().getKiller();

        // 殺したアイテム
        ItemStack item = killer.getInventory().getItemInMainHand();

        // アイテム名を取得
        String itemName;
        if ( item == null || item.getType() == Material.AIR ) { // null または Air なら素手
            itemName = Chat.f("&6素手");
        } else if ( item.hasItemMeta() && item.getItemMeta().hasDisplayName() ) { // DisplayNameが指定されている場合
            // CrackShot Pluginを取得
            CSDirector crackshot = (CSDirector) Bukkit.getPluginManager().getPlugin("CrackShot");

            // 銃ID取得
            String nodes = crackShot.getWeaponTitle(item);
            // DisplayNameを取得
            itemName = crackshot.getString(nodes + ".Item_Information.Item_Name");


            // DisplayNameがnullの場合は普通にアイテム名を取得
            if ( itemName == null ) {
                itemName = item.getItemMeta().getDisplayName();
            }

            Bukkit.getPluginManager().callEvent(new PlayerKillEvent(killer, nodes));
        } else { // それ以外
            itemName = Chat.f("&6{0}", item.getType().name());
        }

        // メッセージ削除
        e.deathMessage(null);
        // メッセージ作成
       // String msg = Chat.f("{0}&r{1} &7━━━ [ &r{2} &7] ━━━> &r{3}", LeonGunWar.GAME_PREFIX, killer.getPlayerListName(),
        //        itemName,
         //       p.getPlayerListName());

        TextComponent msg2 =Component.text()
                .append(Component.text(LeonGunWar.GAME_PREFIX))
                .append(Component.text(killer.getPlayerListName()))
                .append(Component.text("━━━ [").color(NamedTextColor.GRAY))
                .append(Component.text(itemName))
                .append(Component.text("] ━━━>").color(NamedTextColor.GRAY))
                .append(Component.text(p.getPlayerListName()))
                .build();


            // LoreをComponentリストとして取得
            List<Component> loreComponents = p.getKiller().getInventory().getItemInMainHand().lore();

            // Loreを一つのComponentにまとめる
            TextComponent.Builder loreTextBuilder = Component.text();
            if (loreComponents != null) {
                for (Component loreLine : loreComponents) {
                    loreTextBuilder.append(loreLine).append(Component.text("\n"));
                }
            }
            // ホバーイベントの作成（Loreを含む）
            HoverEvent<Component> hoverEvent = HoverEvent.showText(
                    Component.text(itemName, NamedTextColor.AQUA)
                            .append(Component.text("\n"))
                            .append(loreTextBuilder.build())
            );

            // ホバーイベントをメインメッセージに追加
            TextComponent messageWithTooltip = msg2.hoverEvent(hoverEvent);

            // メッセージ送信
            p.getWorld().getPlayers().forEach(player -> player.sendMessage(messageWithTooltip));

            // コンソールに出力
            Bukkit.getConsoleSender().sendMessage(messageWithTooltip);


    }

    @EventHandler
    public void onFireworksDamage(EntityDamageByEntityEvent e) {
        // Entitiyによる爆発ではない場合はreturn
        if ( e.getCause() != DamageCause.ENTITY_EXPLOSION ) {
            return;
        }

        // ダメージを受けたEntityがPlayerでなければreturn
        if ( !(e.getEntity() instanceof Player) ) {
            return;
        }

        // ダメージを与えたEntityが花火でなければreturn
        if ( !(e.getDamager() instanceof Firework) ) {
            return;
        }

        // キャンセル
        e.setCancelled(true);
    }

    /**
     * 試合が終わった時に lastDamaged を初期化します
     */
    @EventHandler
    public void onMatchFinished(MatchFinishedEvent e) {

        if ( LeonGunWar.getPlugin().getManager().isMatching() ) {
            lastDamaged.clear();
        }
    }
}

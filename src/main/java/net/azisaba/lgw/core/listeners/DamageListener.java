package net.azisaba.lgw.core.listeners;

import net.azisaba.crackshot.CrackShot;
import net.azisaba.crackshot.CSUtility;
import net.azisaba.crackshot.events.WeaponDamageEntityEvent;
import net.azisaba.lgw.core.LeonGunWar;
import net.azisaba.lgw.core.events.MatchFinishedEvent;
import net.azisaba.lgw.core.events.PlayerKillEvent;
import net.azisaba.lgw.core.util.AdventureUtil;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.Chat;
import net.azisaba.lgw.core.util.KillLogUtils;
import net.azisaba.lgw.core.util.MatchMode;
import net.azisaba.lgw.core.util.SyogoData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class DamageListener implements Listener {

    private final CSUtility crackShot = new CSUtility();

    // 最初のHashMapはダメージを受けた側のプレイヤーであり、そのValueとなるHashMapにはどのプレイヤーが何秒にそのプレイヤーを攻撃したか
    // アシストの判定に使用される
    private final Map<Player, Map<Player, Long>> lastDamaged = new HashMap<>();

    // 名前変更前の武器ID。既存のNameChangeAutomationデータがあれば引き続き利用する。
    private final Map<String, Optional<String>> baseWeaponIds = new HashMap<>();

    /**
     * プレイヤーを殺したことを検知するリスナー 死亡したプレイヤーの処理は他のリスナーで行います
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onKill(PlayerDeathEvent e) {
        // 試合中でなければreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }

        // 殺したプレイヤーを取得
        Player killer = e.getEntity().getKiller();

        // 殺したプレイヤーがいない場合はreturn
        if (killer == null) {
            return;
        }

        // チームを取得
        BattleTeam killerTeam = LeonGunWar.getPlugin().getManager().getBattleTeam(killer);

        // killerTeamがnullの場合return
        if (killerTeam == null) {
            return;
        }

        // 個人キルを追加
        LeonGunWar.getPlugin().getManager().getKillDeathCounter().addKill(killer);

        if (LeonGunWar.getPlugin().getManager().getMatchMode() != MatchMode.HIJACK) {
            // ポイントを追加
            LeonGunWar.getPlugin().getManager().addTeamPoint(killerTeam);
        }

        if (LeonGunWar.getPlugin().getManager().getLDMLeaderMap().containsValue(killer)) {
            BattleTeam battleTeam = LeonGunWar.getPlugin().getManager().getBattleTeam(killer);
            LeonGunWar.getPlugin().getManager().scheduleOrExtend(battleTeam, LeonGunWar.getPlugin(), 20L * 30);
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
        if (deaderTeam == null) {
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
        if (!(e.getVictim() instanceof Player victim)) {
            return;
        }

        // 同じプレイヤーならreturn
        if (attacker == victim) {
            return;
        }

        // 同じチームならreturn
        if (LeonGunWar.getPlugin().getManager().isSameBattleTeam(attacker, victim)) {
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
        Player victim = e.getEntity();

        // 試合中ではない場合はreturn
        if (!LeonGunWar.getPlugin().getManager().isMatching()) {
            return;
        }

        // 試合中のワールドではない場合はreturn
        if (LeonGunWar.getPlugin().getManager().getCurrentGameMap() == null
                || victim.getWorld() != LeonGunWar.getPlugin().getManager().getCurrentGameMap().getWorld()) {
            return;
        }

        // バニラの死亡メッセージは常に抑止し、このリスナーでキルログを送信する
        e.deathMessage(null);

        // 殺したEntityが居ない場合か、同じプレイヤーの場合自滅とする
        Player killer = victim.getKiller();
        if (killer == null || killer == victim) {
            Component message = legacy(Chat.f("{0}{1} &7は自滅した！", LeonGunWar.GAME_PREFIX,
                    victim.getPlayerListName()))
                    .hoverEvent(HoverEvent.showText(KillLogUtils.createDistanceText(null, victim)));
            victim.getWorld().getPlayers().forEach(player -> player.sendMessage(message));

            // コンソールに出力
            Bukkit.getConsoleSender().sendMessage(message);
            return;
        }

        // 殺したアイテム
        ItemStack item = killer.getInventory().getItemInMainHand();
        String weaponId = crackShot.getWeaponTitle(item);

        // CrackShot Pluginを取得
        CrackShot crackshot = (CrackShot) Bukkit.getPluginManager().getPlugin("CrackShot");

        // アイテム名を取得
        String itemName;
        if (item.getType() == Material.AIR) {
            itemName = Chat.f("&6素手");
        } else if (weaponId != null && crackshot != null) {
            itemName = crackshot.data.getString(weaponId + ".Item_Information.Item_Name");
            if (itemName == null) {
                itemName = getFallbackItemName(item);
            }
            Bukkit.getPluginManager().callEvent(new PlayerKillEvent(killer, weaponId));
        } else {
            itemName = getFallbackItemName(item);
        }

        SyogoData data = SyogoData.getSyogoDataFromCache(killer.getUniqueId());
        String syogo = "";
        if (data != null) {
            syogo = LeonGunWar.getPlugin().getSyogoConfig().syogos.getOrDefault(data.getSyogo(), "") + "&r ";
        }

        String killLog = LeonGunWar.getPlugin().getKillLogsConfig().getFormat(killer)
                .replace("{prefix}", LeonGunWar.GAME_PREFIX)
                .replace("{syogo}", syogo)
                .replace("{killer}", killer.getPlayerListName())
                .replace("{weapon}", itemName)
                .replace("{victim}", victim.getPlayerListName());
        Component message = legacy(killLog);

        // LoreをComponentリストとして取得
        List<Component> loreComponents = new ArrayList<>();
        if (item.lore() != null) {
            loreComponents.addAll(item.lore());
        }
        Optional<String> baseWeaponId = getBaseWeaponId(weaponId);
        if (baseWeaponId.isPresent() && crackshot != null) {
            // 元武器のDisplayNameを取得
            String itemName2 = crackshot.data.getString(baseWeaponId.get() + ".Item_Information.Item_Name");
            if (itemName2 != null) {
                Component previouslore = Component.text("Original:").color(NamedTextColor.GOLD)
                        .append(legacy(itemName2));
                loreComponents.add(previouslore);
            }
        }

        // Loreを一つのComponentにまとめる
        TextComponent.Builder loreTextBuilder = Component.text();
        for (Component loreLine : loreComponents) {
            loreTextBuilder.append(loreLine).append(Component.newline());
        }
        // ホバーイベントの作成（Loreを含む）
        HoverEvent<Component> hoverEvent = HoverEvent.showText(
                Component.text()
                        .append(legacy(itemName))
                        .append(Component.newline())
                        .append(loreTextBuilder.build())
                        .append(KillLogUtils.createDistanceText(killer, victim))
        );

        // ホバーイベントをメインメッセージに追加
        Component messageWithTooltip = message.hoverEvent(hoverEvent);

        // メッセージ送信
        victim.getWorld().getPlayers().forEach(player -> player.sendMessage(messageWithTooltip));

        // コンソールに出力
        Bukkit.getConsoleSender().sendMessage(messageWithTooltip);
    }

    private String getFallbackItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        return Chat.f("&6{0}", item.getType().name());
    }

    private Component legacy(String text) {
        return AdventureUtil.legacy(ChatColor.translateAlternateColorCodes('&', text));
    }

    private Optional<String> getBaseWeaponId(String weaponId) {
        if (weaponId == null || weaponId.isEmpty()) {
            return Optional.empty();
        }
        return baseWeaponIds.computeIfAbsent(weaponId, this::loadBaseWeaponId);
    }

    private Optional<String> loadBaseWeaponId(String weaponId) {
        if (weaponId.contains("..") || weaponId.contains("/") || weaponId.contains("\\")) {
            return Optional.empty();
        }

        File pluginsFolder = LeonGunWar.getPlugin().getDataFolder().getParentFile();
        File infoFolder = new File(new File(pluginsFolder, "NameChangeAutomation"), "NameChangeInfo");
        File infoFile = new File(infoFolder, weaponId + ".yml");
        if (!infoFile.isFile()) {
            return Optional.empty();
        }

        YamlConfiguration info = YamlConfiguration.loadConfiguration(infoFile);
        return Optional.ofNullable(info.getString(weaponId + ".PreviousID"));
    }

    @EventHandler
    public void onFireworksDamage(EntityDamageByEntityEvent e) {
        // Entitiyによる爆発ではない場合はreturn
        if (e.getCause() != DamageCause.ENTITY_EXPLOSION) {
            return;
        }

        // ダメージを受けたEntityがPlayerでなければreturn
        if (!(e.getEntity() instanceof Player)) {
            return;
        }

        // ダメージを与えたEntityが花火でなければreturn
        if (!(e.getDamager() instanceof Firework)) {
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

        if (LeonGunWar.getPlugin().getManager().isMatching()) {
            lastDamaged.clear();
        }
    }
}

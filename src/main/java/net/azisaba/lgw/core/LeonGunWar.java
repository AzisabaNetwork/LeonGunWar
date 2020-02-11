package net.azisaba.lgw.core;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import net.azisaba.lgw.core.commands.*;
import net.azisaba.lgw.core.configs.AssistStreaksConfig;
import net.azisaba.lgw.core.configs.KillStreaksConfig;
import net.azisaba.lgw.core.configs.MapsConfig;
import net.azisaba.lgw.core.configs.SpawnsConfig;
import net.azisaba.lgw.core.listeners.DamageListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.PlayerControlListener;
import net.azisaba.lgw.core.listeners.modes.CustomTDMListener;
import net.azisaba.lgw.core.listeners.modes.LeaderDeathMatchListener;
import net.azisaba.lgw.core.listeners.modes.TDMNoLimitListener;
import net.azisaba.lgw.core.listeners.modes.TeamDeathMatchListener;
import net.azisaba.lgw.core.listeners.others.*;
import net.azisaba.lgw.core.listeners.signs.CustomMatchSignListener;
import net.azisaba.lgw.core.listeners.signs.EntrySignListener;
import net.azisaba.lgw.core.listeners.signs.JoinAfterSignListener;
import net.azisaba.lgw.core.listeners.signs.MatchModeSignListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisableNormalWeaponsInNewYearPvEListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisablePvEsDuringMatchListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisablePvEsInLobbyListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisableToysDuringMatchListener;
import net.azisaba.lgw.core.tasks.CrackShotLagFixTask;
import net.azisaba.lgw.core.tasks.SignRemoveTask;
import net.azisaba.lgw.core.utils.Chat;

import lombok.Getter;

import me.rayzr522.jsonmessage.JSONMessage;

@Getter
public class LeonGunWar extends JavaPlugin {

    public static final String GAME_PREFIX = Chat.f("&7[&6PvP&7]&r ");
    public static final String SIGN_ACTIVE = Chat.f("&a[ACTIVE]");
    public static final String SIGN_INACTIVE = Chat.f("&c[INACTIVE]");

    // plugin
    @Getter
    private static LeonGunWar plugin;

    @Getter
    private static JSONMessage quickBar;

    private KillStreaksConfig killStreaksConfig;
    private AssistStreaksConfig assistStreaksConfig;
    private SpawnsConfig spawnsConfig;
    private MapsConfig mapsConfig;

    private final MatchStartCountdown countdown = new MatchStartCountdown();
    private final ScoreboardDisplayer scoreboardDisplayer = new ScoreboardDisplayer();
    private final MatchManager manager = new MatchManager();
    private final AssistStreaks assistStreaks = new AssistStreaks();
    private final KillStreaks killStreaks = new KillStreaks();
    private final TradeBoardManager tradeBoardManager = new TradeBoardManager();

    @Override
    public void onEnable() {
        plugin = this;
        quickBar = JSONMessage.create(Chat.f("&7[&bQuick&7] ここをクリック → "))
                .then(Chat.f("&a[エントリー]"))
                .runCommand("/leongunwar:match entry")
                .then(" ")
                .then(Chat.f("&c[エントリー解除]"))
                .runCommand("/leongunwar:match leave")
                .then(" ")
                .then(Chat.f("&6[途中参加]"))
                .runCommand("/leongunwar:match rejoin");

        // 設定ファイルを読み込むクラスの初期化
        killStreaksConfig = new KillStreaksConfig(this);
        assistStreaksConfig = new AssistStreaksConfig(this);
        spawnsConfig = new SpawnsConfig(this);
        mapsConfig = new MapsConfig(this);
        // 設定ファイルを読み込む
        killStreaksConfig.loadConfig();
        assistStreaksConfig.loadConfig();
        spawnsConfig.loadConfig();
        mapsConfig.loadConfig();

        // 初期化が必要なファイルを初期化する
        manager.initialize();
        tradeBoardManager.init();

        // コマンドのインスタンスに渡す必要があるListener
        LimitActionListener preventItemDropListener = new LimitActionListener();

        // コマンドの登録
        Bukkit.getPluginCommand("leongunwaradmin").setExecutor(new LgwAdminCommand());
        Bukkit.getPluginCommand("uav").setExecutor(new UAVCommand());
        Bukkit.getPluginCommand("match").setExecutor(new MatchCommand());
        Bukkit.getPluginCommand("kiai").setExecutor(new KIAICommand());
        Bukkit.getPluginCommand("resourcepack").setExecutor(new ResourcePackCommand());
        Bukkit.getPluginCommand("adminchat").setExecutor(new AdminChatCommand());
        Bukkit.getPluginCommand("limit").setExecutor(new LimitCommand(preventItemDropListener));

        // タブ補完の登録
        Bukkit.getPluginCommand("leongunwaradmin").setTabCompleter(new LgwAdminCommand());
        Bukkit.getPluginCommand("match").setTabCompleter(new MatchCommand());

        // コマンドの権限がない時のメッセージの指定
        Bukkit.getPluginCommand("leongunwaradmin").setPermissionMessage(Chat.f("&c権限がありません！"));
        Bukkit.getPluginCommand("uav").setPermissionMessage(Chat.f("&c権限がありません！"));
        Bukkit.getPluginCommand("match").setPermissionMessage(Chat.f("&c権限がありません！"));
        Bukkit.getPluginCommand("kiai").setPermissionMessage(Chat.f("&c権限がありません！"));
        Bukkit.getPluginCommand("resourcepack").setPermissionMessage(Chat.f("&c権限がありません！"));
        Bukkit.getPluginCommand("adminchat").setPermissionMessage(Chat.f("&c権限がありません！"));

        // リスナーの登録
        Bukkit.getPluginManager().registerEvents(new MatchControlListener(), this);
        Bukkit.getPluginManager().registerEvents(new EntrySignListener(), this);
        Bukkit.getPluginManager().registerEvents(new MatchModeSignListener(), this);
        Bukkit.getPluginManager().registerEvents(new JoinAfterSignListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomMatchSignListener(), this);
        Bukkit.getPluginManager().registerEvents(new MatchStartDetectListener(), this);
        Bukkit.getPluginManager().registerEvents(new DamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerControlListener(), this);

        // リスナーの登録 (modes)
        Bukkit.getPluginManager().registerEvents(new TeamDeathMatchListener(), this);
        Bukkit.getPluginManager().registerEvents(new TDMNoLimitListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaderDeathMatchListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomTDMListener(), this);

        // リスナーの登録 (others)
        Bukkit.getPluginManager().registerEvents(new NoArrowGroundListener(), this);
        Bukkit.getPluginManager().registerEvents(new NoKnockbackListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableItemDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableOpenInventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableOffhandListener(), this);
        Bukkit.getPluginManager().registerEvents(new EnableKeepInventoryListener(), this);
        Bukkit.getPluginManager().registerEvents(new RespawnKillProtectionListener(), this);
        Bukkit.getPluginManager().registerEvents(new AutoRespawnListener(), this);
        Bukkit.getPluginManager().registerEvents(new AfkKickEntryListener(), this);
        Bukkit.getPluginManager().registerEvents(new StreaksListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableRecipeListener(), this);
        Bukkit.getPluginManager().registerEvents(new CrackShotLimitListener(), this);
        Bukkit.getPluginManager().registerEvents(new TradeBoardListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableTNTBlockDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new SignWithColorListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableChangeItemListener(), this);
        Bukkit.getPluginManager().registerEvents(new FixStrikesCooldownListener(), this);
        Bukkit.getPluginManager().registerEvents(new OnsenListener(), this);
        Bukkit.getPluginManager().registerEvents(new AdminChatListener((AdminChatCommand) Bukkit.getPluginCommand("adminchat").getExecutor()), this);
        Bukkit.getPluginManager().registerEvents(new CrackShotLagFixListener(), this);
        Bukkit.getPluginManager().registerEvents(preventItemDropListener, this);
        Bukkit.getPluginManager().registerEvents(new DisableHopperPickupListener(), this);

        // 武器コントロールリスナーの登録 (weaponcontrols)
        Bukkit.getPluginManager().registerEvents(new DisableToysDuringMatchListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisablePvEsDuringMatchListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisablePvEsInLobbyListener(), this);
        Bukkit.getPluginManager().registerEvents(new DisableNormalWeaponsInNewYearPvEListener(), this);

        // SignRemoveTask (60秒後に最初の実行、それからは10分周期で実行)
        new SignRemoveTask().runTaskTimer(this, 20 * 60, 20 * 60 * 10);
        new CrackShotLagFixTask().runTaskTimer(this, 0, 20 * 60);

        Bukkit.getLogger().info(Chat.f("{0} が有効化されました。", getName()));
    }

    @Override
    public void onDisable() {
        // Plugin終了時の処理を呼び出す
        manager.onDisablePlugin();

        // 武器交換掲示板の看板を保存
        tradeBoardManager.saveAll();

        Bukkit.getLogger().info(Chat.f("{0} が無効化されました。", getName()));
    }
}

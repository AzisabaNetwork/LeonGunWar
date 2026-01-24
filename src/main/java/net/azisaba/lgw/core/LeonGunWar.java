package net.azisaba.lgw.core;

import lombok.Getter;
import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.commands.*;
import net.azisaba.lgw.core.configs.*;
import net.azisaba.lgw.core.listeners.modes.*;
import net.azisaba.lgw.core.sql.SQLConnection;
import net.azisaba.lgw.core.util.SyogoData;
import net.azisaba.lgw.core.listeners.DamageListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.PlayerControlListener;
import net.azisaba.lgw.core.listeners.others.*;
import net.azisaba.lgw.core.listeners.signs.CustomMatchSignListener;
import net.azisaba.lgw.core.listeners.signs.EntrySignListener;
import net.azisaba.lgw.core.listeners.signs.JoinAfterSignListener;
import net.azisaba.lgw.core.listeners.signs.MatchModeSignListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisableNormalWeaponsInNewYearPvEListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisablePvEsDuringMatchListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisablePvEsInLobbyListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisableToysDuringMatchListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.DisableWaveDuringMatchListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.LimitOneShotPerMatchListener;
import net.azisaba.lgw.core.sql.SQLConnection;
import net.azisaba.lgw.core.tasks.CrackShotLagFixTask;
import net.azisaba.lgw.core.tasks.SignRemoveTask;
import net.azisaba.lgw.core.util.BattleTeam;
import net.azisaba.lgw.core.util.Chat;
import net.azisaba.lgw.core.util.ClockMachine;
import net.azisaba.lgw.core.util.LGWExpansion;
import net.azisaba.lgw.core.util.LgwLog;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class LeonGunWar extends JavaPlugin {
    private final Logger plLogger = LgwLog.getLogger(this.getClass());
    public static final String PL_ID = "leongunwar";
    public static final String GAME_PREFIX = Chat.f("&7[&6PvP&7]&r ");
    public static final String SIGN_ACTIVE = Chat.f("&a[ACTIVE]");
    public static final String SIGN_INACTIVE = Chat.f("&c[INACTIVE]");
    public static List<BukkitTask> timeTaskList = new ArrayList<>();
    public static boolean doubleRewardEnable;
    public static Map<UUID, Long> matchJoin = new HashMap<>();
    public static Map<BattleTeam, BukkitTask> leaderSelectionTaskMap = new HashMap<>();
    // plugin
    private static LeonGunWar plugin;
    private static JSONMessage quickBar;
    private final MatchStartCountdown matchStartCountdown = new MatchStartCountdown();
    private final MapSelectCountdown mapSelectCountdown = new MapSelectCountdown();
    private final ScoreboardDisplayer scoreboardDisplayer = new ScoreboardDisplayer();
    private final MatchManager manager = new MatchManager();
    private final AssistStreaks assistStreaks = new AssistStreaks();
    private final KillStreaks killStreaks = new KillStreaks();
    private final TradeBoardManager tradeBoardManager = new TradeBoardManager();
    private MainConfig mainConfig;
    private KillStreaksConfig killStreaksConfig;
    private AssistStreaksConfig assistStreaksConfig;
    private SpawnsConfig spawnsConfig;
    private MapsConfig mapsConfig;
    private DatabaseConfig databaseConfig;
    private SyogoConfig syogoConfig;
    private WeaponControlConfig weaponControlConfig;
    private ItemsConfig itemsConfig;

    private SQLConnection sqlConnection;

    public static JSONMessage getQuickBar() {
        return quickBar;
    }

    public static LeonGunWar getPlugin() {
        return plugin;
    }

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

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new LGWExpansion(this).register(); //
        }


        // 設定ファイルを読み込むクラスの初期化
        saveDefaultConfig();
        mainConfig = new MainConfig(this);
        killStreaksConfig = new KillStreaksConfig(this);
        assistStreaksConfig = new AssistStreaksConfig(this);
        spawnsConfig = new SpawnsConfig(this);
        mapsConfig = new MapsConfig(this);
        databaseConfig = new DatabaseConfig(this);
        syogoConfig = new SyogoConfig(this);
        weaponControlConfig = new WeaponControlConfig(this);
        itemsConfig = new ItemsConfig(this);
        // 設定ファイルを読み込む
        try {
            mainConfig.loadConfig();
            killStreaksConfig.loadConfig();
            assistStreaksConfig.loadConfig();
            spawnsConfig.loadConfig();
            mapsConfig.loadConfig();
            databaseConfig.loadConfig();
            syogoConfig.loadConfig();
            weaponControlConfig.loadConfig();
            itemsConfig.loadConfig();
        } catch (IOException | InvalidConfigurationException exception) {
            plLogger.error("Failed to load config", exception);
        }

        // 初期化が必要なファイルを初期化する
        manager.initialize();
        tradeBoardManager.init();
        plLogger.info("ファイルの準備が完了しました。");
;
        sqlConnection = new SQLConnection(databaseConfig);

        // コマンドのインスタンスに渡す必要があるListener
        LimitActionListener preventItemDropListener = new LimitActionListener();

        // コマンドの登録
        registerCommand("leongunwaradmin", new LgwAdminCommand());
        registerCommand("uav", new UAVCommand());
        //registerCommand("match", new MatchCommand());
        registerCommand("kiai", new KIAICommand());
        registerCommand("resourcepack", new ResourcePackCommand());
        registerCommand("adminchat", new AdminChatCommand());
        registerCommand("limit", new LimitCommand(preventItemDropListener));
        registerCommand("mapvote", new MapVoteCommand());
        registerCommand("lsyogo", new LSyogoCommand());
        registerCommand("spawn", new SpawnCommand());
        registerCommand("noticewar", new SiaiTuutiCommand());
        registerCommand("toggledoublereward", new ToggleDoubleReward());
        plLogger.info("コマンドの登録完了しました。");

        // タブ補完の登録
        //registerCommand("leongunwaradmin").setTabCompleter(new LgwAdminCommand());
        //registerCommand("match").setTabCompleter(new MatchCommand());

        // コマンドの権限がない時のメッセージの指定
        //registerCommand("leongunwaradmin").setPermissionMessage(Chat.f("&c権限がありません！"));
        //registerCommand("uav").setPermissionMessage(Chat.f("&c権限がありません！"));
        //registerCommand("match").setPermissionMessage(Chat.f("&c権限がありません！"));
        //registerCommand("kiai").setPermissionMessage(Chat.f("&c権限がありません！"));
        //registerCommand("resourcepack").setPermissionMessage(Chat.f("&c権限がありません！"));
        //registerCommand("adminchat").setPermissionMessage(Chat.f("&c権限がありません！"));

        // リスナーの登録
        registerEvents(new MatchControlListener(),
                new EntrySignListener(),
                new MatchModeSignListener(),
                new JoinAfterSignListener(),
                new CustomMatchSignListener(),
                new MatchStartDetectListener(),
                new DamageListener(),
                new PlayerControlListener());

        // リスナーの登録 (modes)
        Bukkit.getPluginManager().registerEvents(new TeamDeathMatchListener(), this);
        Bukkit.getPluginManager().registerEvents(new HijackListener(), this);
        Bukkit.getPluginManager().registerEvents(new TDMNoLimitListener(), this);
        Bukkit.getPluginManager().registerEvents(new LeaderDeathMatchListener(), this);
        Bukkit.getPluginManager().registerEvents(new CustomTDMListener(), this);

        // リスナーの登録 (others)
        registerEvents(new NoArrowGroundListener(),
                new NoKnockbackListener(),
                new DisableItemDamageListener(),
                new DisableOpenInventoryListener(),
                new DisableOffhandListener(),
                new EnableKeepInventoryListener(),
                new RespawnKillProtectionListener(),
                new AutoRespawnListener(),
                new PlayerDeathListener(),
                new AfkKickEntryListener(),
                new StreaksListener(),
                new DisableRecipeListener(),
                new CrackShotLimitListener(),
                new TradeBoardListener(),
                new DisableTNTBlockDamageListener(),
                new SignWithColorListener(),
                new DisableChangeItemListener(),
                new FixStrikesCooldownListener(),
                new DisableBlockInteractListener());
        if (this.mainConfig.isLobby) {
            registerEvents(new OnsenListener());
            registerEvents(new LobbyListener());
            plLogger.info("ロビー用のリスナーを登録しました。");
        }
        registerEvents(new AdminChatListener((AdminChatCommand) Bukkit.getPluginCommand("adminchat").getExecutor()),
                new CrackShotLagFixListener(),
                preventItemDropListener,
                new DisableHopperPickupListener(),
                new NoFishingOnFightListener(),
                new KillVillagerOnChunkLoadListener(),
                new PreventEscapeListener(),
                new RemoveKillStreakScoreListener());

        // 武器コントロールリスナーの登録 (weaponcontrols)
        registerEvents(new DisableToysDuringMatchListener(),
                new DisablePvEsDuringMatchListener(),
                new DisablePvEsInLobbyListener(),
                new DisableNormalWeaponsInNewYearPvEListener(),
                new DisableWaveDuringMatchListener(),
                new LimitOneShotPerMatchListener());

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        // SignRemoveTask (60秒後に最初の実行、それからは10分周期で実行)
        new SignRemoveTask().runTaskTimer(this, 20 * 60, 20 * 60 * 10);
        new CrackShotLagFixTask().runTaskTimer(this, 0, 20 * 60);
        doubleRewardEnable = ClockMachine.isWithinRewardTime();
        if (getConfig().getBoolean("DoubleRewardTaskEnable", false)) {
            new ClockMachine().doubleRewardTaskStarter();
        }

        plLogger.info("{} が有効化されました。", getName());
    }

    public void registerEvents(Listener... listeners) {
        PluginManager pm = Bukkit.getPluginManager();
        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    /**
     * register command
     * @param commandName name of command
     * @param commandExecutor executor of command
     * @return command instance. if failure, returns null.
     */
    @Nullable
    public PluginCommand registerCommand(String commandName, @Nullable CommandExecutor commandExecutor) {
        PluginCommand cmd = Bukkit.getPluginCommand(commandName);
        if(cmd == null) {
            plLogger.warn("Failed to get command instance of {}", commandName);
            return null;
        }

        if(commandExecutor != null) {
            cmd.setExecutor(commandExecutor);
        }
        return cmd;
    }

    @Override
    public void onDisable() {
        // Plugin終了時の処理を呼び出す
        manager.onDisablePlugin();

        this.sqlConnection.onDisable();

        // 武器交換掲示板の看板を保存
        tradeBoardManager.saveAll();

        plLogger.info(Chat.f("{0} が無効化されました。", getName()));
    }

    public MatchManager getManager() {
        return manager;
    }

    public MainConfig getMainConfig() {
        return mainConfig;
    }

    public MapsConfig getMapsConfig() {
        return mapsConfig;
    }

    public SpawnsConfig getSpawnsConfig() {
        return spawnsConfig;
    }

    public SyogoConfig getSyogoConfig() {
        return syogoConfig;
    }

    public MatchStartCountdown getMatchStartCountdown() {
        return matchStartCountdown;
    }

    public AssistStreaksConfig getAssistStreaksConfig() {
        return assistStreaksConfig;
    }

    public AssistStreaks getAssistStreaks() {
        return assistStreaks;
    }

    public KillStreaks getKillStreaks() {
        return killStreaks;
    }

    public KillStreaksConfig getKillStreaksConfig() {
        return killStreaksConfig;
    }

    public TradeBoardManager getTradeBoardManager() {
        return tradeBoardManager;
    }

    public ScoreboardDisplayer getScoreboardDisplayer() {
        return scoreboardDisplayer;
    }

    public SQLConnection getSqlConnection() {
        return sqlConnection;
    }
}

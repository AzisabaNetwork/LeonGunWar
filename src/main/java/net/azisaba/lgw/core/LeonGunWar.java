package net.azisaba.lgw.core;

import lombok.Getter;
import me.rayzr522.jsonmessage.JSONMessage;
import net.azisaba.lgw.core.api.limit.LimitActionAPI;
import net.azisaba.lgw.core.battlesystem.MatchManager;
import net.azisaba.lgw.core.commands.LGWCommands;
import net.azisaba.lgw.core.configs.AssistStreaksConfig;
import net.azisaba.lgw.core.configs.DatabaseConfig;
import net.azisaba.lgw.core.configs.ItemsConfig;
import net.azisaba.lgw.core.configs.KillStreaksConfig;
import net.azisaba.lgw.core.configs.MainConfig;
import net.azisaba.lgw.core.configs.MapsConfig;
import net.azisaba.lgw.core.configs.SpawnsConfig;
import net.azisaba.lgw.core.configs.SyogoConfig;
import net.azisaba.lgw.core.configs.WeaponControlConfig;
import net.azisaba.lgw.core.listeners.DamageListener;
import net.azisaba.lgw.core.listeners.MatchControlListener;
import net.azisaba.lgw.core.listeners.MatchStartDetectListener;
import net.azisaba.lgw.core.listeners.PlayerControlListener;
import net.azisaba.lgw.core.battlesystem.gamemode.leaderdeathmatch.LeaderDeathMatchListener;
import net.azisaba.lgw.core.battlesystem.gamemode.teamdeathmatch.TDMNoLimitListener;
import net.azisaba.lgw.core.battlesystem.gamemode.teamdeathmatch.TeamDeathMatchListener;
import net.azisaba.lgw.core.listeners.others.AfkKickEntryListener;
import net.azisaba.lgw.core.listeners.others.AutoRespawnListener;
import net.azisaba.lgw.core.listeners.others.CrackShotLagFixListener;
import net.azisaba.lgw.core.listeners.others.DisableBlockInteractListener;
import net.azisaba.lgw.core.listeners.others.DisableChangeItemListener;
import net.azisaba.lgw.core.listeners.others.DisableHopperPickupListener;
import net.azisaba.lgw.core.listeners.others.DisableItemDamageListener;
import net.azisaba.lgw.core.listeners.others.DisableOffhandListener;
import net.azisaba.lgw.core.listeners.others.DisableOpenInventoryListener;
import net.azisaba.lgw.core.listeners.others.DisableRecipeListener;
import net.azisaba.lgw.core.listeners.others.DisableTNTBlockDamageListener;
import net.azisaba.lgw.core.listeners.others.EnableKeepInventoryListener;
import net.azisaba.lgw.core.listeners.others.FixStrikesCooldownListener;
import net.azisaba.lgw.core.listeners.others.LimitActionListener;
import net.azisaba.lgw.core.listeners.others.LobbyListener;
import net.azisaba.lgw.core.listeners.others.NoArrowGroundListener;
import net.azisaba.lgw.core.listeners.others.NoFishingOnFightListener;
import net.azisaba.lgw.core.listeners.others.NoKnockbackListener;
import net.azisaba.lgw.core.listeners.others.OnsenListener;
import net.azisaba.lgw.core.listeners.others.PlayerDeathListener;
import net.azisaba.lgw.core.listeners.others.PreventEscapeListener;
import net.azisaba.lgw.core.listeners.others.RemoveKillStreakScoreListener;
import net.azisaba.lgw.core.listeners.others.RespawnKillProtectionListener;
import net.azisaba.lgw.core.listeners.others.SignWithColorListener;
import net.azisaba.lgw.core.listeners.others.StreaksListener;
import net.azisaba.lgw.core.listeners.signs.EntrySignListener;
import net.azisaba.lgw.core.listeners.signs.JoinAfterSignListener;
import net.azisaba.lgw.core.listeners.signs.MatchModeSignListener;
import net.azisaba.lgw.core.listeners.weaponcontrols.LimitOneShotPerMatchListener;
import net.azisaba.lgw.core.sql.SQLConnection;
import net.azisaba.lgw.core.tasks.CrackShotLagFixTask;
import net.azisaba.lgw.core.battlesystem.BattleTeam;
import net.azisaba.lgw.core.api.util.Chat;
import net.azisaba.lgw.core.util.LGWExpansion;
import net.azisaba.lgw.core.util.LgwLog;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
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
    private final LimitActionAPI limitActionAPI = new LimitActionAPI();
    private LGWCommands lgwCommands;
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
        plLogger.info("ファイルの準備が完了しました。");
;
        sqlConnection = new SQLConnection(databaseConfig);

        // コマンドのインスタンスに渡す必要があるListener
        LimitActionListener preventItemDropListener = new LimitActionListener(this);

        // コマンドの登録
        lgwCommands = new LGWCommands(this);
        plLogger.info("コマンドの登録完了しました。");

        // リスナーの登録
        registerEvents(new MatchControlListener(),
                new EntrySignListener(),
                new MatchModeSignListener(),
                new JoinAfterSignListener(),
                new MatchStartDetectListener(),
                new DamageListener(),
                new PlayerControlListener());

        // リスナーの登録 (modes)
        registerEvents(new TeamDeathMatchListener(),
                new TDMNoLimitListener(),
                new LeaderDeathMatchListener()
        );

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
        registerEvents(
                new CrackShotLagFixListener(),
                preventItemDropListener,
                new DisableHopperPickupListener(),
                new NoFishingOnFightListener(),
                new PreventEscapeListener(),
                new RemoveKillStreakScoreListener());

        // 武器コントロールリスナーの登録 (weaponcontrols)
        registerEvents(
                new LimitOneShotPerMatchListener());

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        new CrackShotLagFixTask().runTaskTimer(this, 0, 20 * 60);

        plLogger.info("{} が有効化されました。", getName());
    }

    public void registerEvents(Listener... listeners) {
        PluginManager pm = Bukkit.getPluginManager();
        for (Listener listener : listeners) {
            pm.registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {
        // Plugin終了時の処理を呼び出す
        manager.onDisablePlugin();

        this.sqlConnection.onDisable();

        lgwCommands.onDisable();

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


    public ScoreboardDisplayer getScoreboardDisplayer() {
        return scoreboardDisplayer;
    }

    public SQLConnection getSqlConnection() {
        return sqlConnection;
    }

    public LimitActionAPI getLimitActionAPI() {
        return limitActionAPI;
    }
}

package net.azisaba.lgw.core;

import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;
import net.azisaba.lgw.core.api.config.LGWConfig;
import net.azisaba.lgw.core.api.integration.papi.LGWExpansion;
import net.azisaba.lgw.core.api.integration.papi.PlaceHolderAPI;
import net.azisaba.lgw.core.api.limit.LimitActionAPI;
import net.azisaba.lgw.core.api.util.Chat;
import net.azisaba.lgw.core.battlesystem.BattleTeam;
import net.azisaba.lgw.core.battlesystem.MatchManager;
import net.azisaba.lgw.core.battlesystem.gamemode.leaderdeathmatch.LeaderDeathMatchListener;
import net.azisaba.lgw.core.battlesystem.gamemode.teamdeathmatch.TDMNoLimitListener;
import net.azisaba.lgw.core.battlesystem.gamemode.teamdeathmatch.TeamDeathMatchListener;
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
import net.azisaba.lgw.core.util.LGWComponent;
import net.azisaba.lgw.core.util.LgwLog;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

import java.io.File;
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
    private final MatchStartCountdown matchStartCountdown = new MatchStartCountdown();
    private final MapSelectCountdown mapSelectCountdown = new MapSelectCountdown();
    private final ScoreboardDisplayer scoreboardDisplayer = new ScoreboardDisplayer();
    private final MatchManager manager = new MatchManager();
    private final AssistStreaks assistStreaks = new AssistStreaks();
    private final KillStreaks killStreaks = new KillStreaks();
    private final LimitActionAPI limitActionAPI = new LimitActionAPI();
    private File configFile;
    private LGWConfig config;
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

    /**
     * Get quick bar component
     * For developer: It's deprecated. Use {@link LGWComponent#QUICK_BAR}
     * @return Component for quick bar
     */
    @Deprecated(forRemoval = true, since = "4.2.0")
    public static Component getQuickBar() {
        return LGWComponent.QUICK_BAR;
    }

    public static LeonGunWar getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        Component.join(
                JoinConfiguration.spaces(),
                LGWComponent.surrounded(
                        NamedTextColor.GRAY,
                        Component.text("Quick").color(NamedTextColor.AQUA)
                ),
                Component.text("ここをクリック →"),
                Component.text("[エントリー]")
                        .color(NamedTextColor.GREEN)
                        .clickEvent(ClickEvent.runCommand("/leongunwar:match entry")),
                Component.text("[エントリー解除]")
                        .color(NamedTextColor.RED)
                        .clickEvent(ClickEvent.runCommand("/leongunwar:match leave")),
                Component.text("[途中参加]")
                        .color(NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.runCommand("/leongunwar:match rejoin"))
        );

        PlaceHolderAPI.getApi().register(new LGWExpansion(this));

        // 設定ファイルを読み込むクラスの初期化
        saveDefaultConfig();
        plLogger.info("設定ファイルを読み込み中です...");
        if(tryLoadConfig()) {
            plLogger.error("設定ファイルを編集してから、再度有効化してください。");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        } else {
            plLogger.info("設定ファイルの読み込みが完了しました。");
        }
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
        if (this.config.mainConfig.isLobby) {
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

    /**
     * Try to load config
     * @return is succeeded
     */
    public boolean tryLoadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        configFile.mkdirs();

        if(!configFile.exists()) {
            YamlConfigurations.save(configFile.toPath(), LGWConfig.class, new LGWConfig());
            return false;
        }

        config = YamlConfigurations.load(configFile.toPath(), LGWConfig.class);
        return true;
    }

    public MatchManager getManager() {
        return manager;
    }

    public LGWConfig config() {
        return config;
    }

    @Deprecated(forRemoval = true, since = "4.1.0")
    public MainConfig getMainConfig() {
        return mainConfig;
    }

    @Deprecated(forRemoval = true, since = "4.1.0")
    public MapsConfig getMapsConfig() {
        return mapsConfig;
    }

    @Deprecated(forRemoval = true, since = "4.1.0")
    public SpawnsConfig getSpawnsConfig() {
        return spawnsConfig;
    }

    @Deprecated(forRemoval = true, since = "4.1.0")
    public SyogoConfig getSyogoConfig() {
        return syogoConfig;
    }

    public MatchStartCountdown getMatchStartCountdown() {
        return matchStartCountdown;
    }

    @Deprecated(forRemoval = true, since = "4.1.0")
    public AssistStreaksConfig getAssistStreaksConfig() {
        return assistStreaksConfig;
    }

    public AssistStreaks getAssistStreaks() {
        return assistStreaks;
    }

    public KillStreaks getKillStreaks() {
        return killStreaks;
    }

    @Deprecated(forRemoval = true, since = "4.1.0")
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

package net.azisaba.lgw.core.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import net.azisaba.lgw.core.LeonGunWar;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LGWCommands {
    private static final Logger logger = LoggerFactory.getLogger(LGWCommands.class);
    private final LeonGunWar plugin;
    private final PaperCommandManager commandManager;

    public LGWCommands(LeonGunWar plugin) {
        this.plugin = plugin;

        // acf commands
        commandManager = new PaperCommandManager(plugin);
        commandManager.registerCommand(new LimitCommand());

        // normal commands
        registerCommand("leongunwaradmin", new LgwAdminCommand());
        registerCommand("uav", new UAVCommand());
//        registerCommand("limit", new LimitCommand(preventItemDropListener));
        registerCommand("mapvote", new MapVoteCommand());
        registerCommand("lsyogo", new LSyogoCommand());
        registerCommand("spawn", new OverwriteCommand());
        registerCommand("noticewar", new SiaiTuutiCommand());
    }

    private void register(BaseCommand command) {
        commandManager.registerCommand(command, true);
    }

    public void onDisable() {
        commandManager.unregisterCommands();
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
            logger.warn("Failed to get command instance of {}", commandName);
            return null;
        }

        if(commandExecutor != null) {
            cmd.setExecutor(commandExecutor);
        }
        return cmd;
    }
}

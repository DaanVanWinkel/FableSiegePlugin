package com.fable.fablesiegeplugin;

import co.aikar.commands.BukkitMessageFormatter;
import co.aikar.commands.MessageType;
import co.aikar.commands.PaperCommandManager;
import com.fable.fablesiegeplugin.commands.MainCommand;
import com.fable.fablesiegeplugin.config.DataManager;
import com.fable.fablesiegeplugin.listeners.DeathListener;
import com.fable.fablesiegeplugin.listeners.EntityDamageByEntity;
import com.fable.fablesiegeplugin.utils.Utils;
import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.core.ParticleNativeCore;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Getter
    private static Main instance;
    @Getter
    private DataManager dataManager;
    @Getter
    private PaperCommandManager manager;
    @Getter
    private ParticleNativeAPI particleApi;
    @Getter
    private MainCommand mainCommand;

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;
        dataManager = new DataManager();
        manager = new PaperCommandManager(this);
        particleApi = ParticleNativeCore.loadAPI(this);
        mainCommand = new MainCommand();

        // Register listeners
        getServer().getPluginManager().registerEvents(new DeathListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageByEntity(), this);

        // This is last shit to run onEnable
        registerCommands();
        getLogger().info("Fable Siege Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Fable Siege Plugin has been disabled!");
    }

    private void registerCommands() {
        manager.enableUnstableAPI("help");

        // Command Formatting
        manager.setFormat(MessageType.ERROR, new BukkitMessageFormatter(ChatColor.RED, ChatColor.YELLOW, ChatColor.RED));
        manager.setFormat(MessageType.SYNTAX, new BukkitMessageFormatter(ChatColor.RED, ChatColor.DARK_PURPLE, ChatColor.WHITE));
        manager.setFormat(MessageType.INFO, new BukkitMessageFormatter(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE, ChatColor.WHITE));
        manager.setFormat(MessageType.HELP, new BukkitMessageFormatter(ChatColor.DARK_PURPLE, ChatColor.LIGHT_PURPLE, ChatColor.WHITE));

        // Register commands
        //manager.registerCommand(new RoleplayCommand());
        manager.registerCommand(mainCommand);

        // Register completions
        manager.getCommandCompletions().registerCompletion("maps", c -> Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Sieges")));
        manager.getCommandCompletions().registerCompletion("teams", c -> Utils.getListFromMapKeyset(dataManager.getConfig().getMap("Teams")));

        manager.setDefaultExceptionHandler((command, registeredCommand, sender, args, t) -> {
            getLogger().warning("Error occurred while executing command " + command.getName());
            return false; // mark as unhandeled, sender will see default message
        });
    }

    public ParticleNativeAPI getParticleAPI() {
        return particleApi;
    }
}

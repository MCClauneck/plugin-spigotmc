package io.github.mcclauneck.spigotmc;

import io.github.mcclauneck.api.database.IEconomyDatabase;
import io.github.mcclauneck.bukkit.listener.PlayerJoinCurrencyListener;
import io.github.mcclauneck.common.MCEconomyProvider;
import io.github.mcclauneck.common.database.api.MCEconomyAPI;
import io.github.mcclauneck.common.database.mongodb.MCEconomyMongoDB;
import io.github.mcclauneck.common.database.mysql.MCEconomyMySQL;
import io.github.mcclauneck.common.database.postgresql.MCEconomyPostgreSQL;
import io.github.mcclauneck.common.database.sqlite.MCEconomySQLite;
import io.github.mcclauneck.common.http.MCEconomyHttpClient;

import io.github.mcengine.mcextension.api.HostContext;
import io.github.mcengine.mcextension.common.MCExtensionManager;
import io.github.mcengine.mcextension.spigotmc.commands.MCExtensionCommand;
import io.github.mcengine.mcextension.spigotmc.context.SpigotHostContext;
import io.github.mcengine.mcextension.spigotmc.tabcompleters.MCExtensionTabCompleter;

import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

/**
 * Main plugin class for MCEconomy on SpigotMC.
 * It integrates the central MCEconomyProvider and initializes the MCExtension environment.
 */
public class MCEconomyPlugin extends JavaPlugin {

    /**
     * Provider serving as the central API and entry point for economy workflows and queries.
     */
    private MCEconomyProvider provider;

    /**
     * Manager handling the dynamic loading and unloading of extensions.
     */
    private MCExtensionManager extensionManager;

    /**
     * Host context for MCExtension platform abstraction.
     */
    private HostContext hostContext;

    @Override
    public void onEnable() {
        Logger logger = getLogger();
        
        // Load default config
        saveDefaultConfig();
        if (!getConfig().getBoolean("enable", true)) {
            logger.warning("Plugin is set to 'enable: false' in config.yml. Shutting down MCEconomy...");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        IEconomyDatabase database;
        String dbType = getConfig().getString("database.type", "sqlite").toLowerCase();

        switch (dbType) {
            case "postgresql":
                logger.info("Initializing PostgreSQL Database...");
                database = new MCEconomyPostgreSQL(
                    getConfig().getString("database.postgresql.host"),
                    getConfig().getInt("database.postgresql.port"),
                    getConfig().getString("database.postgresql.database"),
                    getConfig().getString("database.postgresql.username"),
                    getConfig().getString("database.postgresql.password")
                );
                break;
            case "mysql":
                logger.info("Initializing MySQL Database...");
                database = new MCEconomyMySQL(
                    getConfig().getString("database.mysql.host"),
                    getConfig().getInt("database.mysql.port"),
                    getConfig().getString("database.mysql.database"),
                    getConfig().getString("database.mysql.username"),
                    getConfig().getString("database.mysql.password")
                );
                break;
            case "api":
                logger.info("Initializing Remote API Database...");
                database = new MCEconomyAPI(
                    getConfig().getString("database.api.url"),
                    getConfig().getString("database.api.token")
                );
                break;
            case "mongodb":
                logger.info("Initializing MongoDB Database...");
                database = new MCEconomyMongoDB(
                    getConfig().getString("database.mongodb.uri"),
                    getConfig().getString("database.mongodb.database")
                );
                break;
            default:
            case "sqlite":
                logger.info("Initializing SQLite Database...");
                database = new MCEconomySQLite(
                    new File(getDataFolder(), getConfig().getString("database.sqlite.file", "economy.db"))
                );
                break;
        }

        // Ensure database tables are fully created before proceeding
        database.initialize().join();

        // Read the configurable currency list ("code,Display Name") from config.yml
        Map<String, String> currencies = MCEconomyProvider.parseCurrencyList(getConfig().getStringList("currencies"));

        // Initialize Central Economy Provider with the configured currencies
        this.provider = new MCEconomyProvider(database, currencies);

        // Pre-load all currencies into cache
        logger.info("Loading currencies into cache...");
        this.provider.loadCurrencies().join();

        // Generate the default currencies on first run (creates any that are missing)
        logger.info("Ensuring default currencies exist...");
        this.provider.ensureDefaultCurrencies().join();

        // Keep the default currencies present by re-checking whenever a player joins
        getServer().getPluginManager().registerEvents(new PlayerJoinCurrencyListener(this.provider, logger), this);

        logger.info("Initializing Extension System...");
        
        // Initialize MCExtension API via SpigotHostContext
        this.hostContext = new SpigotHostContext(this);
        
        // Define -1 for unlimited extensions and set platform to 'spigotmc'
        this.extensionManager = new MCExtensionManager(-1, "spigotmc");
        
        // Use an asynchronous executor to match MCExtension's Async-First Design
        Executor extensionExecutor = command -> Bukkit.getScheduler().runTaskAsynchronously(this, command);
        
        // Load all external extensions
        this.extensionManager.loadAllExtensions(this.hostContext, extensionExecutor);

        // Register MCExtension Command & TabCompleter
        PluginCommand extensionCmd = getCommand("mceconomyextension");
        if (extensionCmd != null) {
            extensionCmd.setExecutor(new MCExtensionCommand(this.hostContext, this.extensionManager, extensionExecutor));
            extensionCmd.setTabCompleter(new MCExtensionTabCompleter(this.extensionManager));
        } else {
            logger.warning("Command 'mceconomyextension' is not defined in plugin.yml. Skipped command registration.");
        }

        logger.info("MCEconomy Plugin has been successfully enabled!");
    }

    @Override
    public void onDisable() {
        // Safely disable all loaded extensions
        if (this.extensionManager != null && this.hostContext != null) {
            Executor disableExecutor = command -> {
                // If the plugin is still enabled, run the disable task asynchronously to prevent main thread blocking
                if (this.isEnabled()) {
                    Bukkit.getScheduler().runTaskAsynchronously(this, command);
                } else {
                    // Fallback to synchronous execution if the server is shutting down
                    command.run(); 
                }
            };
            this.extensionManager.disableAllExtensions(this.hostContext, disableExecutor);
        }

        // Safely shutdown the database provider
        if (this.provider != null) {
            getLogger().info("Shutting down database connection...");
            this.provider.shutdown().join();
        }

        getLogger().info("MCEconomy Plugin has been disabled!");
    }
    
    /**
     * Gets the MCEconomyProvider instance which serves as the central API for economy features.
     * @return The active MCEconomyProvider.
     */
    public MCEconomyProvider getProvider() {
        return this.provider;
    }

    /**
     * Gets the shared {@link java.net.http.HttpClient} used by the plugin for
     * remote API calls.
     * <p>
     * Extensions that talk to the central server should reuse this client
     * instead of opening their own, so the whole plugin shares a single
     * connection pool.
     * </p>
     *
     * @return The shared HTTP client.
     */
    public java.net.http.HttpClient getHttpClient() {
        return MCEconomyHttpClient.shared();
    }
}

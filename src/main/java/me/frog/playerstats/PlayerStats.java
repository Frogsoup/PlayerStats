package me.frog.playerstats;

import me.frog.playerstats.command.StatsCommand;
import me.frog.playerstats.database.DatabaseManager;
import me.frog.playerstats.event.Events;
import me.frog.playerstats.util.FileManager;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class PlayerStats extends Plugin {

    private static PlayerStats instance;

    private FileManager fileManager = new FileManager();
    private Configuration configuration;

    @Override
    public void onEnable() {
        if(instance == null) instance = this;

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(fileManager.loadConfigurationFile(this, "config.yml"));
        }catch(IOException exception) {
            exception.printStackTrace();
        }

        String address = configuration.getString("address");
        String username = configuration.getString("username");
        String password = configuration.getString("password");
        String database = configuration.getString("database");

        DatabaseManager.establishConnection(address, username, password, database);
        DatabaseManager.createDatabaseIfNotExist();

        ProxyServer proxyServer = ProxyServer.getInstance();
        proxyServer.getPluginManager().registerListener(this, new Events());
        proxyServer.getPluginManager().registerCommand(this, new StatsCommand(this));
    }

    @Override
    public void onDisable() { instance = null; }

    /**
     * Called to get the main configuration file.
     *
     * @return The config.yml file.
     */
    public Configuration getConfigurationFile() { return configuration; }

    /**
     * Gets the instance of the plugin.
     *
     * @return The instance.
     */
    public static PlayerStats getInstance() { return instance; }
}

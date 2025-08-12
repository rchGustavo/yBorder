package me.ylabui.yBorder;

import me.ylabui.yBorder.command.BorderCommand;
import me.ylabui.yBorder.command.BorderTabComplete;
import me.ylabui.yBorder.listener.BorderListener;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public final class Main extends JavaPlugin {

    //Teste de Git

    private FileConfiguration config;
    private File configFile;

    @Override
    public void onEnable() {
        loadFunction();
    }

    @Override
    public void onDisable() {
        getLogger().info("yBorder Disable!");
    }

    public void loadFunction() {
        setupConfig();

        getCommand("border").setExecutor(new BorderCommand(this));
        getCommand("border").setTabCompleter(new BorderTabComplete());
        getServer().getPluginManager().registerEvents(new BorderListener(this), this);

        getLogger().info(" ");
        getLogger().info("-- yBorder --");
        getLogger().info("Plugin successfully actived!");
        getLogger().info("Developed by yLabui.");
        getLogger().info("Version: " + getDescription().getVersion());
        getLogger().info(" ");
    }

    public void setupConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void reloadConfigFile() {
        setupConfig();
    }

    public FileConfiguration getCustomConfig() {
        return config;
    }

    public String getString(String path, Map<String, String> placeholders) {
        String value = config.getString(path);
        if (value == null) return "§cMessage not found:: " + path;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            value = value.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return ChatColor.translateAlternateColorCodes('&', value);
    }

    public String getString(String path) {
        return getString(path, new HashMap<>());
    }
}

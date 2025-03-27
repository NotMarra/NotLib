package com.notmarra.notlib.extensions;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class NotPlugin extends JavaPlugin {
    private static NotPlugin instance;
    private static final Map<String, Runnable> ON_PLUGIN_ENABLED_CALLBACKS = new HashMap<>();
    private static final Map<String, FileConfiguration> CONFIGS = new HashMap<>();
    private static final Map<String, NotListener> LISTENERS = new HashMap<>();
    private static final Map<String, NotCommandGroup> CMDGROUPS = new HashMap<>();

    public void addPluginEnabledCallback(String pluginId, Runnable callback) { ON_PLUGIN_ENABLED_CALLBACKS.put(pluginId, callback); }
    public void initPluginCallbacks() {}

    public void addListener(String id, NotListener listener) { LISTENERS.put(id, listener); }
    public void initListeners() {}; // addListener("listener_id", new Listener(this));
    public NotListener getListener(String id) { return LISTENERS.get(id); }

    public void addCommandGroup(String id, NotCommandGroup cmdGroup) { CMDGROUPS.put(id, cmdGroup); }
    public void initCommandGroups() {}; // addCommandManager("cmdgroup_id", new CommandManager(this));
    public NotCommandGroup getCommandGroup(String id) { return CMDGROUPS.get(id); }

    private void loadConfigFiles() {
        CONFIGS.put("config.yml", this.getConfig());

        for (NotListener listener : LISTENERS.values()) {
            reloadConfig(listener.getConfigPath());
        }

        for (NotCommandGroup cmdGroup : CMDGROUPS.values()) {
            reloadConfig(cmdGroup.getConfigPath());
        }
    }

    @Override
    public void saveDefaultConfig() {
        InputStream mainResource = getResource("config.yml");
        if (mainResource != null) super.saveDefaultConfig();

        for (NotListener listener : LISTENERS.values()) {
            File configFile = new File(getDataFolder(), listener.getConfigPath());
            InputStream resource = getResource(listener.getConfigPath());
            if (!configFile.exists() && resource != null) {
                saveResource(listener.getConfigPath(), false);
            }
        }

        for (NotCommandGroup cmdGroup : CMDGROUPS.values()) {
            File configFile = new File(getDataFolder(), cmdGroup.getConfigPath());
            InputStream resource = getResource(cmdGroup.getConfigPath());
            if (!configFile.exists() && resource != null) {
                saveResource(cmdGroup.getConfigPath(), false);
            }
        }
    }

    @Override
    public void onEnable() {
        instance = this;

        NotMinecraftStuff.getInstance().initialize();

        // NOTE: this order is important
        this.initListeners();
        this.initCommandGroups();
        this.initPluginCallbacks();
        this.saveDefaultConfig();
        this.loadConfigFiles();

        for (String pluginId : ON_PLUGIN_ENABLED_CALLBACKS.keySet()) {
            if (Bukkit.getPluginManager().isPluginEnabled(pluginId)) {
                ON_PLUGIN_ENABLED_CALLBACKS.get(pluginId).run();
            }
        }

        LISTENERS.values().forEach(l -> l.register());
        CMDGROUPS.values().forEach(c -> c.register());
    }

    public static NotPlugin getInstance() { return instance; }
    public FileConfiguration getSubConfig(String file) { return CONFIGS.get(file); }
    public FileConfiguration reloadConfig(String file) {
        File configFile = new File(getDataFolder(), file);
        if (!configFile.exists()) return null;
        CONFIGS.put(file, YamlConfiguration.loadConfiguration(configFile));
        return CONFIGS.get(file);
    }
}
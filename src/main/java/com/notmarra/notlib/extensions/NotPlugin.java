package com.notmarra.notlib.extensions;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class NotPlugin extends JavaPlugin {
    private static final Map<String, Runnable> ON_PLUGIN_ENABLED_CALLBACKS = new HashMap<>();
    // <path, config>
    private static final Map<String, FileConfiguration> CONFIGS = new HashMap<>();
    // <id, listener>
    private static final Map<String, NotListener> LISTENERS = new HashMap<>();
    // <id, cmdGroup>
    private static final Map<String, NotCommandGroup> CMDGROUPS = new HashMap<>();
    // <path, [configurable]>
    private static final Map<String, List<NotConfigurable>> CONFIGURABLES = new HashMap<>();

    public final String CONFIG_YML = "config.yml";

    public void addPluginEnabledCallback(String pluginId, Runnable callback) { ON_PLUGIN_ENABLED_CALLBACKS.put(pluginId, callback); }
    public void initPluginCallbacks() {}

    public void addListener(String id, NotListener listener) {
        registerConfigurable(listener);
        LISTENERS.put(id, listener);
    }
    public void initListeners() {}; // addListener("listener_id", new Listener(this));
    public NotListener getListener(String id) { return LISTENERS.get(id); }

    public void addCommandGroup(String id, NotCommandGroup cmdGroup) {
        registerConfigurable(cmdGroup);
        CMDGROUPS.put(id, cmdGroup);
    }
    public void initCommandGroups() {}; // addCommandManager("cmdgroup_id", new CommandManager(this));
    public NotCommandGroup getCommandGroup(String id) { return CMDGROUPS.get(id); }

    public void registerConfigurable(NotConfigurable configurable) {
        String configPath = configurable.getConfigPath();
        if (configPath == null) return;
        CONFIGURABLES.computeIfAbsent(configPath, k -> new ArrayList<>()).add(configurable);
    }

    private void loadConfigFiles() {
        CONFIGS.put(CONFIG_YML, this.getConfig());

        for (NotListener listener : LISTENERS.values()) {
            String configPath = listener.getConfigPath();
            if (configPath == null) continue;
            if (CONFIGS.containsKey(configPath)) continue;
            reloadConfig(configPath);
        }

        for (NotCommandGroup cmdGroup : CMDGROUPS.values()) {
            String configPath = cmdGroup.getConfigPath();
            if (configPath == null) continue;
            if (CONFIGS.containsKey(configPath)) continue;
            reloadConfig(configPath);
        }
    }

    @Override
    public void saveDefaultConfig() {
        InputStream mainResource = getResource(CONFIG_YML);
        if (mainResource != null) super.saveDefaultConfig();

        for (NotListener listener : LISTENERS.values()) {
            String configPath = listener.getConfigPath();
            if (configPath == null) continue;
            File configFile = new File(getDataFolder(), configPath);
            InputStream resource = getResource(configPath);
            if (!configFile.exists() && resource != null) {
                saveResource(configPath, false);
            }
        }

        for (NotCommandGroup cmdGroup : CMDGROUPS.values()) {
            String configPath = cmdGroup.getConfigPath();
            if (configPath == null) continue;
            File configFile = new File(getDataFolder(), configPath);
            InputStream resource = getResource(configPath);
            if (!configFile.exists() && resource != null) {
                saveResource(configPath, false);
            }
        }
    }

    @Override
    public void onEnable() {
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

    public FileConfiguration getSubConfig(String file) { return CONFIGS.get(file); }
    public FileConfiguration reloadConfig(String file) {
        File configFile = new File(getDataFolder(), file);
        if (!configFile.exists()) return null;
        FileConfiguration newConfig = YamlConfiguration.loadConfiguration(configFile);
        CONFIGS.put(file, newConfig);
        if (CONFIGURABLES.containsKey(file)) {
            CONFIGURABLES.get(file).forEach(c -> c.reloadConfig(newConfig));
        }
        return newConfig;
    }
}
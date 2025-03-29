package com.notmarra.notlib.extensions;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class NotPlugin extends JavaPlugin {
    private final Map<String, Runnable> ON_PLUGIN_ENABLED_CALLBACKS = new HashMap<>();
    // <path, config>
    private final Map<String, FileConfiguration> CONFIGS = new HashMap<>();
    // <id, listener>
    private final Map<String, NotListener> LISTENERS = new HashMap<>();
    // <id, cmdGroup>
    private final Map<String, NotCommandGroup> CMDGROUPS = new HashMap<>();
    // <path, [configurable]>
    private final Map<String, List<NotConfigurable>> CONFIGURABLES = new HashMap<>();

    private NotTranslationManager translationManager;
    public NotTranslationManager getTranslationManager() { return translationManager; }

    public final String CONFIG_YML = "config.yml";

    public void addPluginEnabledCallback(String pluginId, Runnable callback) { ON_PLUGIN_ENABLED_CALLBACKS.put(pluginId, callback); }

    // addListener("listener_id", new Listener(this));
    public void addListener(String id, NotListener listener) { LISTENERS.put(id, listener); }
    public NotListener getListener(String id) { return LISTENERS.get(id); }

    // addCommandManager("cmdgroup_id", new CommandManager(this));
    public void addCommandGroup(String id, NotCommandGroup cmdGroup) { CMDGROUPS.put(id, cmdGroup); }
    public NotCommandGroup getCommandGroup(String id) { return CMDGROUPS.get(id); }

    public void registerConfigurable(NotConfigurable configurable, String configPath) {
        CONFIGURABLES.computeIfAbsent(configPath, k -> new ArrayList<>()).add(configurable);
        saveDefaultConfig(configPath);
    }

    public void registerConfigurable(NotConfigurable configurable) {
        List<String> configPaths = configurable.getConfigPaths();
        if (configPaths.isEmpty()) return;
        for (String configPath : configPaths) {
            registerConfigurable(configurable, configPath);
        }
    }

    public void saveDefaultConfig(String forConfig) {
        getLogger().info("SAVING DEFAULT CONFIG: " + forConfig);
        if (forConfig == null) return;
        if (CONFIGS.containsKey(forConfig)) return;
        File configFile = new File(getDataFolder(), forConfig);
        if (configFile.exists()) return;
        if (getResource(forConfig) == null) return;
        configFile.getParentFile().mkdirs();
        saveResource(forConfig, false);
    }

    @Override
    public void saveDefaultConfig() {
        saveDefaultConfig(CONFIG_YML);

        for (String configPath : CONFIGURABLES.keySet()) {
            saveDefaultConfig(configPath);
        }
    }

    private FileConfiguration loadConfigFile(String configPath) {
        getLogger().info("LOADING CONFIG: " + configPath);
        if (configPath == null) return null;
        if (CONFIGS.containsKey(configPath)) return null;
        File configFile = new File(getDataFolder(), configPath);
        if (!configFile.exists()) return null;
        CONFIGS.put(configPath, YamlConfiguration.loadConfiguration(configFile));
        return CONFIGS.get(configPath);
    }

    private void loadConfigFiles() {
        loadConfigFile(CONFIG_YML);

        for (List<NotConfigurable> configurables : CONFIGURABLES.values()) {
            configurables.forEach(c -> {
                c.getConfigPaths().forEach(configPath -> loadConfigFile(configPath));
            });
        }
    }

    public abstract void initNotPlugin();

    @Override
    public void onEnable() {
        NotMinecraftStuff.getInstance().initialize();

        this.translationManager = new NotTranslationManager(this);

        // NOTE: this order is important
        this.initNotPlugin();
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
        CONFIGS.put(file, YamlConfiguration.loadConfiguration(configFile));
        if (CONFIGURABLES.containsKey(file)) {
            CONFIGURABLES.get(file).forEach(c -> c.onConfigReload(List.of(file)));
        }
        return CONFIGS.get(file);
    }
}
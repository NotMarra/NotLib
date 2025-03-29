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

import com.notmarra.notlib.database.NotDatabase;
import com.notmarra.notlib.database.NotDatabaseManager;

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
    public NotTranslationManager tm() { return translationManager; }
    public String tm(String key) { return tm().get(key); }
    public List<String> tmList(String key) { return tm().getList(key); }

    private NotDatabaseManager databaseManager;
    public NotDatabaseManager db() { return databaseManager; }
    public NotDatabase db(String dbId) { return databaseManager.getDatabase(dbId); }

    public final String CONFIG_YML = "config.yml";

    public void addPluginEnabledCallback(String pluginId, Runnable callback) { ON_PLUGIN_ENABLED_CALLBACKS.put(pluginId, callback); }

    // addListener("listener_id", new Listener(this));
    public void addListener(String id, NotListener listener) { LISTENERS.put(id, listener); }
    public NotListener getListener(String id) { return LISTENERS.get(id); }

    // addCommandManager("cmdgroup_id", new CommandManager(this));
    public void addCommandGroup(String id, NotCommandGroup cmdGroup) { CMDGROUPS.put(id, cmdGroup); }
    public NotCommandGroup getCommandGroup(String id) { return CMDGROUPS.get(id); }

    public void registerConfigurable(NotConfigurable configurable, String configPath) {
        if (!CONFIGURABLES.containsKey(configPath)) {
            CONFIGURABLES.computeIfAbsent(configPath, k -> new ArrayList<>());
        } else {
            if (!CONFIGURABLES.get(configPath).contains(configurable)) {
                CONFIGURABLES.get(configPath).add(configurable);
            }
        }
        saveDefaultConfig(configPath);
    }

    public void registerConfigurable(NotConfigurable configurable) {
        List<String> configPaths = configurable.getConfigPaths();
        if (configPaths.isEmpty()) return;
        configPaths.forEach(path -> {
            registerConfigurable(configurable, path);
            loadConfigFile(path);
        });
        configurable.reload();
    }

    public void saveDefaultConfig(String forConfig) {
        if (forConfig == null) return;
        if (CONFIGS.containsKey(forConfig)) return;
        File configFile = new File(getDataFolder(), forConfig);
        if (configFile.exists()) return;
        if (getResource(forConfig) == null) return;
        configFile.getParentFile().mkdirs();
        saveResource(forConfig, false);
    }

    private void loadConfigFile(String configPath) {
        if (configPath == null) return;
        if (CONFIGS.containsKey(configPath)) return;
        File configFile = new File(getDataFolder(), configPath);
        if (!configFile.exists()) return;
        CONFIGS.put(configPath, YamlConfiguration.loadConfiguration(configFile));
    }

    public abstract void initNotPlugin();

    @Override
    public void onEnable() {
        NotMinecraftStuff.getInstance().initialize();

        this.translationManager = new NotTranslationManager(this);
        this.databaseManager = new NotDatabaseManager(this);

        saveDefaultConfig(CONFIG_YML);
        loadConfigFile(CONFIG_YML);
        initNotPlugin();

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
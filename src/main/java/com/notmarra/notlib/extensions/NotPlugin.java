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
import com.notmarra.notlib.utils.ChatF;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

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
    public void addListener(NotListener listener) { LISTENERS.put(listener.getId(), listener); }
    public NotListener getListener(String id) { return LISTENERS.get(id); }

    // addCommandManager("cmdgroup_id", new CommandManager(this));
    public void addCommandGroup(NotCommandGroup cmdGroup) { CMDGROUPS.put(cmdGroup.getId(), cmdGroup); }
    public NotCommandGroup getCommandGroup(String id) { return CMDGROUPS.get(id); }

    public void registerConfigurable(NotConfigurable configurable, String configPath) {
        CONFIGURABLES.computeIfAbsent(configPath, k -> new ArrayList<>());
        if (!CONFIGURABLES.get(configPath).contains(configurable)) {
            CONFIGURABLES.get(configPath).add(configurable);
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

    public File getFile(String child) { return new File(getDataFolder(), child); }
    public String getAbsPath(String child) { return (new File(getDataFolder(), child)).getAbsolutePath(); }

    public abstract void initNotPlugin();
    public abstract void onNotPluginEnable();

    public ComponentLogger log() { return getComponentLogger(); }

    @Override
    public void onEnable() {
        try {
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
            onNotPluginEnable();
        } catch (Exception e) {
            getComponentLogger().error(
                ChatF.empty()
                    .append("[CRITICAL ERROR] Disabling plugin", ChatF.C_RED)
                    .nl()
                    .appendListString(List.of(e.getStackTrace()).stream().map(x -> "    " + x.toString() + '\n').toList(), ChatF.C_ORANGE)
                    .build()
            );
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        db().close();
    }

    public FileConfiguration getSubConfig(String file) { return CONFIGS.get(file); }
    public FileConfiguration reloadConfig(String file) {
        File configFile = new File(getDataFolder(), file);
        if (!configFile.exists()) return new YamlConfiguration();
        CONFIGS.put(file, YamlConfiguration.loadConfiguration(configFile));
        if (CONFIGURABLES.containsKey(file)) {
            CONFIGURABLES.get(file).forEach(c -> c.onConfigReload(List.of(file)));
        }
        return CONFIGS.get(file);
    }
}
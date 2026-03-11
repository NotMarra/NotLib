package dev.notmarra.notlib.extensions;

import dev.notmarra.notlib.chat.Colors;
import dev.notmarra.notlib.chat.Text;
import dev.notmarra.notlib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class NotPlugin extends JavaPlugin {
    private final Map<String, Runnable> ON_PLUGIN_ENABLED_CALLBACKS = new HashMap<>();
    // <path, config>
    private final Map<String, FileConfiguration> CONFIGS = new HashMap<>();
    // <id, listener>
    private final Map<String, NotListener> LISTENERS = new HashMap<>();
    // <id, cmdGroup>
    private final Map<String, CommandGroup> CMDGROUPS = new HashMap<>();
    // <path, [configurable]>
    private final Map<String, List<Configurable>> CONFIGURABLES = new HashMap<>();

    public List<String> getConfigFilePaths() {
        return CONFIGS.keySet().stream().toList();
    }
    /* TODO
    private TranslationManager translationManager;

    public TranslationManager tm() {
        return translationManager;
    }

    public String tm(String key) {
        return tm().get(key);
    }

    public List<String> tmList(String key) {
        return tm().getList(key);
    }

    private DatabaseManager databaseManager;

    public DatabaseManager db() {
        return databaseManager;
    }

    public Database db(String dbId) {
        return databaseManager.getDatabase(dbId);
    }

    */
    private Scheduler scheduler;

    public Scheduler scheduler() {
        return scheduler;
    }

    public final String CONFIG_YML = "config.yml";

    public void addPluginEnabledCallback(String pluginId, Runnable callback) {
        ON_PLUGIN_ENABLED_CALLBACKS.put(pluginId, callback);
    }

    // addListener("listener_id", new Listener(this));
    public void addListener(NotListener notListener) {
        LISTENERS.put(notListener.getId(), notListener);
    }

    public NotListener getListener(String id) {
        return LISTENERS.get(id);
    }

    // addCommandManager("cmdgroup_id", new CommandManager(this));
    public void addCommandGroup(CommandGroup cmdGroup) {
        CMDGROUPS.put(cmdGroup.getId(), cmdGroup);
    }

    public CommandGroup getCommandGroup(String id) {
        return CMDGROUPS.get(id);
    }

    public void registerConfigurable(Configurable configurable, String configPath) {
        CONFIGURABLES.computeIfAbsent(configPath, k -> new ArrayList<>());
        if (!CONFIGURABLES.get(configPath).contains(configurable)) {
            CONFIGURABLES.get(configPath).add(configurable);
        }

        if (getResource(configPath) != null) {
            saveDefaultConfig(configPath);
        }
        loadConfigFile(configPath);
    }

    public void registerConfigurable(Configurable configurable) {
        List<String> configPaths = configurable.getConfigPaths();
        if (configPaths.isEmpty())
            return;
        configPaths.forEach(path -> {
            registerConfigurable(configurable, path);
            loadConfigFile(path);
        });
        configurable.reload();
    }

    public void saveDefaultConfig(String forConfig) {
        if (forConfig == null)
            return;
        if (CONFIGS.containsKey(forConfig))
            return;
        File configFile = new File(getDataFolder(), forConfig);
        if (configFile.exists())
            return;
        if (getResource(forConfig) == null)
            return;
        configFile.getParentFile().mkdirs();
        saveResource(forConfig, false);
    }

    private void loadConfigFile(String configPath) {
        if (configPath == null || CONFIGS.containsKey(configPath))
            return;

        File configFile = new File(getDataFolder(), configPath);
        if (!configFile.exists())
            return;

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        java.io.InputStream defaultStream = getResource(configPath);

        /* TODO
        if (defaultStream == null && translationManager != null) {

            String langFolder = "lang/";
            if (configPath.startsWith(langFolder)) {
                defaultStream = getResource(langFolder + "en.yml");
            }
        }
        */

        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new java.io.InputStreamReader(defaultStream));

            config.setDefaults(defaultConfig);
            config.options().copyDefaults(true);

            try {
                config.save(configFile);
            } catch (IOException e) {
                getComponentLogger().error("Failed to update configuration file: " + configPath, e);
            }
        }

        CONFIGS.put(configPath, config);
    }

    public File getFile(String child) {
        return new File(getDataFolder(), child);
    }

    public String getAbsPath(String child) {
        return (new File(getDataFolder(), child)).getAbsolutePath();
    }

    public abstract void initPlugin();

    public abstract void onPluginEnable();

    public abstract void onPluginDisable();

    /* TODO
    public ComponentLogger log() {
        return getComponentLogger();
    }
    */

    @Override
    public void onEnable() {
        try {
            //MinecraftStuff.getInstance().initialize();

            //this.translationManager = new TranslationManager(this);
            //this.databaseManager = new DatabaseManager(this);
            this.scheduler = new Scheduler(this);

            saveDefaultConfig(CONFIG_YML);
            loadConfigFile(CONFIG_YML);
            //tm().discoverAndRegisterLocalLangs();
            initPlugin();

            for (String pluginId : ON_PLUGIN_ENABLED_CALLBACKS.keySet()) {
                if (Bukkit.getPluginManager().isPluginEnabled(pluginId)) {
                    ON_PLUGIN_ENABLED_CALLBACKS.get(pluginId).run();
                }
            }

            LISTENERS.values().forEach(l -> l.register());
            CMDGROUPS.values().forEach(c -> c.register());
            onPluginEnable();
        } catch (Exception e) {
            getComponentLogger().error(
                    Text.empty()
                            .append("[CRITICAL ERROR] Disabling plugin", Colors.RED.get())
                            .nl()
                            .appendListString(
                                    List.of(e.getStackTrace()).stream().map(x -> "    " + x.toString() + '\n').toList(),
                                    Colors.ORANGE.get())
                            .build());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        onPluginDisable();
        //db().close();
    }

    public FileConfiguration getSubConfig(String file) {
        return CONFIGS.get(file);
    }

    public FileConfiguration reloadConfig(String file) {
        File configFile = new File(getDataFolder(), file);
        if (!configFile.exists())
            return new YamlConfiguration();
        CONFIGS.put(file, YamlConfiguration.loadConfiguration(configFile));
        if (CONFIGURABLES.containsKey(file)) {
            CONFIGURABLES.get(file).forEach(c -> c.onConfigReload(List.of(file)));
        }
        return CONFIGS.get(file);
    }

    public List<String> listResources(String folderPath) {
        List<String> resources = new ArrayList<>();

        try {
            // Get the JAR file of your plugin
            JarFile jarFile = new JarFile(this.getClass().getProtectionDomain()
                    .getCodeSource().getLocation().getPath());

            // Get all entries in the JAR
            Enumeration<JarEntry> entries = jarFile.entries();

            // Loop through all entries
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                // Check if this entry is in the specified folder and is not a directory
                if (name.startsWith(folderPath) && !name.endsWith("/")) {
                    resources.add(name);
                }
            }

            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return resources;
    }
}

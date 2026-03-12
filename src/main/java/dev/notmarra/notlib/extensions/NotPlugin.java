package dev.notmarra.notlib.extensions;

import dev.notmarra.notlib.file.ConfigFileManager;
import dev.notmarra.notlib.file.ConfigOptions;
import dev.notmarra.notlib.file.ManagedConfig;
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
    private final Map<String, Runnable>         ON_PLUGIN_ENABLED_CALLBACKS = new HashMap<>();
    private final Map<String, NotListener>      LISTENERS   = new HashMap<>();
    private final Map<String, CommandGroup>     CMDGROUPS   = new HashMap<>();
    // <configPath, [configurable]>  – kept for onConfigReload callbacks
    private final Map<String, List<Configurable>> CONFIGURABLES = new HashMap<>();

    // -----------------------------------------------------------------------
    // ConfigFileManager – single source of truth for all config files
    // -----------------------------------------------------------------------

    private ConfigFileManager cfm;

    /**
     * Returns the plugin's {@link ConfigFileManager}.
     * Available after {@link #onEnable()} initialises it; do not call from a static context.
     */
    public ConfigFileManager getCfm() { return cfm; }

    // -----------------------------------------------------------------------
    // Legacy shims – keep existing API working unchanged
    // -----------------------------------------------------------------------

    /**
     * Returns the relative paths of all config files currently managed by the CFM.
     * Drop-in replacement for the old {@code CONFIGS.keySet()} approach.
     */
    public List<String> getConfigFilePaths() {
        return cfm.getAll().keySet().stream().toList();
    }

    // -----------------------------------------------------------------------
    // Scheduler & constants
    // -----------------------------------------------------------------------

    private Scheduler scheduler;

    public Scheduler scheduler() { return scheduler; }

    public final String CONFIG_YML = "config.yml";

    // -----------------------------------------------------------------------
    // Listener / CommandGroup registration (unchanged)
    // -----------------------------------------------------------------------

    public void addPluginEnabledCallback(String pluginId, Runnable callback) {
        ON_PLUGIN_ENABLED_CALLBACKS.put(pluginId, callback);
    }

    public void addListener(NotListener notListener) {
        LISTENERS.put(notListener.getId(), notListener);
    }

    public NotListener getListener(String id) { return LISTENERS.get(id); }

    public void addCommandGroup(CommandGroup cmdGroup) {
        CMDGROUPS.put(cmdGroup.getId(), cmdGroup);
    }

    public CommandGroup getCommandGroup(String id) { return CMDGROUPS.get(id); }

    // -----------------------------------------------------------------------
    // Configurable registration
    // -----------------------------------------------------------------------

    /**
     * Registers a {@link Configurable} for a specific config path.
     *
     * <ul>
     *   <li>If the path is not yet tracked by the CFM it is registered and loaded.</li>
     *   <li>The configurable is added to the callback list for that path so it receives
     *       {@link Configurable#onConfigReload} on every reload.</li>
     * </ul>
     */
    public void registerConfigurable(Configurable configurable, String configPath) {
        // Register in the callback index
        CONFIGURABLES.computeIfAbsent(configPath, k -> new ArrayList<>())
                .add(configurable);

        // Ensure CFM knows about this file (idempotent)
        if (!cfm.isRegistered(configPath)) {
            cfm.register(configPath);
            cfm.load(configPath);
        }
    }

    /**
     * Registers a {@link Configurable} for all paths returned by
     * {@link Configurable#getConfigPaths()}, then triggers an initial reload.
     */
    public void registerConfigurable(Configurable configurable) {
        List<String> paths = configurable.getConfigPaths();
        if (paths.isEmpty()) return;
        paths.forEach(path -> registerConfigurable(configurable, path));
        configurable.reload();
    }

    // -----------------------------------------------------------------------
    // Config access – delegates to CFM
    // -----------------------------------------------------------------------

    /**
     * Returns the {@link FileConfiguration} for the given relative path.
     * Forwards to the underlying {@link ManagedConfig} in the CFM.
     *
     * <p>Returns an empty {@link YamlConfiguration} if the file is not registered,
     * preserving the old null-safe behaviour callers may rely on.
     */
    public FileConfiguration getSubConfig(String file) {
        if (!cfm.isRegistered(file)) return new YamlConfiguration();
        return cfm.get(file).yaml();
    }

    /**
     * Reloads a config file from disk, updates the CFM cache and notifies all
     * registered {@link Configurable} instances for that path.
     *
     * <p>Drop-in replacement for the old {@code reloadConfig(String)} method.
     */
    public FileConfiguration reloadConfig(String file) {
        if (!cfm.isRegistered(file)) return new YamlConfiguration();

        cfm.reload(file);   // re-reads disk + runs auto-update

        // Notify all configurables registered for this path
        List<Configurable> subscribers = CONFIGURABLES.getOrDefault(file, List.of());
        subscribers.forEach(c -> c.onConfigReload(List.of(file)));

        return cfm.get(file).yaml();
    }

    // -----------------------------------------------------------------------
    // Directory watching – new feature, integrates with CFM
    // -----------------------------------------------------------------------

    /**
     * Watches a directory for user-created YAML files (e.g. language packs, arena configs).
     *
     * <p>Delegates to {@link ConfigFileManager#watchDirectory}. Call this from
     * {@link #initPlugin()} before {@code loadAll()} / {@code onPluginEnable()}.
     *
     * <pre>{@code
     * // In initPlugin():
     * watchUserDirectory("languages", ConfigOptions.builder()
     *         .autoUpdate(false)
     *         .seedFile("languages/en_US.yml")
     *         .build());
     * }</pre>
     *
     * @param dirPath relative path inside the plugin data folder
     * @param options options applied to every file found in the directory
     */
    public void watchUserDirectory(String dirPath, ConfigOptions options) {
        cfm.watchDirectory(dirPath, options);
    }

    /** @see #watchUserDirectory(String, ConfigOptions) */
    public void watchUserDirectory(String dirPath) {
        cfm.watchDirectory(dirPath);
    }

    // -----------------------------------------------------------------------
    // Legacy config helpers (saveDefaultConfig / loadConfigFile kept for
    // back-compat; internally they now go through CFM)
    // -----------------------------------------------------------------------

    /**
     * Copies the resource to disk if it does not yet exist, then ensures the CFM
     * has it registered. Equivalent to the old {@code saveDefaultConfig(String)}.
     */
    public void saveDefaultConfig(String forConfig) {
        if (forConfig == null) return;

        File configFile = new File(getDataFolder(), forConfig);
        if (!configFile.exists() && getResource(forConfig) != null) {
            configFile.getParentFile().mkdirs();
            saveResource(forConfig, false);
        }

        // Register with CFM (noop if already registered)
        if (!cfm.isRegistered(forConfig)) {
            cfm.register(forConfig);
        }
    }

    /**
     * Loads a config file through the CFM.
     * No-op if the file does not exist on disk (preserves old behaviour).
     *
     * @deprecated Use {@link ConfigFileManager#load(String)} directly via {@link #getCfm()}.
     */
    @Deprecated
    private void loadConfigFile(String configPath) {
        if (configPath == null) return;
        File f = new File(getDataFolder(), configPath);
        if (!f.exists()) return;

        if (!cfm.isRegistered(configPath)) {
            cfm.register(configPath);
        }
        cfm.load(configPath);
    }

    // -----------------------------------------------------------------------
    // Plugin lifecycle
    // -----------------------------------------------------------------------

    public abstract void initPlugin();
    public abstract void onPluginEnable();
    public abstract void onPluginDisable();

    @Override
    public void onEnable() {
        try {
            this.scheduler = new Scheduler(this);

            // Initialise the CFM early so initPlugin() can call register/watchDirectory
            this.cfm = new ConfigFileManager(this);

            // Ensure the main config is always available
            saveDefaultConfig(CONFIG_YML);
            cfm.load(CONFIG_YML);

            initPlugin();

            // Load everything the plugin registered during initPlugin()
            cfm.loadAll();

            // Fire plugin-enabled callbacks
            ON_PLUGIN_ENABLED_CALLBACKS.forEach((pluginId, cb) -> {
                if (Bukkit.getPluginManager().isPluginEnabled(pluginId)) cb.run();
            });

            LISTENERS.values().forEach(NotListener::register);
            CMDGROUPS.values().forEach(CommandGroup::register);

            onPluginEnable();
        } catch (Exception e) {
            getComponentLogger().error(
                    Text.empty()
                            .append("[CRITICAL ERROR] Disabling plugin", Colors.RED.get())
                            .nl()
                            .appendListString(
                                    List.of(e.getStackTrace()).stream()
                                            .map(x -> "    " + x + '\n').toList(),
                                    Colors.ORANGE.get())
                            .build());
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        onPluginDisable();
    }

    // -----------------------------------------------------------------------
    // Utilities
    // -----------------------------------------------------------------------

    public File getFile(String child)        { return new File(getDataFolder(), child); }
    public String getAbsPath(String child)   { return new File(getDataFolder(), child).getAbsolutePath(); }

    /**
     * Lists all resources inside the given folder path in the plugin jar.
     * Unchanged from the original implementation.
     */
    public List<String> listResources(String folderPath) {
        List<String> resources = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(
                    this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.startsWith(folderPath) && !name.endsWith("/")) resources.add(name);
            }
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resources;
    }

    /**
     * Convenience method: watches a directory whose contents are discovered via
     * {@link #listResources(String)} and seeds it from jar resources on first run.
     *
     * <p>Useful for bundling multiple default files (e.g. all built-in language files)
     * while still allowing users to add their own:
     *
     * <pre>{@code
     * // Copies languages/en_US.yml and languages/cs_CZ.yml from the jar,
     * // then also loads any extra *.yml files users drop into the folder.
     * watchResourceDirectory("languages");
     * }</pre>
     *
     * @param folderPath folder path as used in the jar (e.g. {@code "languages"})
     */
    public void watchResourceDirectory(String folderPath) {
        // Copy every bundled file in the folder to disk on first run
        listResources(folderPath + "/").forEach(resourcePath -> {
            File dest = new File(getDataFolder(), resourcePath);
            if (!dest.exists() && getResource(resourcePath) != null) {
                dest.getParentFile().mkdirs();
                saveResource(resourcePath, false);
            }
        });

        // Then watch the directory so user-added files are also picked up
        cfm.watchDirectory(folderPath, ConfigOptions.builder()
                .autoUpdate(false)
                .copyFromResources(false)
                .build());
    }
}
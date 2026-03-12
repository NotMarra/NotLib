package dev.notmarra.notlib.file;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

/**
 * Central manager for configuration files in PaperMC plugins.
 *
 * <p>Features:
 * <ul>
 *   <li>YAML support with comment preservation</li>
 *   <li>Auto-update: missing keys are added from the resource template while user values are kept</li>
 *   <li>Multi-file management with a fluent registration API</li>
 *   <li>Reload via API or a command handler</li>
 *   <li>User-defined file directories: scan and load YAML files created by users at runtime</li>
 * </ul>
 *
 * <h3>Basic usage:</h3>
 * <pre>{@code
 * ConfigFileManager cfm = new ConfigFileManager(plugin);
 * cfm.register("config.yml")
 *    .register("messages.yml")
 *    .loadAll();
 *
 * String prefix = cfm.get("config.yml").getString("prefix", "[Plugin]");
 * cfm.reload("config.yml");
 * cfm.reloadAll();
 * }</pre>
 *
 * <h3>User directories (e.g. custom language files):</h3>
 * <pre>{@code
 * cfm.watchDirectory("languages",
 *     ConfigOptions.builder().autoUpdate(false).build())
 *    .loadAll();
 *
 * // After a user drops "languages/de_DE.yml" on the server:
 * cfm.reloadDirectory("languages");
 * Collection<ManagedConfig> langs = cfm.getDirectory("languages");
 * }</pre>
 */
public class ConfigFileManager {

    private final JavaPlugin plugin;

    /** Explicitly registered files, keyed by their relative path (e.g. "config.yml"). */
    private final Map<String, ManagedConfig> configs = new LinkedHashMap<>();

    /** Watched user directories, keyed by the directory path relative to the plugin data folder. */
    private final Map<String, UserDirectory> directories = new LinkedHashMap<>();

    public ConfigFileManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // -----------------------------------------------------------------------
    // Registration – explicit files
    // -----------------------------------------------------------------------

    /**
     * Registers a file with default options.
     * If the file exists as a plugin resource it is used as the auto-update template.
     *
     * @param fileName file path relative to the plugin data folder (e.g. {@code "config.yml"},
     *                 {@code "data/rewards.yml"})
     * @return {@code this} for chaining
     */
    public ConfigFileManager register(String fileName) {
        return register(fileName, ConfigOptions.defaults());
    }

    /**
     * Registers a file with custom options.
     *
     * @param fileName file path relative to the plugin data folder
     * @param options  per-file configuration options
     * @return {@code this} for chaining
     */
    public ConfigFileManager register(String fileName, ConfigOptions options) {
        if (configs.containsKey(fileName)) {
            plugin.getLogger().warning("[ConfigFileManager] File '" + fileName + "' is already registered.");
            return this;
        }

        ConfigFormat format  = ConfigFormat.detect(fileName);
        File         file    = new File(plugin.getDataFolder(), fileName);
        configs.put(fileName, new ManagedConfig(plugin, fileName, file, format, options));
        return this;
    }

    // -----------------------------------------------------------------------
    // Registration – user directories
    // -----------------------------------------------------------------------

    /**
     * Registers a directory whose YAML files are created and maintained by users at runtime
     * (e.g. custom language files, per-arena configurations, reward packs).
     *
     * <p>On {@link #loadAll()} or {@link #reloadDirectory(String)} the manager scans the
     * directory for {@code *.yml} / {@code *.yaml} files and loads every one it finds.
     * Files added to the directory after the initial load are picked up on the next reload.
     *
     * <p>The directory is created automatically if it does not exist.
     * An optional seed file from plugin resources can be copied there on first run to give
     * users an example to copy and customise (see {@link ConfigOptions.Builder#seedFile}).
     *
     * @param dirPath relative path of the directory inside the plugin data folder
     *                (e.g. {@code "languages"}, {@code "arenas"})
     * @param options options applied to every file found inside the directory
     * @return {@code this} for chaining
     */
    public ConfigFileManager watchDirectory(String dirPath, ConfigOptions options) {
        if (directories.containsKey(dirPath)) {
            plugin.getLogger().warning("[ConfigFileManager] Directory '" + dirPath + "' is already watched.");
            return this;
        }
        File dir = new File(plugin.getDataFolder(), dirPath);
        directories.put(dirPath, new UserDirectory(plugin, dirPath, dir, options));
        return this;
    }

    /**
     * Registers a directory with default options.
     *
     * @param dirPath relative path of the directory inside the plugin data folder
     * @return {@code this} for chaining
     * @see #watchDirectory(String, ConfigOptions)
     */
    public ConfigFileManager watchDirectory(String dirPath) {
        return watchDirectory(dirPath, ConfigOptions.builder().autoUpdate(false).build());
    }

    // -----------------------------------------------------------------------
    // Loading
    // -----------------------------------------------------------------------

    /**
     * Loads (or creates) all registered files and scans all watched directories.
     */
    public void loadAll() {
        for (ManagedConfig cfg : configs.values())    load(cfg);
        for (UserDirectory  dir : directories.values()) loadDirectory(dir);
    }

    /**
     * Loads a single registered file.
     *
     * @param fileName registered file name
     * @throws IllegalArgumentException if the file is not registered
     */
    public void load(String fileName) {
        load(getOrThrow(fileName));
    }

    private void load(ManagedConfig cfg) {
        try {
            cfg.load();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ConfigFileManager] Failed to load '" + cfg.getFileName() + "'", e);
        }
    }

    // -----------------------------------------------------------------------
    // Reload
    // -----------------------------------------------------------------------

    /**
     * Reloads all registered files and re-scans all watched directories from disk.
     */
    public void reloadAll() {
        for (ManagedConfig cfg : configs.values())    reload(cfg);
        for (UserDirectory  dir : directories.values()) reloadDirectory(dir.getDirPath());
        plugin.getLogger().info("[ConfigFileManager] All configurations reloaded.");
    }

    /**
     * Reloads a single registered file from disk.
     *
     * @param fileName registered file name
     * @throws IllegalArgumentException if the file is not registered
     */
    public void reload(String fileName) {
        reload(getOrThrow(fileName));
    }

    private void reload(ManagedConfig cfg) {
        try {
            cfg.reload();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ConfigFileManager] Failed to reload '" + cfg.getFileName() + "'", e);
        }
    }

    /**
     * Re-scans a watched directory and reloads all YAML files found inside it.
     * New files added since the last scan are picked up automatically.
     *
     * @param dirPath directory path as passed to {@link #watchDirectory(String, ConfigOptions)}
     * @throws IllegalArgumentException if the directory is not watched
     */
    public void reloadDirectory(String dirPath) {
        UserDirectory dir = directories.get(dirPath);
        if (dir == null) throw new IllegalArgumentException(
                "[ConfigFileManager] Directory '" + dirPath + "' is not watched. Call watchDirectory() first.");
        reloadDirectory(dir);
    }

    private void loadDirectory(UserDirectory dir) {
        try { dir.load(); }
        catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ConfigFileManager] Failed to load directory '" + dir.getDirPath() + "'", e);
        }
    }

    private void reloadDirectory(UserDirectory dir) {
        try { dir.reload(); }
        catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ConfigFileManager] Failed to reload directory '" + dir.getDirPath() + "'", e);
        }
    }

    // -----------------------------------------------------------------------
    // Saving
    // -----------------------------------------------------------------------

    /**
     * Saves a single registered file to disk.
     *
     * <p><strong>Note:</strong> saving via Bukkit's {@code YamlConfiguration} strips comments.
     * Prefer editing files programmatically only when necessary.
     *
     * @param fileName registered file name
     */
    public void save(String fileName) {
        ManagedConfig cfg = getOrThrow(fileName);
        try {
            cfg.save();
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE,
                    "[ConfigFileManager] Failed to save '" + cfg.getFileName() + "'", e);
        }
    }

    /**
     * Saves all registered files to disk.
     */
    public void saveAll() {
        for (ManagedConfig cfg : configs.values()) {
            try { cfg.save(); }
            catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE,
                        "[ConfigFileManager] Failed to save '" + cfg.getFileName() + "'", e);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Access – explicit files
    // -----------------------------------------------------------------------

    /**
     * Returns the {@link ManagedConfig} for a registered file.
     *
     * @param fileName registered file name
     * @return the managed config
     * @throws IllegalArgumentException if the file is not registered
     */
    public ManagedConfig get(String fileName) {
        return getOrThrow(fileName);
    }

    /**
     * Returns {@code true} if the given file is registered.
     */
    public boolean isRegistered(String fileName) {
        return configs.containsKey(fileName);
    }

    /**
     * Returns an unmodifiable view of all explicitly registered configurations.
     */
    public Map<String, ManagedConfig> getAll() {
        return Collections.unmodifiableMap(configs);
    }

    /**
     * Unregisters a file from this manager without deleting it from disk.
     *
     * @param fileName registered file name
     */
    public void unregister(String fileName) {
        configs.remove(fileName);
    }

    // -----------------------------------------------------------------------
    // Access – user directories
    // -----------------------------------------------------------------------

    /**
     * Returns all {@link ManagedConfig} instances currently loaded from a watched directory.
     *
     * @param dirPath directory path as passed to {@link #watchDirectory}
     * @return unmodifiable collection of loaded configs (may be empty if the directory is empty)
     * @throws IllegalArgumentException if the directory is not watched
     */
    public Collection<ManagedConfig> getDirectory(String dirPath) {
        UserDirectory dir = directories.get(dirPath);
        if (dir == null) throw new IllegalArgumentException(
                "[ConfigFileManager] Directory '" + dirPath + "' is not watched. Call watchDirectory() first.");
        return dir.getConfigs();
    }

    /**
     * Returns a specific file from a watched directory, looked up by file name.
     *
     * @param dirPath  directory path as passed to {@link #watchDirectory}
     * @param fileName file name relative to the directory (e.g. {@code "en_US.yml"})
     * @return the config, or {@link Optional#empty()} if the file was not found in the directory
     */
    public Optional<ManagedConfig> getFromDirectory(String dirPath, String fileName) {
        UserDirectory dir = directories.get(dirPath);
        if (dir == null) throw new IllegalArgumentException(
                "[ConfigFileManager] Directory '" + dirPath + "' is not watched. Call watchDirectory() first.");
        return dir.getConfig(fileName);
    }

    /**
     * Returns {@code true} if the given directory path is being watched.
     */
    public boolean isWatched(String dirPath) {
        return directories.containsKey(dirPath);
    }

    // -----------------------------------------------------------------------
    // Internal
    // -----------------------------------------------------------------------

    private ManagedConfig getOrThrow(String fileName) {
        ManagedConfig cfg = configs.get(fileName);
        if (cfg == null) throw new IllegalArgumentException(
                "[ConfigFileManager] File '" + fileName + "' is not registered. Call register() first.");
        return cfg;
    }
}
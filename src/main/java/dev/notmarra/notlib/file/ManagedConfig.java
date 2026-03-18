package dev.notmarra.notlib.file;

import dev.notmarra.notlib.file.updater.YamlConfigUpdater;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.logging.Level;

/**
 * Represents a single managed configuration file.
 *
 * <p>Provides:
 * <ul>
 *   <li>Value access via Bukkit's {@link FileConfiguration} API</li>
 *   <li>Auto-update: missing keys added from the resource template, user values preserved</li>
 *   <li>Comment and block scalar preservation during update</li>
 *   <li>Reload from disk</li>
 * </ul>
 *
 * <p>Instances are created exclusively by {@link ConfigFileManager} and {@link UserDirectory}.
 */
public class ManagedConfig {

    private final JavaPlugin    plugin;
    private final String        fileName;
    private final File          file;
    private final ConfigFormat  format;
    private final ConfigOptions options;

    private FileConfiguration yamlConfig;

    // -----------------------------------------------------------------------
    // Constructor (package-private – created by ConfigFileManager / UserDirectory)
    // -----------------------------------------------------------------------

    ManagedConfig(JavaPlugin plugin, String fileName, File file, ConfigFormat format, ConfigOptions options) {
        this.plugin   = plugin;
        this.fileName = fileName;
        this.file     = file;
        this.format   = format;
        this.options  = options;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    /**
     * Loads the file from disk. If the file does not exist and
     * {@link ConfigOptions#isCopyFromResources()} is {@code true} it is copied
     * from plugin resources first. Then, if {@link ConfigOptions#isAutoUpdate()}
     * is {@code true}, missing keys are merged in from the resource template.
     */
    void load() throws IOException {
        ensureFileExists();
        readFromDisk();
        if (options.isAutoUpdate()) runAutoUpdate();
    }

    /**
     * Reloads the file from disk and re-runs auto-update if enabled.
     * Does not re-copy from resources.
     */
    void reload() throws IOException {
        readFromDisk();
        if (options.isAutoUpdate()) runAutoUpdate();
        plugin.getLogger().info("[ConfigFileManager] Reloaded '" + fileName + "'.");
    }

    /**
     * Saves the current in-memory state to disk via Bukkit's YAML serialiser.
     *
     * <p><strong>Warning:</strong> Bukkit's {@link YamlConfiguration#save} discards all
     * comments. Only call this when programmatically mutating values via {@link #set}.
     */
    void save() throws IOException {
        if (yamlConfig != null) yamlConfig.save(file);
    }

    // -----------------------------------------------------------------------
    // Internal – file initialisation
    // -----------------------------------------------------------------------

    private void ensureFileExists() throws IOException {
        if (file.exists()) return;

        if (options.isCopyFromResources()) {
            copyFromResources(fileName);
        } else {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
    }

    /**
     * Copies a resource from the plugin jar to {@link #file}.
     *
     * @param resourcePath path inside the jar (e.g. {@code "config.yml"})
     */
    void copyFromResources(String resourcePath) throws IOException {
        InputStream resource = plugin.getResource(resourcePath);
        if (resource == null) {
            file.getParentFile().mkdirs();
            file.createNewFile();
            plugin.getLogger().warning("[ConfigFileManager] Resource '" + resourcePath +
                    "' not found in plugin jar. Created empty file at '" + fileName + "'.");
            return;
        }

        file.getParentFile().mkdirs();
        try (InputStream in = resource; OutputStream out = new FileOutputStream(file)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
        plugin.getLogger().info("[ConfigFileManager] Created '" + fileName + "' from resources.");
    }

    // -----------------------------------------------------------------------
    // Internal – read & auto-update
    // -----------------------------------------------------------------------

    private void readFromDisk() {
        yamlConfig = YamlConfiguration.loadConfiguration(file);
    }

    private void runAutoUpdate() {
        InputStream resource = plugin.getResource(fileName);
        if (resource == null) return; // No template in jar – nothing to merge

        try {
            new YamlConfigUpdater(plugin, fileName, file, options).update(resource);
            readFromDisk(); // Refresh so yamlConfig reflects newly added keys
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                    "[ConfigFileManager] Auto-update failed for '" + fileName + "'", e);
        } finally {
            try { resource.close(); } catch (IOException ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // Value access
    // -----------------------------------------------------------------------

    /**
     * Returns the underlying Bukkit {@link FileConfiguration} for full API access.
     *
     * @throws IllegalStateException if the file has not been loaded yet
     */
    public FileConfiguration yaml() {
        if (yamlConfig == null) throw new IllegalStateException(
                "[ConfigFileManager] File '" + fileName + "' has not been loaded yet. Call loadAll() first.");
        return yamlConfig;
    }

    /** @see FileConfiguration#getString(String, String) */
    public String getString(String path, String fallback)    { return yaml().getString(path, fallback); }

    /** @see FileConfiguration#getInt(String, int) */
    public int getInt(String path, int fallback)             { return yaml().getInt(path, fallback); }

    /** @see FileConfiguration#getBoolean(String, boolean) */
    public boolean getBoolean(String path, boolean fallback) { return yaml().getBoolean(path, fallback); }

    /** @see FileConfiguration#getDouble(String, double) */
    public double getDouble(String path, double fallback)    { return yaml().getDouble(path, fallback); }

    /**
     * Sets a value in the in-memory configuration.
     * Call {@link ConfigFileManager#save(String)} to persist the change to disk.
     */
    public void set(String path, Object value) { yaml().set(path, value); }

    // -----------------------------------------------------------------------
    // Metadata
    // -----------------------------------------------------------------------

    /** File name / registration key (e.g. {@code "config.yml"} or {@code "languages/de_DE.yml"}). */
    public String getFileName()       { return fileName; }

    /** The {@link File} handle pointing to the file on disk. */
    public File getFile()             { return file; }

    /** Detected file format. */
    public ConfigFormat getFormat()   { return format; }

    /** Options controlling how this file is managed. */
    public ConfigOptions getOptions() { return options; }
}
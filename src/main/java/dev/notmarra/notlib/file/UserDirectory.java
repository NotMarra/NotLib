package dev.notmarra.notlib.file;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Manages a directory whose YAML files are created and maintained by users at runtime.
 *
 * <p>Typical use cases:
 * <ul>
 *   <li><strong>Language files</strong> – users drop {@code en_US.yml}, {@code de_DE.yml}, etc.
 *       into a {@code languages/} directory.</li>
 *   <li><strong>Arena configs</strong> – each arena lives in its own YAML file inside
 *       {@code arenas/}.</li>
 *   <li><strong>Reward packs</strong> – community-made packs placed in {@code rewards/}.</li>
 * </ul>
 *
 * <h3>Behaviour:</h3>
 * <ol>
 *   <li>The directory is created on first load if it does not exist.</li>
 *   <li>If a {@link ConfigOptions#getSeedFile() seed file} is configured and the directory
 *       is empty, that resource is copied in as a starting template.</li>
 *   <li>Every {@code *.yml} / {@code *.yaml} file in the directory (non-recursive) is
 *       loaded as a {@link ManagedConfig} with the options supplied to the directory.</li>
 *   <li>On reload, previously loaded configs are refreshed and any newly added files are
 *       picked up automatically.</li>
 * </ol>
 *
 * <p>Instances are created exclusively by {@link dev.library.config.ConfigFileManager}.
 */
public class UserDirectory {

    private final JavaPlugin    plugin;
    private final String        dirPath;   // relative to plugin data folder
    private final File          dir;
    private final ConfigOptions options;

    /** Currently loaded configs, keyed by their file name (e.g. "en_US.yml"). */
    private final Map<String, ManagedConfig> configs = new LinkedHashMap<>();

    // -----------------------------------------------------------------------
    // Constructor (package-private)
    // -----------------------------------------------------------------------

    public UserDirectory(JavaPlugin plugin, String dirPath, File dir, ConfigOptions options) {
        this.plugin  = plugin;
        this.dirPath = dirPath;
        this.dir     = dir;
        this.options = options;
    }

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------

    /**
     * Creates the directory if needed, copies the seed file on first run,
     * then loads all YAML files found inside.
     */
    public void load() throws IOException {
        ensureDirectoryExists();
        copySeedFileIfEmpty();
        scanAndLoad();
        plugin.getLogger().info("[ConfigFileManager] Loaded " + configs.size() +
                " file(s) from directory '" + dirPath + "'.");
    }

    /**
     * Re-scans the directory and reloads all YAML files.
     * Files added since the last scan are picked up automatically.
     * Files that no longer exist on disk are removed from the cache.
     */
    public void reload() throws IOException {
        ensureDirectoryExists();
        scanAndLoad();
        plugin.getLogger().info("[ConfigFileManager] Reloaded " + configs.size() +
                " file(s) from directory '" + dirPath + "'.");
    }

    // -----------------------------------------------------------------------
    // Internal – directory setup
    // -----------------------------------------------------------------------

    private void ensureDirectoryExists() {
        if (!dir.exists()) {
            dir.mkdirs();
            plugin.getLogger().info("[ConfigFileManager] Created directory '" + dirPath + "'.");
        }
    }

    /**
     * Copies the configured seed file from plugin resources into the directory
     * if the directory is currently empty. This gives users a starting template.
     */
    private void copySeedFileIfEmpty() {
        String seedResource = options.getSeedFile();
        if (seedResource == null) return;

        File[] existing = dir.listFiles(f -> isYaml(f.getName()));
        if (existing != null && existing.length > 0) return; // Already has files – skip

        InputStream resource = plugin.getResource(seedResource);
        if (resource == null) {
            plugin.getLogger().warning("[ConfigFileManager] Seed file resource '" + seedResource +
                    "' not found in plugin jar. Skipping seed copy for '" + dirPath + "'.");
            return;
        }

        // Determine destination file name (last segment of the resource path)
        String destName = seedResource.contains("/")
                ? seedResource.substring(seedResource.lastIndexOf('/') + 1)
                : seedResource;
        File dest = new File(dir, destName);

        try (InputStream in = resource; OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
            plugin.getLogger().info("[ConfigFileManager] Copied seed file '" + destName +
                    "' into '" + dirPath + "'.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING,
                    "[ConfigFileManager] Failed to copy seed file into '" + dirPath + "'", e);
        }
    }

    // -----------------------------------------------------------------------
    // Internal – scanning
    // -----------------------------------------------------------------------

    /**
     * Scans the directory for YAML files (non-recursive), loads new ones,
     * reloads existing ones, and removes stale entries for deleted files.
     */
    private void scanAndLoad() {
        File[] found = dir.listFiles(f -> f.isFile() && isYaml(f.getName()));
        if (found == null) found = new File[0];

        // Build a set of currently present file names
        Set<String> present = Arrays.stream(found)
                .map(File::getName)
                .collect(Collectors.toSet());

        // Remove configs for files that were deleted
        configs.keySet().removeIf(name -> !present.contains(name));

        // Load new files / reload existing ones
        for (File file : found) {
            String name = file.getName();
            // Relative path for logging and registration (e.g. "languages/en_US.yml")
            String relativePath = dirPath + "/" + name;

            ManagedConfig existing = configs.get(name);
            if (existing == null) {
                // New file – create and load
                ManagedConfig cfg = new ManagedConfig(
                        plugin, relativePath, file, ConfigFormat.detect(name), options);
                try {
                    cfg.load();
                    configs.put(name, cfg);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING,
                            "[ConfigFileManager] Failed to load user file '" + relativePath + "'", e);
                }
            } else {
                // Existing – reload
                try {
                    existing.reload();
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING,
                            "[ConfigFileManager] Failed to reload user file '" + relativePath + "'", e);
                }
            }
        }
    }

    private static boolean isYaml(String name) {
        String lower = name.toLowerCase();
        return lower.endsWith(".yml") || lower.endsWith(".yaml");
    }

    // -----------------------------------------------------------------------
    // Access
    // -----------------------------------------------------------------------

    /**
     * Returns all currently loaded configs from this directory.
     *
     * @return unmodifiable collection; may be empty if the directory contains no YAML files
     */
    public Collection<ManagedConfig> getConfigs() {
        return Collections.unmodifiableCollection(configs.values());
    }

    /**
     * Returns a config by its file name (e.g. {@code "en_US.yml"}).
     *
     * @param fileName file name relative to the directory
     * @return the config wrapped in an {@link Optional}, or empty if not found
     */
    public Optional<ManagedConfig> getConfig(String fileName) {
        return Optional.ofNullable(configs.get(fileName));
    }

    /** Returns the directory path relative to the plugin data folder. */
    public String getDirPath() { return dirPath; }

    /** Returns the {@link File} handle for the directory on disk. */
    public File getDir()       { return dir; }
}
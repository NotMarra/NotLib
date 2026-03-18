package dev.notmarra.notlib.language;

import dev.notmarra.notlib.file.ConfigFileManager;
import dev.notmarra.notlib.file.ConfigOptions;
import dev.notmarra.notlib.file.ManagedConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Manages localised messages loaded from YAML files in a watched directory.
 *
 * <h3>Directory layout</h3>
 * <pre>
 * plugins/MyPlugin/
 *   languages/
 *     en_US.yml   ← default / fallback
 *     cs_CZ.yml
 * </pre>
 *
 * <h3>config.yml integration</h3>
 * <pre>{@code
 * language:
 *   default: "en_US"
 * }</pre>
 *
 * <h3>YAML message file format</h3>
 * <pre>{@code
 * prefix: "<gray>[<aqua>MyPlugin</aqua>]</gray> "
 *
 * player:
 *   join: "%prefix%<green>%player% joined!"
 *   quit: "%prefix%<red>%player% left."
 * }</pre>
 *
 * <h3>Setup inside NotPlugin</h3>
 * <pre>{@code
 * lang = languageManager()
 *         .defaultLocale("en_US")
 *         .seedFile("languages/en_US.yml")
 *         .build();
 *
 * lang.get("player.join").withPlayer(player).sendTo(player);
 * }</pre>
 */
public class LanguageManager {

    private static final Logger LOGGER = Logger.getLogger(LanguageManager.class.getName());

    private static final String PREFIX_KEY    = "prefix";
    private static final String PREFIX_HOLDER = "%prefix%";
    private static final String CFG_DEFAULT   = "language.default";

    private final JavaPlugin plugin;
    private final ConfigFileManager cfm;
    private final String directory;
    private final String configPath;
    private final String builderDefaultLocale;

    /** Active locale — read from config on every reload. */
    private String defaultLocale;

    /** locale code → in-memory FileConfiguration. Rebuilt on every reload. */
    private final Map<String, FileConfiguration> localeCache = new ConcurrentHashMap<>();

    /** locale code → resolved prefix string. Rebuilt on every reload. */
    private final Map<String, String> prefixCache = new ConcurrentHashMap<>();

    // ── CONSTRUCTOR ──────────────────────────────────────────────────────────

    private LanguageManager(Builder b) {
        this.plugin               = b.plugin;
        this.directory            = b.directory;
        this.configPath           = b.configPath;
        this.builderDefaultLocale = normalise(b.defaultLocale);
        this.defaultLocale        = this.builderDefaultLocale;

        this.cfm = b.cfm != null ? b.cfm : new ConfigFileManager(plugin);

        cfm.watchDirectory(directory, ConfigOptions.builder()
                .autoUpdate(false)
                .copyFromResources(false)
                .seedFile(b.seedFile)
                .build());
        cfm.loadAll();

        readConfig();
        rebuildLocaleCache();

        if (!localeCache.containsKey(defaultLocale)) {
            LOGGER.warning("[LanguageManager] Default locale '" + defaultLocale
                    + "' not found in '" + directory + "/'. Messages will return their keys.");
        }
    }

    // ── PUBLIC API ───────────────────────────────────────────────────────────

    /** Returns a {@link LangMessage} for {@code key} in the active server locale. */
    public LangMessage get(String key) {
        return new LangMessage(this, key);
    }

    /** Returns all locale codes currently loaded. */
    public Set<String> availableLocales() {
        return Collections.unmodifiableSet(localeCache.keySet());
    }

    /** Returns the currently active server locale. */
    public String getDefaultLocale() { return defaultLocale; }

    /** Returns the underlying plugin instance. */
    public JavaPlugin getPlugin() { return plugin; }

    /**
     * Reloads all language files from disk, re-reads config and rebuilds caches.
     */
    public void reload() {
        cfm.reloadDirectory(directory);
        readConfig();
        rebuildLocaleCache();
        LOGGER.info("[LanguageManager] Reloaded " + localeCache.size()
                + " locale(s). Active: " + defaultLocale);
    }

    // ── INTERNAL – RESOLUTION ────────────────────────────────────────────────

    /** Resolves {@code key} in the active locale with fallback to the key string. */
    String resolve(String key) {
        FileConfiguration yaml = localeCache.get(defaultLocale);
        String raw = yaml != null ? yaml.getString(key) : null;

        if (raw == null) {
            LOGGER.warning("[LanguageManager] Missing key '" + key
                    + "' in locale '" + defaultLocale + "'.");
            return key;
        }

        return raw.replace(PREFIX_HOLDER, prefixCache.getOrDefault(defaultLocale, ""));
    }

    // ── INTERNAL – CACHE MANAGEMENT ──────────────────────────────────────────

    private void rebuildLocaleCache() {
        localeCache.clear();
        prefixCache.clear();
        for (ManagedConfig cfg : cfm.getDirectory(directory)) {
            String locale = stemOf(cfg.getFileName());
            FileConfiguration yaml = cfg.yaml();
            localeCache.put(locale, yaml);
            prefixCache.put(locale, yaml.getString(PREFIX_KEY, ""));
        }
    }

    private void readConfig() {
        if (!cfm.isRegistered(configPath)) return;
        String cfgLocale = cfm.get(configPath).yaml().getString(CFG_DEFAULT);
        defaultLocale = (cfgLocale != null && !cfgLocale.isBlank())
                ? normalise(cfgLocale)
                : builderDefaultLocale;
    }

    // ── INTERNAL – HELPERS ───────────────────────────────────────────────────

    private static String stemOf(String fileName) {
        String name = fileName.contains("/")
                ? fileName.substring(fileName.lastIndexOf('/') + 1)
                : fileName;
        return name.endsWith(".yml")  ? name.substring(0, name.length() - 4)
                : name.endsWith(".yaml") ? name.substring(0, name.length() - 5)
                : name;
    }

    private static String normalise(String locale) {
        if (locale == null) return "en_US";
        return locale.replace('-', '_');
    }

    // ── BUILDER ──────────────────────────────────────────────────────────────

    public static Builder builder(JavaPlugin plugin) {
        return new Builder(plugin);
    }

    public static class Builder {
        private final JavaPlugin plugin;
        private String directory     = "languages";
        private String defaultLocale = "en_US";
        private String seedFile      = null;
        private String configPath    = "config.yml";
        private ConfigFileManager cfm = null;

        private Builder(JavaPlugin plugin) { this.plugin = plugin; }

        /** Directory containing locale YAML files. Default: {@code "languages"}. */
        public Builder directory(String directory) {
            this.directory = directory; return this;
        }

        /**
         * Fallback locale used when {@code language.default} is absent from config.
         * Default: {@code "en_US"}.
         */
        public Builder defaultLocale(String locale) {
            this.defaultLocale = locale; return this;
        }

        /** Resource path copied to the language directory on first startup. */
        public Builder seedFile(String resourcePath) {
            this.seedFile = resourcePath; return this;
        }

        /**
         * Config file containing {@code language.default}.
         * Default: {@code "config.yml"}.
         */
        public Builder configPath(String configPath) {
            this.configPath = configPath; return this;
        }

        /** Share an existing {@link ConfigFileManager} (done automatically via {@code NotPlugin.languageManager()}). */
        public Builder configFileManager(ConfigFileManager cfm) {
            this.cfm = cfm; return this;
        }

        public LanguageManager build() {
            return new LanguageManager(this);
        }
    }
}
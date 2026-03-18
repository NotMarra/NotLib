package dev.notmarra.notlib.language;

import dev.notmarra.notlib.file.ConfigFileManager;
import dev.notmarra.notlib.file.ConfigOptions;
import dev.notmarra.notlib.file.ManagedConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Manages localised messages loaded from YAML files in a watched directory.
 *
 * <h3>Directory layout</h3>
 * <pre>
 * plugins/MyPlugin/
 *   languages/
 *     en_US.yml   ← default / fallback locale
 *     cs_CZ.yml
 *     de_DE.yml
 * </pre>
 *
 * <h3>YAML message file format</h3>
 * <pre>{@code
 * prefix: "<gray>[<aqua>MyPlugin</aqua>]</gray> "
 *
 * player:
 *   join: "%prefix%<green>%player% joined the server!"
 *   quit: "%prefix%<red>%player% left the server."
 *   level_up: "%prefix%<yellow>%player% reached level %level%!"
 *
 * error:
 *   no_permission: "%prefix%<red>You don't have permission."
 *   player_only: "%prefix%<red>This command can only be used by players."
 * }</pre>
 *
 * <h3>Minimal setup inside NotPlugin</h3>
 * <pre>{@code
 * private LanguageManager lang;
 *
 * @Override
 * public void initPlugin() {
 *     lang = LanguageManager.builder(this)
 *             .directory("languages")
 *             .defaultLocale("en_US")
 *             .seedFile("languages/en_US.yml")
 *             .build();
 * }
 *
 * // Sending a message:
 * lang.get("player.join")
 *     .withPlayer(player)
 *     .sendTo(player);
 *
 * // Switching locale per player:
 * lang.setLocale(player.getUniqueId().toString(), "cs_CZ");
 * }</pre>
 *
 * <h3>Prefix</h3>
 * Every message value may contain {@code %prefix%} which is replaced with the
 * {@code prefix} key from the active locale file. If the key is absent the
 * replacement is an empty string — no NullPointerException.
 */
public class LanguageManager {

    private static final Logger LOGGER = Logger.getLogger(LanguageManager.class.getName());

    private static final String PREFIX_KEY     = "prefix";
    private static final String PREFIX_HOLDER  = "%prefix%";

    private final JavaPlugin plugin;
    private final ConfigFileManager cfm;

    private final String directory;
    private final String defaultLocale;

    /** Maps arbitrary entity IDs (player UUID string, "console", …) to locale codes. */
    private final java.util.Map<String, String> localeMap = new java.util.HashMap<>();

    // ── CONSTRUCTOR ──────────────────────────────────────────────────────────

    private LanguageManager(Builder b) {
        this.plugin        = b.plugin;
        this.directory     = b.directory;
        this.defaultLocale = normalise(b.defaultLocale);

        // Wire into the plugin's ConfigFileManager so reloadAll() picks it up
        this.cfm = b.cfm != null ? b.cfm : new ConfigFileManager(plugin);

        ConfigOptions opts = ConfigOptions.builder()
                .autoUpdate(false)          // user-maintained translation files
                .copyFromResources(false)
                .seedFile(b.seedFile)
                .build();

        cfm.watchDirectory(directory, opts);
        cfm.loadAll();

        if (getLocaleConfig(defaultLocale).isEmpty()) {
            LOGGER.warning("[LanguageManager] Default locale '" + defaultLocale
                    + "' not found in '" + directory + "/'. Messages will return their keys.");
        }
    }

    // ── PUBLIC API ───────────────────────────────────────────────────────────

    /**
     * Returns a {@link LangMessage} for the given dot-separated key using the
     * <em>default</em> locale.
     *
     * @param key dot-separated YAML path, e.g. {@code "player.join"}
     */
    public LangMessage get(String key) {
        return new LangMessage(this, key);
    }

    /**
     * Returns a {@link LangMessage} for the given key using the locale registered
     * for {@code entityId}, falling back to the default locale if none is set.
     *
     * @param entityId arbitrary ID, typically a player's UUID string
     * @param key      dot-separated YAML path
     */
    public LangMessage getFor(String entityId, String key) {
        return new LangMessage(this, key, localeFor(entityId));
    }

    /**
     * Returns a {@link LangMessage} for the given key using the locale registered
     * for the given {@link org.bukkit.entity.Player}.
     */
    public LangMessage getFor(org.bukkit.entity.Player player, String key) {
        return getFor(player.getUniqueId().toString(), key);
    }

    /**
     * Registers a locale override for an entity.
     *
     * @param entityId arbitrary ID (player UUID string, "console", …)
     * @param locale   locale code matching a file in the language directory,
     *                 e.g. {@code "cs_CZ"} for {@code cs_CZ.yml}
     */
    public void setLocale(String entityId, String locale) {
        localeMap.put(entityId, normalise(locale));
    }

    /** Removes the locale override for an entity (falls back to the default). */
    public void clearLocale(String entityId) {
        localeMap.remove(entityId);
    }

    /** Returns the active locale for an entity, or the default locale if none is set. */
    public String localeFor(String entityId) {
        return localeMap.getOrDefault(entityId, defaultLocale);
    }

    /** Returns all locale codes currently loaded (file stems without {@code .yml}). */
    public java.util.Set<String> availableLocales() {
        java.util.Set<String> locales = new java.util.LinkedHashSet<>();
        for (ManagedConfig cfg : cfm.getDirectory(directory)) {
            locales.add(stemOf(cfg.getFileName()));
        }
        return locales;
    }

    /** Reloads all language files from disk. */
    public void reload() {
        cfm.reloadDirectory(directory);
        LOGGER.info("[LanguageManager] Reloaded " + availableLocales().size() + " locale(s).");
    }

    /** Returns the underlying plugin instance. */
    public JavaPlugin getPlugin() { return plugin; }

    // ── INTERNAL ─────────────────────────────────────────────────────────────

    /**
     * Resolves a key against the default locale, injects the prefix, and returns
     * the raw MiniMessage string. Used by {@link LangMessage#build()}.
     */
    String resolve(String key) {
        return resolveIn(key, defaultLocale);
    }

    /**
     * Resolves a key against a specific locale with default-locale fallback.
     */
    String resolveIn(String key, String locale) {
        // 1. Try requested locale
        Optional<FileConfiguration> yaml = getLocaleConfig(locale).map(ManagedConfig::yaml);
        String raw = yaml.map(y -> y.getString(key)).orElse(null);

        // 2. Fallback to default locale
        if (raw == null && !locale.equals(defaultLocale)) {
            Optional<FileConfiguration> fallback = getLocaleConfig(defaultLocale).map(ManagedConfig::yaml);
            raw = fallback.map(y -> y.getString(key)).orElse(null);
        }

        // 3. Last resort – return the key itself so nothing explodes silently
        if (raw == null) {
            LOGGER.warning("[LanguageManager] Missing key '" + key + "' in locale '" + locale + "'.");
            return key;
        }

        // Inject prefix
        String prefix = yaml.map(y -> y.getString(PREFIX_KEY, "")).orElse("");
        return raw.replace(PREFIX_HOLDER, prefix);
    }

    private Optional<ManagedConfig> getLocaleConfig(String locale) {
        Collection<ManagedConfig> configs = cfm.getDirectory(directory);
        return configs.stream()
                .filter(c -> stemOf(c.getFileName()).equalsIgnoreCase(locale))
                .findFirst();
    }

    /** {@code "languages/en_US.yml"} → {@code "en_US"} */
    private static String stemOf(String fileName) {
        String name = fileName.contains("/")
                ? fileName.substring(fileName.lastIndexOf('/') + 1)
                : fileName;
        return name.endsWith(".yml")  ? name.substring(0, name.length() - 4)
                : name.endsWith(".yaml") ? name.substring(0, name.length() - 5)
                : name;
    }

    /** Normalises locale codes: {@code "en-us"} → {@code "en_US"} etc. */
    private static String normalise(String locale) {
        if (locale == null) return "en_US";
        // Accept both "en_US" and "en-US" forms
        return locale.replace('-', '_');
    }

    // ── BUILDER ──────────────────────────────────────────────────────────────

    public static Builder builder(JavaPlugin plugin) {
        return new Builder(plugin);
    }

    public static class Builder {
        private final JavaPlugin plugin;
        private String directory    = "languages";
        private String defaultLocale = "en_US";
        private String seedFile     = null;
        private ConfigFileManager cfm = null;

        private Builder(JavaPlugin plugin) {
            this.plugin = plugin;
        }

        /**
         * Directory (relative to the plugin data folder) that contains locale YAML files.
         * Default: {@code "languages"}.
         */
        public Builder directory(String directory) {
            this.directory = directory;
            return this;
        }

        /**
         * Locale code of the file used as the primary source and fallback.
         * Default: {@code "en_US"} (expects {@code languages/en_US.yml}).
         */
        public Builder defaultLocale(String locale) {
            this.defaultLocale = locale;
            return this;
        }

        /**
         * Resource path of an example file to copy into the language directory on
         * first startup, giving server admins a template to translate.
         *
         * <p>Example: {@code .seedFile("languages/en_US.yml")}
         */
        public Builder seedFile(String resourcePath) {
            this.seedFile = resourcePath;
            return this;
        }

        /**
         * Provide an existing {@link ConfigFileManager} to share (e.g. the one
         * from {@code NotPlugin.getCfm()}). If omitted a new one is created internally.
         */
        public Builder configFileManager(ConfigFileManager cfm) {
            this.cfm = cfm;
            return this;
        }

        public LanguageManager build() {
            return new LanguageManager(this);
        }
    }

}
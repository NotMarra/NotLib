package dev.notmarra.notlib.extensions;

import dev.notmarra.notlib.file.ConfigFileManager;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * Base class for any plugin component that reads from one or more config files.
 *
 * <p>Subclasses declare their config paths via {@link #getConfigPaths()} and receive
 * {@link #onConfigReload} whenever those files are reloaded – either programmatically
 * or via a server command.
 *
 * <p>The {@link ConfigFileManager} integration happens transparently through
 * {@link NotPlugin#registerConfigurable}: the plugin registers the file with the CFM,
 * the CFM handles disk I/O and auto-update, and the configurable receives callbacks.
 */
public abstract class Configurable {
    public final NotPlugin plugin;

    public Configurable(NotPlugin plugin) { this.plugin = plugin; }

    public NotPlugin getPlugin() { return plugin; }

    // -----------------------------------------------------------------------
    // Config access – thin wrappers over NotPlugin / CFM
    // -----------------------------------------------------------------------

    /**
     * Returns the {@link FileConfiguration} for the given path.
     * Delegates to {@link NotPlugin#getSubConfig(String)} which reads from the CFM cache.
     */
    public FileConfiguration getConfig(String path) { return plugin.getSubConfig(path); }

    /**
     * Returns the {@link FileConfiguration} for the first path in {@link #getConfigPaths()}.
     */
    public FileConfiguration getFirstConfig() { return plugin.getSubConfig(getConfigPaths().getFirst()); }

    /**
     * Returns the plugin's main {@code config.yml}.
     *
     * <p>Example usage in {@link #isEnabled()}:
     * <pre>{@code
     * return getPluginConfig().getBoolean("modules.something");
     * }</pre>
     */
    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    // -----------------------------------------------------------------------
    // Lifecycle hooks
    // -----------------------------------------------------------------------

    /**
     * The config paths this component reads from.
     * Override to declare dependencies on additional files.
     *
     * <p>Default: the plugin's main {@code config.yml}.
     */
    public List<String> getConfigPaths() { return List.of(plugin.CONFIG_YML); }

    /**
     * Whether this component should be considered active.
     * Called during registration; return {@code false} to skip event/command registration
     * in {@link NotListener} subclasses.
     *
     * <p>Default: always enabled.
     */
    public boolean isEnabled() { return true; }

    /**
     * Called after one or more of this component's config files have been reloaded.
     *
     * @param reloadedConfigs the subset of {@link #getConfigPaths()} that were reloaded
     */
    public void onConfigReload(List<String> reloadedConfigs) {}

    // -----------------------------------------------------------------------
    // Reload helpers
    // -----------------------------------------------------------------------

    /**
     * Notifies this component that its configs have changed without re-reading files from disk.
     * Use when you already reloaded the files elsewhere and just need to propagate the change.
     */
    public Configurable reload() {
        onConfigReload(getConfigPaths());
        return this;
    }

    /**
     * Reloads all config files for this component from disk via the CFM, then calls
     * {@link #onConfigReload}.
     *
     * <p>This replaces the old manual approach of calling {@code plugin.reloadConfig(path)}
     * per-path; the CFM now handles disk I/O, auto-update and caching.
     */
    public Configurable reloadWithFiles() {
        getConfigPaths().forEach(path -> plugin.reloadConfig(path));
        // onConfigReload is triggered by NotPlugin.reloadConfig via the CONFIGURABLES index
        return this;
    }

    // -----------------------------------------------------------------------
    // Registration
    // -----------------------------------------------------------------------

    /**
     * Registers this configurable with the plugin, which in turn registers its config
     * paths with the {@link ConfigFileManager}.
     *
     * <p>Equivalent to calling {@code plugin.registerConfigurable(this)}.
     */
    public Configurable registerConfigurable() {
        plugin.registerConfigurable(this);
        return this;
    }
}
package dev.notmarra.notlib.extensions;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public abstract class Configurable {
    public final NotPlugin plugin;

    public Configurable(NotPlugin plugin) { this.plugin = plugin; }

    public NotPlugin getPlugin() { return plugin; }

    public FileConfiguration getConfig(String path) { return plugin.getSubConfig(path); }

    public FileConfiguration getFirstConfig() { return plugin.getSubConfig(getConfigPaths().getFirst()); }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public List<String> getConfigPaths() { return List.of(plugin.CONFIG_YML); }

    public void onConfigReload(List<String> reloadedConfigs) {}

    public Configurable reload() { onConfigReload(getConfigPaths()); return this; }

    public Configurable reloadWithFiles() {
        getConfigPaths().forEach(path -> plugin.reloadConfig(path));
        onConfigReload(getConfigPaths());
        return this;
    }

    //public ComponentLogger getLogger() { return plugin.getComponentLogger(); }

    /* TODO
    public NotTranslationManager tm() { return plugin.tm(); }
    public String tm(String key) { return plugin.tm(key); }
    public List<String> tmList(String key) { return plugin.tmList(key); }
    */

    public Configurable registerConfigurable() {
        plugin.registerConfigurable(this);
        return this;
    }
}

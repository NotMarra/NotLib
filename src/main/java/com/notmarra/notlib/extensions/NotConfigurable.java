package com.notmarra.notlib.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class NotConfigurable {
    public final NotPlugin plugin;
    public Map<String, FileConfiguration> configs = new HashMap<>();

    public NotConfigurable(NotPlugin plugin) {
        this(plugin, null);
    }

    public NotConfigurable(NotPlugin plugin, String defaultConfigPath) {
        this.plugin = plugin;
        plugin.registerConfigurable(this);
    }

    public void setConfig(String path, FileConfiguration config) { configs.put(path, config); }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public List<String> getConfigPaths() { return List.of(plugin.CONFIG_YML); }

    public void onConfigReload(List<String> reloadedConfigs) {}

    public void reloadConfig(String path, FileConfiguration newConfig) {
        setConfig(path, newConfig);
        onConfigReload(List.of(path));
    }

    public void reloadConfig(String path) {
        plugin.reloadConfig(path);
    }

    public NotConfigurable reload() { onConfigReload(getConfigPaths()); return this; }

    public ComponentLogger getLogger() { return plugin.getComponentLogger(); }
}
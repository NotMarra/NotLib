package com.notmarra.notlib.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class NotConfigurable {
    public final NotPlugin plugin;
    public FileConfiguration config;

    public NotConfigurable(NotPlugin plugin) {
        this.plugin = plugin;
        plugin.registerConfigurable(this);
        initialize();
    }

    public void initialize() {
        String path = getConfigPath();
        if (path == null) return;
        this.config = plugin.getSubConfig(path);
        if (config == null) return;
        loadConfig();
    }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public String getConfigPath() { return plugin.CONFIG_YML; }

    public void loadConfig() {}

    public void reloadConfig(FileConfiguration newConfig) {
        this.config = newConfig;
        loadConfig();
    }

    public void reloadConfig() {
        String path = getConfigPath();
        if (path == null) return;
        plugin.reloadConfig(path);
    }

    public ComponentLogger getLogger() { return plugin.getComponentLogger(); }
}
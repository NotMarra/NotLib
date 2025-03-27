package com.notmarra.notlib.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class NotConfigurable {
    public final NotPlugin plugin;
    public FileConfiguration config;

    private boolean isInitialized = false;

    public NotConfigurable(NotPlugin plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        if (isInitialized) return;
        isInitialized = true;
        String path = getConfigPath();
        if (path == null) return;
        this.config = plugin.getSubConfig(path);
        loadConfig();
    }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public abstract String getConfigPath();

    public void loadConfig() {}

    public void reloadConfig() {
        String path = getConfigPath();
        if (path == null) return;
        FileConfiguration newConfig = plugin.reloadConfig(path);
        if (newConfig == null) return;
        this.config = newConfig;
        loadConfig();
    }

    public ComponentLogger getLogger() { return plugin.getComponentLogger(); }
}
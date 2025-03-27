package com.notmarra.notlib.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class NotConfigurable {
    public final NotPlugin plugin;
    public FileConfiguration config;

    public NotConfigurable(NotPlugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getSubConfig(getConfigPath());
        plugin.registerConfigurable(this);
    }

    public void setConfig(FileConfiguration config) { this.config = config; }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public String getConfigPath() { return plugin.CONFIG_YML; }

    public void onConfigReload() {}

    public void reloadConfig(FileConfiguration newConfig) {
        setConfig(newConfig);
        onConfigReload();
    }

    public void reloadConfig() {
        String path = getConfigPath();
        if (path == null) return;
        plugin.reloadConfig(path);
    }

    public ComponentLogger getLogger() { return plugin.getComponentLogger(); }
}
package com.notmarra.notlib.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class NotConfigurable {
    public final NotPlugin plugin;

    public NotConfigurable(NotPlugin plugin) {
        this.plugin = plugin;
        plugin.registerConfigurable(this);
    }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public String getConfigPath() { return plugin.CONFIG_YML; }

    public void onConfigReload(FileConfiguration config) {}

    public void reloadConfig() {
        String path = getConfigPath();
        if (path == null) return;
        plugin.reloadConfig(path);
    }

    public ComponentLogger getLogger() { return plugin.getComponentLogger(); }
}
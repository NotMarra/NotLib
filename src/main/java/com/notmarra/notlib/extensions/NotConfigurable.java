package com.notmarra.notlib.extensions;

import net.kyori.adventure.text.logger.slf4j.ComponentLogger;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;

public abstract class NotConfigurable {
    public final NotPlugin plugin;

    public NotConfigurable(NotPlugin plugin) { this.plugin = plugin; }

    public FileConfiguration getConfig(String path) { return plugin.getSubConfig(path); }

    // e.g: return getPluginConfig().getBoolean("modules.something");
    public boolean isEnabled() { return true; }

    public FileConfiguration getPluginConfig() { return plugin.getConfig(); }

    public List<String> getConfigPaths() { return List.of(plugin.CONFIG_YML); }

    public void onConfigReload(List<String> reloadedConfigs) {}

    public NotConfigurable reload() { onConfigReload(getConfigPaths()); return this; }
    
    public NotConfigurable reloadWithFiles() {
        getConfigPaths().forEach(path -> plugin.reloadConfig(path));
        onConfigReload(getConfigPaths());
        return this;
    }

    public ComponentLogger getLogger() { return plugin.getComponentLogger(); }

    public NotConfigurable registerConfigurable() {
        plugin.registerConfigurable(this);
        return this;
    }
}
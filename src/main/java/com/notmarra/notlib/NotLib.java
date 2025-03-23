package com.notmarra.notlib;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotLib extends JavaPlugin {
    private static NotLib instance;
    private static Boolean PlaceholderAPI = false;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            this.getLogger().info("PlaceholderAPI found, hooking into it");
            PlaceholderAPI = true;
        }

        this.getLogger().info("Enabled successfully!");
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Disabled successfully!");
    }

    public static NotLib getInstance() {
        return instance;
    }

    public static Boolean hasPAPI() { return PlaceholderAPI; }
}

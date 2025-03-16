package com.notmarra.notlib;

import org.bukkit.plugin.java.JavaPlugin;

public final class NotLib extends JavaPlugin {
    private static NotLib instance;

    @Override
    public void onEnable() {
        instance = this;
        this.getLogger().info("NotLib has been enabled!");

    }

    @Override
    public void onDisable() {
        this.getLogger().info("NotLib has been disabled!");
    }

    public static NotLib getInstance() {
        return instance;
    }
}

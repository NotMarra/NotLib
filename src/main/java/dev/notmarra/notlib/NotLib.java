package dev.notmarra.notlib;

import dev.notmarra.notlib.scheduler.Scheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class NotLib extends JavaPlugin {
    private NotLib instance;
    private Scheduler scheduler;

    @Override
    public void onEnable() {
        instance = this;
        scheduler = new Scheduler(this);
    }

    @Override
    public void onDisable() {
    }

    public NotLib getInstance() {
        return instance;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }
}

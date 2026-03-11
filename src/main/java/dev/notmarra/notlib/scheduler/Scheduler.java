package dev.notmarra.notlib.scheduler;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.TimeUnit;

public class Scheduler {
    private final Plugin plugin;

    public Scheduler(Plugin plugin) {
        this.plugin = plugin;
    }

    // GLOBAL

    public void global(Runnable task) {
        plugin.getServer().getGlobalRegionScheduler().execute(plugin, task);
    }

    public void globalDelayed(Runnable task, long delayTicks) {
        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, $-> task.run(), delayTicks);
    }

    public void globalRepeating(Runnable task, long delayTicks, long periodTicks) {
        plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, $ -> task.run(), delayTicks, periodTicks);
    }

    // ASYNC

    public void async(Runnable task) {
        plugin.getServer().getAsyncScheduler().runNow(plugin, $ -> task.run());
    }

    public void asyncDelayed(Runnable task, long delayTicks, TimeUnit unit) {
        plugin.getServer().getAsyncScheduler().runDelayed(plugin, $ -> task.run(), delayTicks, unit);
    }

    public void asyncRepeating(Runnable task, long delayTicks, long periodTicks, TimeUnit unit) {
        plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, $ -> task.run(), delayTicks, periodTicks, unit);
    }

    public void asyncCancelTasks() {
        plugin.getServer().getAsyncScheduler().cancelTasks(plugin);
    }

    // REGION

    public void region(Location location, Runnable task) {
        plugin.getServer().getRegionScheduler().execute(plugin, location, task);
    }

    public void regionDelayed(Location location, Runnable task, long delayTicks) {
        plugin.getServer().getRegionScheduler().runDelayed(plugin, location, $ -> task.run(), delayTicks);
    }

    // ENTITY

    public void entity(Entity entity, Runnable task, Runnable retired) {
        entity.getScheduler().execute(plugin, task, retired, 1L);
    }

    public void entityDelayed(Entity entity, Runnable task, Runnable retired, long delayTicks) {
        entity.getScheduler().execute(plugin, task, retired, delayTicks);
    }
}

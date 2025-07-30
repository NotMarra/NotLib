package com.notmarra.notlib.utils;

import org.bukkit.Location;
import org.bukkit.entity.Entity;

import com.notmarra.notlib.extensions.NotPlugin;

import java.util.concurrent.TimeUnit;

/**
 * Provides static methods to easily schedule various types of tasks.
 * Supports both Paper/Spigot and Folia platforms without BukkitTask dependencies.
 */
public final class NotScheduler {
    private final NotPlugin plugin;
    private final boolean isFolia;

    public NotScheduler(NotPlugin plugin) {
        this.plugin = plugin;
        this.isFolia = isFolia();
    }

    /**
     * Check if the current server is running Folia.
     *
     * @return true if running on Folia, false otherwise
     */
    private static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // ========== MAIN THREAD / GLOBAL REGION TASKS ==========

    /**
     * Run a task on the next server tick (main thread/global region).
     *
     * @param runnable The task to run
     */
    public void runTask(Runnable runnable) {
        if (isFolia) {
            plugin.getServer().getGlobalRegionScheduler().execute(plugin, runnable);
        } else {
            plugin.getServer().getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Run a task after a specified delay in ticks (main thread/global region).
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task runs (20 ticks = 1 second)
     */
    public void runTaskLater(Runnable runnable, long delay) {
        if (isFolia) {
            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, task -> runnable.run(), delay);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Run a task repeatedly at a specified interval after an initial delay (main thread/global region).
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the first execution (20 ticks = 1 second)
     * @param period   The period in ticks between subsequent executions
     */
    public void runTaskTimer(Runnable runnable, long delay, long period) {
        if (isFolia) {
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(plugin, task -> runnable.run(), delay, period);
        } else {
            plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
        }
    }

    // ========== ASYNC TASKS ==========

    /**
     * Run a task asynchronously (off the main server thread).
     *
     * @param runnable The task to run
     */
    public void runTaskAsync(Runnable runnable) {
        if (isFolia) {
            plugin.getServer().getAsyncScheduler().runNow(plugin, task -> runnable.run());
        } else {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
        }
    }

    /**
     * Run a task asynchronously after a specified delay in ticks.
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task runs (20 ticks = 1 second)
     */
    public void runTaskLaterAsync(Runnable runnable, long delay) {
        if (isFolia) {
            long delayMs = delay * 50; // Convert ticks to milliseconds (20 ticks = 1000ms)
            plugin.getServer().getAsyncScheduler().runDelayed(plugin, task -> runnable.run(), delayMs, TimeUnit.MILLISECONDS);
        } else {
            plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
        }
    }

    /**
     * Run a task asynchronously at a specified interval after an initial delay.
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the first execution (20 ticks = 1 second)
     * @param period   The period in ticks between subsequent executions
     */
    public void runTaskTimerAsync(Runnable runnable, long delay, long period) {
        if (isFolia) {
            long delayMs = delay * 50;
            long periodMs = period * 50;
            plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, task -> runnable.run(), delayMs, periodMs, TimeUnit.MILLISECONDS);
        } else {
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
        }
    }

    // ========== REGION-SPECIFIC TASKS ==========

    /**
     * Run a task on the region that owns the specified location.
     *
     * @param location The location to determine which region to run on
     * @param runnable The task to run
     */
    public void runTaskAtLocation(Location location, Runnable runnable) {
        if (isFolia) {
            plugin.getServer().getRegionScheduler().execute(plugin, location, runnable);
        } else {
            // On Paper/Spigot, just run on main thread
            plugin.getServer().getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Run a task on the region that owns the specified location after a delay.
     *
     * @param location The location to determine which region to run on
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task runs
     */
    public void runTaskLaterAtLocation(Location location, Runnable runnable, long delay) {
        if (isFolia) {
            plugin.getServer().getRegionScheduler().runDelayed(plugin, location, task -> runnable.run(), delay);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Run a task repeatedly on the region that owns the specified location.
     *
     * @param location The location to determine which region to run on
     * @param runnable The task to run
     * @param delay    The delay in ticks before the first execution
     * @param period   The period in ticks between subsequent executions
     */
    public void runTaskTimerAtLocation(Location location, Runnable runnable, long delay, long period) {
        if (isFolia) {
            plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, location, task -> runnable.run(), delay, period);
        } else {
            plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
        }
    }

    // ========== ENTITY-SPECIFIC TASKS ==========

    /**
     * Run a task on the entity's scheduler (follows the entity across regions).
     *
     * @param entity   The entity to run the task on
     * @param runnable The task to run
     */
    public void runTaskForEntity(Entity entity, Runnable runnable) {
        if (isFolia) {
            entity.getScheduler().execute(plugin, runnable, null, 1);
        } else {
            // On Paper/Spigot, just run on main thread
            plugin.getServer().getScheduler().runTask(plugin, runnable);
        }
    }

    /**
     * Run a task on the entity's scheduler after a delay.
     *
     * @param entity   The entity to run the task on
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task runs
     */
    public void runTaskLaterForEntity(Entity entity, Runnable runnable, long delay) {
        if (isFolia) {
            entity.getScheduler().runDelayed(plugin, task -> runnable.run(), null, delay);
        } else {
            plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
        }
    }

    /**
     * Run a task repeatedly on the entity's scheduler.
     *
     * @param entity   The entity to run the task on
     * @param runnable The task to run
     * @param delay    The delay in ticks before the first execution
     * @param period   The period in ticks between subsequent executions
     */
    public void runTaskTimerForEntity(Entity entity, Runnable runnable, long delay, long period) {
        if (isFolia) {
            entity.getScheduler().runAtFixedRate(plugin, task -> runnable.run(), null, delay, period);
        } else {
            plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
        }
    }

    // ========== UTILITY METHODS ==========

    /**
     * Cancel all tasks associated with a plugin.
     * Note: This only works on Paper/Spigot.
     */
    public void cancelAllTasks() {
        if (!isFolia) {
            plugin.getServer().getScheduler().cancelTasks(plugin);
        }
        // On Folia, tasks are automatically cleaned up when the plugin is disabled
    }

    /**
     * Check if the server is running Folia.
     *
     * @return true if running on Folia, false otherwise
     */
    public boolean isFoliaServer() {
        return isFolia;
    }

    /**
     * Get the plugin instance.
     *
     * @return The plugin instance
     */
    public NotPlugin getPlugin() {
        return plugin;
    }

    // ========== STATIC UTILITY METHODS ==========

    /**
     * Create a new NotScheduler instance for the given plugin.
     *
     * @param plugin The plugin to create scheduler for
     * @return A new NotScheduler instance
     */
    public static NotScheduler create(NotPlugin plugin) {
        return new NotScheduler(plugin);
    }

    /**
     * Check if the current server is running Folia (static method).
     *
     * @return true if running on Folia, false otherwise
     */
    public static boolean isRunningFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Convert ticks to milliseconds.
     *
     * @param ticks The number of ticks
     * @return The equivalent milliseconds (1 tick = 50ms)
     */
    public static long ticksToMillis(long ticks) {
        return ticks * 50;
    }

    /**
     * Convert milliseconds to ticks.
     *
     * @param millis The number of milliseconds
     * @return The equivalent ticks (20 ticks = 1000ms)
     */
    public static long millisToTicks(long millis) {
        return millis / 50;
    }
}
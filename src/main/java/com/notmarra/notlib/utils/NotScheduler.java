package com.notmarra.notlib.utils;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;

import com.notmarra.notlib.extensions.NotPlugin;

// TODO: change to this: https://docs.papermc.io/paper/dev/folia-support/
/**
 * @deprecated
 * Provides static methods to easily schedule various types of tasks.
 */
public final class NotScheduler {
    private final NotPlugin plugin;

    public NotScheduler(NotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Run a task on the next server tick.
     *
     * @param runnable The task to run
     * @return A BukkitTask representing the task
     */
    public BukkitTask runTask(Runnable runnable) {
        return plugin.getServer().getScheduler().runTask(plugin, runnable);
    }

    /**
     * Run a task after a specified delay in ticks.
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task runs (20 ticks = 1 second)
     * @return A BukkitTask representing the task
     */
    public BukkitTask runTaskLater(Runnable runnable, long delay) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, runnable, delay);
    }

    /**
     * Run a task repeatedly at a specified interval after an initial delay.
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the first execution (20 ticks = 1 second)
     * @param period   The period in ticks between subsequent executions
     * @return A BukkitTask representing the task
     */
    public BukkitTask runTaskTimer(Runnable runnable, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, runnable, delay, period);
    }

    /**
     * Run a task asynchronously (off the main server thread).
     *
     * @param runnable The task to run
     * @return A BukkitTask representing the task
     */
    public BukkitTask runTaskAsync(Runnable runnable) {
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, runnable);
    }

    /**
     * Run a task asynchronously after a specified delay in ticks.
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the task runs (20 ticks = 1 second)
     * @return A BukkitTask representing the task
     */
    public BukkitTask runTaskLaterAsync(Runnable runnable, long delay) {
        return plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
    }

    /**
     * Run a task asynchronously at a specified interval after an initial delay.
     *
     * @param runnable The task to run
     * @param delay    The delay in ticks before the first execution (20 ticks = 1 second)
     * @param period   The period in ticks between subsequent executions
     * @return A BukkitTask representing the task
     */
    public BukkitTask runTaskTimerAsync(Runnable runnable, long delay, long period) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, period);
    }

    /**
     * Create a new BukkitRunnable task.
     *
     * @return A new BukkitRunnable
     */
    public BukkitRunnable createTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                // Override in anonymous class or with lambda
            }
        };
    }

    /**
     * Cancel a task if it's not null and is still running.
     *
     * @param task The task to cancel
     * @return true if the task was cancelled, false otherwise
     */
    public boolean cancelTask(BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            return true;
        }
        return false;
    }

    /**
     * Cancel all tasks associated with a plugin.
     *
     */
    public void cancelAllTasks() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
    }

    /**
     * Check if a task is currently running.
     *
     * @param taskId The ID of the task to check
     * @return true if the task is running, false otherwise
     */
    public boolean isTaskRunning(int taskId) {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        return scheduler.isCurrentlyRunning(taskId) || scheduler.isQueued(taskId);
    }
}
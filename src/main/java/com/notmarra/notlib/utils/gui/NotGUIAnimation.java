package com.notmarra.notlib.utils.gui;

import java.util.function.Consumer;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class NotGUIAnimation {
    private final NotGUI gui;
    private final JavaPlugin plugin;
    private BukkitTask task;
    private long totalFrames;
    private long currentFrame;
    private long frameDelay;
    private boolean isRunning = false;
    
    public NotGUIAnimation(NotGUI gui, long durationTicks, long frames) {
        this.gui = gui;
        this.plugin = gui.getPlugin();
        this.totalFrames = frames;
        this.frameDelay = Math.max(1, durationTicks / frames);
        this.currentFrame = 0;
    }
    
    public void start(Consumer<Float> frameUpdateFunction) {
        if (isRunning) return;
        isRunning = true;
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            float progress = (float) currentFrame / totalFrames;
            frameUpdateFunction.accept(progress);
            gui.refresh();
            currentFrame++;
            if (currentFrame > totalFrames) stop();
        }, 0L, frameDelay);
    }
    
    public void stop() {
        if (!isRunning) return;
        if (task != null && !task.isCancelled()) task.cancel();
        isRunning = false;
    }
}

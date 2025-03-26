package com.notmarra.notlib.utils.gui;

import java.util.function.Consumer;

import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class NotGUIAnimation {
    private final NotGUI gui;
    private long durationTicks;
    private long frames;
    private BukkitTask task;
    
    public NotGUIAnimation(NotGUI gui, long durationTicks, long frames) {
        this.gui = gui;
        this.durationTicks = durationTicks;
        this.frames = frames;
    }
    
    public void start(Consumer<Float> updateFunction) {
        if (task != null && !task.isCancelled()) task.cancel();

        final long ticksPerFrame = durationTicks / frames;

        task = new BukkitRunnable() {
            private long currentFrame = 0;
            @Override
            public void run() {
                if (currentFrame > frames) {
                    this.cancel();
                    return;
                }

                float progress = (float) currentFrame / frames;
                updateFunction.accept(progress);
                gui.refresh();
                currentFrame++;
            }
        }.runTaskTimer(gui.getPlugin(), 0L, Math.max(1, ticksPerFrame));
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) task.cancel();
    }
}

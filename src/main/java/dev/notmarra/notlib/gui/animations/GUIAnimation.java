package dev.notmarra.notlib.gui.animations;

import java.util.UUID;
import java.util.function.Consumer;

import dev.notmarra.notlib.gui.GUI;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public abstract class GUIAnimation {
    protected final GUI gui;
    protected final UUID uid;
    protected long durationTicks;
    protected long frames;
    protected BukkitTask task;
    protected boolean infinite;
    
    public GUIAnimation(GUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, false);
    }

    public GUIAnimation(GUI gui, long durationTicks, long frames, boolean infinite) {
        this.gui = gui;
        this.uid = UUID.randomUUID();
        this.durationTicks = durationTicks;
        this.frames = frames;
        this.infinite = infinite;
        gui.registerAnimation(this);
    }

    public UUID id() { return uid; }

    public GUI gui() { return gui; }

    protected abstract float calculateProgress(long currentFrame);

    public GUIAnimation inf() {
        this.infinite = true;
        return this;
    }

    public boolean isInfinite() { return infinite; }

    public void start(Consumer<Float> updateFunction) {
        cancel();

        final long ticksPerFrame = Math.max(1, durationTicks / frames);

        task = new BukkitRunnable() {
            private long currentFrame = 0;

            @Override
            public void run() {
                if (!infinite && !shouldContinue(currentFrame)) {
                    this.cancel();
                    return;
                }

                long effectiveFrame = infinite ? (currentFrame % frames) : currentFrame;
                float progress = calculateProgress(effectiveFrame);
                updateFunction.accept(progress);
                gui.refresh();
                currentFrame++;
            }
        }.runTaskTimer(gui.getPlugin(), 0L, ticksPerFrame);
    }

    protected boolean shouldContinue(long currentFrame) {
        return currentFrame <= frames;
    }

    public void cancel() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            gui.removeAnimation(this);
        }
    }
}

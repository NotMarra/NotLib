package dev.notmarra.notlib.gui.animations;

import dev.notmarra.notlib.gui.GUI;

public class GUIInfiniteAnimation extends GUIAnimation {
    public GUIInfiniteAnimation(GUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        return (float) (currentFrame % frames) / frames;
    }

    @Override
    protected boolean shouldContinue(long currentFrame) {
        return true;
    }
}
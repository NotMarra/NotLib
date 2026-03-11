package dev.notmarra.notlib.gui.animations;

import dev.notmarra.notlib.gui.GUI;

public class GUIProgressAnimation extends GUIAnimation {
    public GUIProgressAnimation(GUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        return (float) currentFrame / frames;
    }
}
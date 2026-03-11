package dev.notmarra.notlib.gui.animations;

import dev.notmarra.notlib.gui.GUI;

public class GUIStepAnimation extends GUIAnimation {
    private static final int DEFAULT_STEPS = 1;

    private int steps;

    public GUIStepAnimation(GUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_STEPS);
    }

    public GUIStepAnimation(GUI gui, long durationTicks, long frames, int steps) {
        super(gui, durationTicks, frames);
        this.steps = steps;
    }

    public GUIStepAnimation setSteps(int steps) {
        this.steps = steps;
        return this;
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        return (float) Math.floor(x * steps) / (steps - 1);
    }
}
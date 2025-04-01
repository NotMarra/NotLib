package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIStepAnimation extends NotGUIAnimation {
    private static final int DEFAULT_STEPS = 1;

    private int steps;

    public NotGUIStepAnimation(NotGUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_STEPS);
    }

    public NotGUIStepAnimation(NotGUI gui, long durationTicks, long frames, int steps) {
        super(gui, durationTicks, frames);
        this.steps = steps;
    }

    public NotGUIStepAnimation setSteps(int steps) {
        this.steps = steps;
        return this;
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        return (float) Math.floor(x * steps) / (steps - 1);
    }
}
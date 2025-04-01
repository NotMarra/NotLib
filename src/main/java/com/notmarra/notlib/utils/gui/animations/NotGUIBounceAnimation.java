package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIBounceAnimation extends NotGUIAnimation {
    private static final int DEFAULT_BOUNCES = 1;

    private int bounces;

    public NotGUIBounceAnimation(NotGUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_BOUNCES);
    }

    public NotGUIBounceAnimation(NotGUI gui, long durationTicks, long frames, int bounces) {
        super(gui, durationTicks, frames);
        this.bounces = bounces;
    }

    public NotGUIBounceAnimation setBounces(int bounces) {
        this.bounces = bounces;
        return this;
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        return (float)Math.abs(Math.sin(x * Math.PI * bounces));
    }
}
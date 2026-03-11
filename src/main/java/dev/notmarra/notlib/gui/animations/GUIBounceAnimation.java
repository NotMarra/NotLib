package dev.notmarra.notlib.gui.animations;

import dev.notmarra.notlib.gui.GUI;

public class GUIBounceAnimation extends GUIAnimation {
    private static final int DEFAULT_BOUNCES = 1;

    private int bounces;

    public GUIBounceAnimation(GUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_BOUNCES);
    }

    public GUIBounceAnimation(GUI gui, long durationTicks, long frames, int bounces) {
        super(gui, durationTicks, frames);
        this.bounces = bounces;
    }

    public dev.notmarra.notlib.gui.animations.GUIBounceAnimation setBounces(int bounces) {
        this.bounces = bounces;
        return this;
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        return (float)Math.abs(Math.sin(x * Math.PI * bounces));
    }
}
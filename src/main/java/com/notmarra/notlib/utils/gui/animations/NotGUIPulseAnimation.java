package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIPulseAnimation extends NotGUIAnimation {
    private static final int DEFAULT_PULSE_COUNT = 1;
    private static final float DEFAULT_MIN_VALUE = 0.0f;
    private static final float DEFAULT_MAX_VALUE = 1.0f;

    private int pulseCount;
    private float minValue;
    private float maxValue;

    public NotGUIPulseAnimation(NotGUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_PULSE_COUNT, DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    public NotGUIPulseAnimation(NotGUI gui, long durationTicks, long frames, int pulseCount, float minValue, float maxValue) {
        super(gui, durationTicks, frames);
        this.pulseCount = pulseCount;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public NotGUIPulseAnimation setPulseCount(int pulseCount) {
        this.pulseCount = pulseCount;
        return this;
    }

    public NotGUIPulseAnimation setMinValue(float minValue) {
        this.minValue = minValue;
        return this;
    }

    public NotGUIPulseAnimation setMaxValue(float maxValue) {
        this.maxValue = maxValue;
        return this;
    }

    public NotGUIPulseAnimation setValues(int pulseCount, float minValue, float maxValue) {
        this.pulseCount = pulseCount;
        this.minValue = minValue;
        this.maxValue = maxValue;
        return this;
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        
        float range = maxValue - minValue;
        float normalized = minValue + range * (0.5f + 0.5f * (float)Math.sin(2 * Math.PI * pulseCount * x));
        
        return normalized;
    }
}
package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIElasticAnimation extends NotGUIAnimation {
    private static final float DEFAULT_AMPLITUDE = 1.0f;
    private static final float DEFAULT_PERIOD = 0.3f;

    private float amplitude;
    private float period;

    public NotGUIElasticAnimation(NotGUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_AMPLITUDE, DEFAULT_PERIOD);
    }

    public NotGUIElasticAnimation(NotGUI gui, long durationTicks, long frames, float amplitude, float period) {
        super(gui, durationTicks, frames);
        this.amplitude = amplitude;
        this.period = period;
    }

    public NotGUIElasticAnimation setAmplitude(float amplitude) {
        this.amplitude = amplitude;
        return this;
    }

    public NotGUIElasticAnimation setPeriod(float period) {
        this.period = period;
        return this;
    }

    public NotGUIAnimation setValues(float amplitude, float period) {
        this.amplitude = amplitude;
        this.period = period;
        return this;
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;

        if (x == 0 || x >= 1) return x;
        
        float s = period / (2 * (float)Math.PI) * (float)Math.asin(1 / amplitude);
        x = x - 1;
        return amplitude * (float)Math.pow(2, 10 * x) * (float)Math.sin((x - s) * (2 * Math.PI) / period) * -1 + 1;
    }
}
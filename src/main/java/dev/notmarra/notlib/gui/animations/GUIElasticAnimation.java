package dev.notmarra.notlib.gui.animations;

import dev.notmarra.notlib.gui.GUI;

public class GUIElasticAnimation extends GUIAnimation {
    private static final float DEFAULT_AMPLITUDE = 1.0f;
    private static final float DEFAULT_PERIOD = 0.3f;

    private float amplitude;
    private float period;

    public GUIElasticAnimation(GUI gui, long durationTicks, long frames) {
        this(gui, durationTicks, frames, DEFAULT_AMPLITUDE, DEFAULT_PERIOD);
    }

    public GUIElasticAnimation(GUI gui, long durationTicks, long frames, float amplitude, float period) {
        super(gui, durationTicks, frames);
        this.amplitude = amplitude;
        this.period = period;
    }

    public GUIElasticAnimation setAmplitude(float amplitude) {
        this.amplitude = amplitude;
        return this;
    }

    public GUIElasticAnimation setPeriod(float period) {
        this.period = period;
        return this;
    }

    public GUIAnimation setValues(float amplitude, float period) {
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
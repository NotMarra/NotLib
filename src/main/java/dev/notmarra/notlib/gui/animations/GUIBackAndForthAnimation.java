package dev.notmarra.notlib.gui.animations;


import dev.notmarra.notlib.gui.GUI;

public class GUIBackAndForthAnimation extends GUIAnimation {
    public GUIBackAndForthAnimation(GUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        return 1 - Math.abs(2 * x - 1);
    }
}
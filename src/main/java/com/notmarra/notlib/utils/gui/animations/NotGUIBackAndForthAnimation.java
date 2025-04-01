package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIBackAndForthAnimation extends NotGUIAnimation {
    public NotGUIBackAndForthAnimation(NotGUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float x = (float) currentFrame / frames;
        return 1 - Math.abs(2 * x - 1);
    }
}
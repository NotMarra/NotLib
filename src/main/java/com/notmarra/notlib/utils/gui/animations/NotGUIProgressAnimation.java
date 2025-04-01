package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIProgressAnimation extends NotGUIAnimation {
    public NotGUIProgressAnimation(NotGUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        return (float) currentFrame / frames;
    }
}
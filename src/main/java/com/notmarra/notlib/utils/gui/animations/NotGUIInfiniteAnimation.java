package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIInfiniteAnimation extends NotGUIAnimation {
    public NotGUIInfiniteAnimation(NotGUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        return (float) (currentFrame % frames) / frames;
    }

    @Override
    protected boolean shouldContinue(long currentFrame) {
        return true;
    }
}
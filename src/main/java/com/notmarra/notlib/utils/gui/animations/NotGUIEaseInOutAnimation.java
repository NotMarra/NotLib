package com.notmarra.notlib.utils.gui.animations;

import com.notmarra.notlib.utils.gui.NotGUI;

public class NotGUIEaseInOutAnimation extends NotGUIAnimation {
    public NotGUIEaseInOutAnimation(NotGUI gui, long durationTicks, long frames) {
        super(gui, durationTicks, frames);
    }

    @Override
    protected float calculateProgress(long currentFrame) {
        float linear = (float) currentFrame / frames;
        
        if (linear < 0.5f) {
            return 4 * linear * linear * linear;
        } else {
            float t = linear - 1;
            return 1 + t * t * t * (4 * t + 6);
        }
    }
}
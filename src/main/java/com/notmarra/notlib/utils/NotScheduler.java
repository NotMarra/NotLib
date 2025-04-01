package com.notmarra.notlib.utils;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotScheduler implements Runnable {

    private final NotPlugin plugin;

    public NotScheduler(NotPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void run() { //TODO: some type of taskmanager and autorunnable?

    }
}

package com.notmarra.notlib.utils;

import com.notmarra.notlib.NotLib;

public class NotScheduler implements Runnable {

    private final NotLib notLib;

    public NotScheduler(NotLib notLib) {
        this.notLib = notLib;
    }
    
    @Override
    public void run() { //TODO: some type of taskmanager and autorunnable?

    }
    
}

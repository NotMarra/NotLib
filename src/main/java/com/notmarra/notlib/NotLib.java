package com.notmarra.notlib;

import com.notmarra.notlib.cache.NotCache;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotDebugger;
import com.notmarra.notlib.utils.NotUpdater;
import com.notmarra.notlib.utils.gui.NotGUIListener;

public final class NotLib extends NotPlugin {
    private static NotLib instance;
    private static Boolean hasPlaceholderAPI = false;
    private static Boolean hasVault = false;
    private static String PLUGINURL = "https://github.com/NotMarra/NotLib";
    private static String FETCHURL = "https://api.github.com/repos/NotMarra/NotLib/releases";

    private NotDebugger debugger;
    public NotDebugger getDebugger() { return debugger; }

    @Override
    public void initNotPlugin() {
        instance = this;
        this.debugger = new NotDebugger(this);
        NotCache.initialize(this);

        addListener(new NotGUIListener(this));

        addCommandGroup(new NotLibCommandGroup(this));
        //addCommandGroup(new NotDevCommandGroup(this));

        // plugin callbacks
        addPluginEnabledCallback("PlaceholderAPI", () -> hasPlaceholderAPI = true);
        addPluginEnabledCallback("Vault", () -> hasVault = true);
    }

    @Override
    public void onNotPluginEnable() {
        log().info(ChatF.of("NotLib started successfully!").build());

        NotUpdater.check(instance, PLUGINURL, FETCHURL);
    }

    @Override
    public void onNotPluginDisable() {
        log().info(ChatF.of("NotLib shut down successfully!").build());
    }

    public NotGUIListener getNotGUIListener() { return (NotGUIListener)getListener(NotGUIListener.ID); }

    public static NotDebugger dbg() { return getInstance().getDebugger(); }
    public static NotLib getInstance() { return instance; }
    public static Boolean hasPlaceholderAPI() { return hasPlaceholderAPI; }
    public static Boolean hasVault() { return hasVault; }
}

package com.notmarra.notlib;

import com.notmarra.notlib.cache.NotCache;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.quests.NotQuestListener;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotDebugger;
import com.notmarra.notlib.utils.gui.NotGUIListener;

public final class NotLib extends NotPlugin {
    private static NotLib instance;
    private static Boolean hasPlaceholderAPI = false;
    private static Boolean hasVault = false;

    private NotDebugger debugger;
    public NotDebugger getDebugger() { return debugger; }

    @Override
    public void initNotPlugin() {
        instance = this;
        this.debugger = new NotDebugger(this);
        //NotCache.initialize(this);

        // listeners
        //addListener(new NotGUIListener(this));

        //addListener(new NotQuestListener(this));

        // commands
        addCommandGroup(new NotLibCommandGroup(this));
        //addCommandGroup(new NotDevCommandGroup(this));

        // TODO: test stuff, remove
        //addListener(new NotDevListener(this));
        // db().registerDatabase(new NotDevTestMySQL(this, CONFIG_YML));

        // plugin callbacks
        addPluginEnabledCallback("PlaceholderAPI", () -> hasPlaceholderAPI = true);
        addPluginEnabledCallback("Vault", () -> hasVault = true);
    }

    @Override
    public void onNotPluginEnable() {
        log().info(ChatF.of("Enabled!").build());
    }

    @Override
    public void onDisable() {
        log().info(ChatF.of("Disabled!").build());
    }

    public NotGUIListener getNotGUIListener() { return (NotGUIListener)getListener(NotGUIListener.ID); }

    public static NotDebugger dbg() { return getInstance().getDebugger(); }
    public static NotLib getInstance() { return instance; }
    public static Boolean hasPlaceholderAPI() { return hasPlaceholderAPI; }
    public static Boolean hasVault() { return hasVault; }
}

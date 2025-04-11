package com.notmarra.notlib;

import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.quests.NotQuestListener;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotDebugger;
import com.notmarra.notlib.utils.gui.NotGUIListener;

public final class NotLib extends NotPlugin {
    private static NotLib instance;
    private static Boolean hasPlaceholderAPI = false;
    private static Boolean hasVault = false;

    public static final String DEBUG_DB = "debug_db";

    @Override
    public void initNotPlugin() {
        NotDebugger.register(DEBUG_DB);

        // listeners
        addListener(new NotGUIListener(this));

        addListener(new NotQuestListener(this));

        // commands
        addCommandGroup(new NotDevCommandGroup(this));

        // TODO: test stuff, remove
        addListener(new NotDevListener(this));
        // db().registerDatabase(new NotDevTestMySQL(this, CONFIG_YML));

        // plugin callbacks
        addPluginEnabledCallback("PlaceholderAPI", () -> hasPlaceholderAPI = true);
        addPluginEnabledCallback("Vault", () -> hasVault = true);
    }

    @Override
    public void onEnable() {
        instance = this;
        super.onEnable();
        log().info(ChatF.of("Enabled!").build());
    }

    @Override
    public void onDisable() {
        log().info(ChatF.of("Disabled!").build());
    }

    public NotGUIListener getNotGUIListener() { return (NotGUIListener)getListener(NotGUIListener.ID); }

    public static NotLib getInstance() { return instance; }
    public static Boolean hasPlaceholderAPI() { return hasPlaceholderAPI; }
    public static Boolean hasVault() { return hasVault; }
}

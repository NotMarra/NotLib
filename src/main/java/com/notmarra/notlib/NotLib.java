package com.notmarra.notlib;

import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.gui.NotGUIListener;

public final class NotLib extends NotPlugin {
    private static NotLib instance;
    private static Boolean PlaceholderAPI = false;
    private NotGUIListener guiListener;

    @Override
    public void initListeners() {
        addListener(NotGUIListener.ID, new NotGUIListener(this));
    }

    @Override
    public void initCommandGroups() {
        addCommandGroup(NotDevCommandGroup.ID, new NotDevCommandGroup(this));
    }

    @Override
    public void initPluginCallbacks() {
        addPluginEnabledCallback("PlaceholderAPI", () -> {
            this.getLogger().info("PlaceholderAPI found, hooking into it");
            PlaceholderAPI = true;
        });
    }

    @Override
    public void onEnable() {
        super.onEnable();
        this.getComponentLogger().info(ChatF.of("Enabled!").build());
    }

    @Override
    public void onDisable() {
        this.getComponentLogger().info(ChatF.of("Disabled!").build());
    }

    public static NotLib getInstance() {
        return instance;
    }

    public static Boolean hasPAPI() { return PlaceholderAPI; }

    public NotGUIListener getGUIListener() {
        return guiListener;
    }
}

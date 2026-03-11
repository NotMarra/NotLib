package dev.notmarra.notlib;

import dev.notmarra.notlib.extensions.NotPlugin;
import dev.notmarra.notlib.gui.GUIListener;

public final class NotLib extends NotPlugin {
    private static NotLib instance;

    @Override
    public void initPlugin() {
        instance = this;

        addListener(new GUIListener(this));
        addCommandGroup(new NotLibCommandGroup(this));
    }

    @Override
    public void onPluginEnable() {
        this.getLogger().info("Plugin started successfully!");
    }

    @Override
    public void onPluginDisable() {
        this.getLogger().info("Plugin shutdown successfully!");
    }

    public static NotLib getInstance() {
        return instance;
    }

    public GUIListener getGUIListener() { return (GUIListener)getListener(GUIListener.ID); }

}

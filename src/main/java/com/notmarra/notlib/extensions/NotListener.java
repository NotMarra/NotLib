package com.notmarra.notlib.extensions;

import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

import org.bukkit.Server;
import org.bukkit.event.Listener;

public abstract class NotListener extends NotConfigurable implements Listener {
    private boolean isRegistered = false;

    public NotListener(NotPlugin plugin) {
        super(plugin);
    }

    public abstract String getId();

    public void onRegister() {}

    public List<NotCommand> notCommands() { return List.of(); }

    public Server getServer() { return plugin.getServer(); }

    @Override
    public String getConfigPath() { return null; }

    public void register() {
        if (isRegistered) return;
        isRegistered = true;

        initialize();

        if (!isEnabled()) return;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.onRegister();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            notCommands().forEach(cmd -> commands.registrar().register(cmd.build()));
        });
    }
}
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
        registerConfigurable();
    }

    public abstract String getId();

    public void onRegister() {}

    public List<NotCommand> notCommands() { return List.of(); }

    public Server getServer() { return plugin.getServer(); }

    @Override
    public List<String> getConfigPaths() { return List.of(); }

    public void register() {
        if (isRegistered) return;
        isRegistered = true;

        if (!isEnabled()) return;
        this.onRegister();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            notCommands().forEach(cmd -> commands.registrar().register(cmd.build()));
        });
    }
}
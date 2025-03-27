package com.notmarra.notlib.extensions;

import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

import org.bukkit.Server;

public abstract class NotCommandGroup extends NotConfigurable {
    private boolean isRegistered = false;

    public NotCommandGroup(NotPlugin plugin) {
        super(plugin);
    }

    public abstract String getId();

    public void onRegister() {}

    public abstract List<NotCommand> notCommands();

    public Server getServer() { return plugin.getServer(); }

    @Override
    public String getConfigPath() { return null; }

    public void register() {
        if (isRegistered) return;
        isRegistered = true;

        if (!isEnabled()) return;
        this.onRegister();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            notCommands().forEach(cmd -> commands.registrar().register(cmd.build()));
        });
    }
}
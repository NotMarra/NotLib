package com.notmarra.notlib.extensions;

import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

public abstract class NotCommandGroup extends NotConfigurable {
    private boolean isRegistered = false;

    public NotCommandGroup(NotPlugin plugin) {
        super(plugin);
    }

    public void onRegister() {}

    public abstract List<NotCommand> notCommands();

    public void register() {
        if (isRegistered) return;
        isRegistered = true;

        initialize();
        if (!isEnabled()) return;
        this.onRegister();

        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            notCommands().forEach(cmd -> commands.registrar().register(cmd.build()));
        });
    }
}
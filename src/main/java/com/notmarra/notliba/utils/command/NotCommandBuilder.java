package com.notmarra.notliba.utils.command;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.registrar.ReloadableRegistrarEvent;

import java.util.List;

public class NotCommandBuilder {
    private List<NotCommand> commands;

    public NotCommandBuilder(List<NotCommand> commands) {
        this.commands = commands;
    }

    public NotCommandBuilder build (ReloadableRegistrarEvent<Commands> event) {
        for (NotCommand command : commands) {
            event.registrar().register(command.build());
        }
        return this;
    }
}

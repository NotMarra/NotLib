package com.notmarra.notlib;

import java.util.List;

import com.notmarra.notlib.extensions.NotCommandGroup;
import com.notmarra.notlib.extensions.NotPlugin;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotDebugger;
import com.notmarra.notlib.utils.command.NotCommand;

public final class NotLibCommandGroup extends NotCommandGroup {
    public static final String ID = "notlibcommandgroup";

    public NotLibCommandGroup(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public List<NotCommand> notCommands() {
        return List.of(
                notLibCommand());
    }

    private NotCommand notLibCommand() {
        NotCommand command = NotCommand.of("notlib", cmd -> {
            ChatF.of("Current version: " + plugin.getPluginMeta().getVersion()).sendTo(cmd.getPlayer());
        });

        command.literalArg("reload", arg -> {
            plugin.reloadConfig(plugin.CONFIG_YML);
            NotLib.dbg().log(NotDebugger.C_INFO, "NotLib has been reloaded");
            ChatF.of("NotLib has been reloaded!", ChatF.C_GREEN).sendTo(arg.getPlayer());
        });

        return command;
    }
}

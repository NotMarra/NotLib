package dev.notmarra.notlib;

import dev.notmarra.notlib.chat.Colors;
import dev.notmarra.notlib.chat.Text;
import dev.notmarra.notlib.command.Command;
import dev.notmarra.notlib.extensions.CommandGroup;
import dev.notmarra.notlib.extensions.NotPlugin;

import java.util.List;

public final class NotLibCommandGroup extends CommandGroup {

    public NotLibCommandGroup(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public List<Command> notCommands() {
        return List.of(notLibCommand());
    }

    private Command notLibCommand() {
        Command command = Command.of("notlib")
                .onExecute(cmd -> {
                    Text.of("Current version: " + plugin.getPluginMeta().getVersion()).sendTo(cmd.getPlayer());
                });

        command.literalArg("reload")
                .setPermission("notlib.reload")
                .onExecute(arg -> {
                    plugin.reloadConfig(plugin.CONFIG_YML);
                    plugin.getLogger().info("NotLib has been reloaded");
                    Text.of("NotLib has been reloaded!", Colors.GREEN.get()).sendTo(arg.getPlayer());
                });

        return command;
    }
}
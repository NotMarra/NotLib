package com.notmarra.notlib.utils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.function.Consumer;

public class NotCommand extends Base<NotCommand> {
    public NotCommand(String name) { super(name); }
    public static NotCommand of(String name) { return new NotCommand(name); }
    public static NotCommand of(String name, Consumer<NotCommand> executor) { return (NotCommand)NotCommand.of(name).onExecute(executor); }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(this.name);
        for (String arg : this.arguments.keySet()) {
            cmd.then(this.arguments.get(arg).build());
        }
        cmd.requires(source -> {
            if (this.permission != null) {
                return source.getSender().hasPermission(this.permission);
            }
            return true;
        });
        if (executor != null) {
            cmd.executes(ctx -> {
                setContext(ctx);
                executor.accept(this);
                return 1;
            });
        }
        return cmd.build();
    }
}

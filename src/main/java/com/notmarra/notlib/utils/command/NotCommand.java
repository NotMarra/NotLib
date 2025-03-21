package com.notmarra.notlib.utils.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.notmarra.notlib.utils.command.arguments.NotArgument;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.HashMap;

public class NotCommand extends Base {

    public NotCommand(String name) {
        super(name);
    }

    public NotCommand setArgs(HashMap<String, NotArgument<?>> args) {
        this.arguments = args;
        return this;
    }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(this.name);
        for (String arg : this.arguments.keySet()) {
            cmd = cmd.then(this.arguments.get(arg).build());
        }
        cmd.requires(source -> {
            if (this.permission != null) {
                return source.getSender().hasPermission(this.permission);
            }
            return true;
        });
        if (executor != null) {
            cmd.executes(ctx -> {
                executor.accept(ctx);
                return 1;
            });
        }
        return cmd.build();
    }

}

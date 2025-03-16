package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.notmarra.notlib.utils.command.Base;

import io.papermc.paper.command.brigadier.CommandSourceStack;



public abstract class NotArgument<T> extends Base {
    public NotArgument(String name) {
        super(name);
    }

    public abstract ArgumentBuilder<CommandSourceStack, ?> construct();

    public abstract T get(CommandContext<CommandSourceStack> ctx);

    @SuppressWarnings("unchecked")
    public CommandNode<CommandSourceStack> build() {
        ArgumentBuilder<CommandSourceStack, ?> cmd = construct();

        for (String arg : this.arguments.keySet()) {
            NotArgument<?> argument = this.arguments.get(arg);

            ArgumentBuilder<CommandSourceStack, ?> argBuilder = argument.construct();

            if (argBuilder instanceof RequiredArgumentBuilder && !argument.suggestions.isEmpty()) {
                argBuilder = ((RequiredArgumentBuilder<CommandSourceStack, ?>) argBuilder).suggests(
                    (context, suggestionsBuilder) -> {
                        for (String suggestion : argument.suggestions) {
                            suggestionsBuilder.suggest(suggestion);
                        }
                        return suggestionsBuilder.buildFuture();
                    }
                );
            }

            if (argument.executor != null) {
                argBuilder = argBuilder.executes(context -> argument.executor.apply(context));
            }

            cmd = cmd.then(argBuilder.build());
        }


        if (cmd instanceof RequiredArgumentBuilder && !this.suggestions.isEmpty()) {
            cmd = ((RequiredArgumentBuilder<CommandSourceStack, ?>) cmd).suggests(
                (context, suggestionsBuilder) -> {
                    for (String suggestion : this.suggestions) {
                        suggestionsBuilder.suggest(suggestion);
                    }
                    return suggestionsBuilder.buildFuture();
                }
            );
        }

        cmd = cmd.requires(source -> {
            if (this.permission != null) {
                return source.getSender().hasPermission(this.permission);
            }
            return true;
        });

        if (this.executor != null) {
            cmd = cmd.executes(context -> executor.apply(context));
        }

        return cmd.build();
    }
}


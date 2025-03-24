package com.notmarra.notlib.utils.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.notmarra.notlib.utils.command.Base;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public abstract class NotArgument<T> extends Base<NotArgument<T>> {
    public NotArgument(String name) {
        super(name);
    }

    public abstract ArgumentBuilder<CommandSourceStack, ?> construct();

    public abstract T get();

    @Override
    public Base<NotArgument<T>> onExecute(Consumer<NotArgument<T>> executor) {
        return super.onExecute(executor);
    }

    @SuppressWarnings({ "unchecked" })
    public CommandNode<CommandSourceStack> build() {
        ArgumentBuilder<CommandSourceStack, ?> cmd = construct();

        for (String arg : this.arguments.keySet()) {
            NotArgument<Object> argument = this.arguments.get(arg);

            ArgumentBuilder<CommandSourceStack, ?> argBuilder = argument.construct();

            if (argBuilder instanceof RequiredArgumentBuilder && !argument.suggestions.isEmpty()) {
                argBuilder = ((RequiredArgumentBuilder<CommandSourceStack, ?>) argBuilder).suggests(
                    (ctx, suggestionsBuilder) -> {
                        argument.suggestions.forEach(suggestionsBuilder::suggest);
                        return suggestionsBuilder.buildFuture();
                    }
                );
            }

            if (argument.executor != null) {
                argBuilder = argBuilder.executes(ctx -> {
                    argument.setContext(ctx);
                    argument.executor.accept(argument);
                    return 1;
                });
            }

            cmd = cmd.then(argBuilder.build());
        }


        if (cmd instanceof RequiredArgumentBuilder && !this.suggestions.isEmpty()) {
            cmd = ((RequiredArgumentBuilder<CommandSourceStack, ?>) cmd).suggests(
                (ctx, suggestionsBuilder) -> {
                    this.suggestions.forEach(suggestionsBuilder::suggest);
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
            cmd = cmd.executes(ctx -> {
                setContext(ctx);
                executor.accept(this);
                return 1;
            });
        }

        return cmd.build();
    }
}


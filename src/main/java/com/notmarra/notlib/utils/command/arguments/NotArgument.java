package com.notmarra.notlib.utils.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.notmarra.notlib.utils.command.Base;
import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public abstract class NotArgument<T> extends Base<NotArgument<T>> {
    public NotArgument(String name) {
        super(name);
    }

    public NotCommand cmd() {
        Base<?> parent = this;
        while (parent.parent != null) parent = parent.parent;
        return (NotCommand)parent;
    }

    public abstract ArgumentBuilder<CommandSourceStack, ?> construct();

    public abstract T get();

    @Override
    public Base<NotArgument<T>> onExecute(Consumer<NotArgument<T>> executor) {
        return super.onExecute(executor);
    }

    @SuppressWarnings({ "unchecked" })
    public CommandNode<CommandSourceStack> build() {
        ArgumentBuilder<CommandSourceStack, ?> arg = construct();

        for (String subArg : this.arguments.keySet()) {
            arg.then(this.arguments.get(subArg).build());
        }

        if (arg instanceof RequiredArgumentBuilder && !this.suggestions.isEmpty()) {
            ((RequiredArgumentBuilder<CommandSourceStack, ?>) arg).suggests(
                (ctx, suggestionsBuilder) -> {
                    this.suggestions.forEach(suggestionsBuilder::suggest);
                    return suggestionsBuilder.buildFuture();
                }
            );
        }

        arg.requires(source -> {
            if (this.permission != null) {
                return source.getSender().hasPermission(this.permission);
            }
            return true;
        });

        if (this.executor != null) {
            arg.executes(ctx -> {
                cmd().setContext(ctx);
                executor.accept(this);
                return 1;
            });
        }

        return arg.build();
    }
}


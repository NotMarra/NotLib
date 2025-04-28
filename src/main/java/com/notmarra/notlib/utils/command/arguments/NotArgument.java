package com.notmarra.notlib.utils.command.arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.command.Base;
import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.command.brigadier.CommandSourceStack;

public abstract class NotArgument<T> extends Base<NotArgument<T>> {
    public boolean isLiteral = false;

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

    private String sigArgs(List<String> args) {
        return String.join(" ", args.stream().map(x -> "<" + x + ">").toList());
    }

    public String getPath() {
        List<String> path = new ArrayList<>();
        path.add(name);
        Base<?> current = parent;
        while (current != null) {
            if (!(current instanceof NotCommand)) {
                path.add(current.name);
            }
            current = current.parent;
        }
        Collections.reverse(path);
        return String.join(".", path);
    }

    public ChatF getHelp() {
        return cmd().getHelpFor(List.of(getPath()));
    }

    public String getSignature() {
        List<String> path = new ArrayList<>();
        path.add(name);

        Base<?> current = parent;
        while (current != null) {
            path.add(current.name);
            current = current.parent;
        }

        Collections.reverse(path);
        String command = path.get(0);
        List<String> subArgs = path.subList(1, path.size() - 1);
        return "/" + command + " " + sigArgs(subArgs);
    }

    public String getSignatureWith(String... suffixArgs) {
        return getSignature() + " " + sigArgs(List.of(suffixArgs));
    }

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
        } else {
            arg.executes(ctx -> {
                cmd().setContext(ctx);
                getHelp().sendTo(getPlayer());
                return 1;
            });
        }

        return arg.build();
    }
}


package com.notmarra.notlib.utils.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.notmarra.notlib.utils.command.arguments.NotArgument;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public abstract class Base {
    public final String name;
    public HashMap<String, NotArgument<?>> arguments = new HashMap<>();
    @Nullable public Consumer<CommandContext<CommandSourceStack>> executor;
    public List<String> suggestions = List.of();
    @Nullable public String permission;


    public Base(String name) {
        this.name = name;
    }

    public Base addArg(NotArgument<?> arg) {
        this.arguments.put(arg.name, arg);
        return this;
    }

    public Base onExecute(Consumer<CommandContext<CommandSourceStack>> executor) {
        this.executor = executor;
        return this;
    }

    public Base setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public Base setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public @Nullable String getPermission() {
        return this.permission;
    }

    public abstract CommandNode<CommandSourceStack> build();
}

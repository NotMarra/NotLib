package com.notmarra.notlib.utils.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class NotStringArg extends NotArgument<String> {
    public NotStringArg(String name) {
        super(name);
    }

    public static NotStringArg of(String name) { return new NotStringArg(name); }
    public static NotStringArg of(String name, Object description) {
        return (NotStringArg)NotStringArg.of(name).setDescription(description);
    }
    public static NotStringArg of(String name, Consumer<NotArgument<String>> executor) {
        return (NotStringArg)NotStringArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, String> construct() {
        return Commands.argument(this.name, StringArgumentType.string());
    }

    @Override
    public String get() {
        return StringArgumentType.getString(this.ctx, this.name);
    }
}
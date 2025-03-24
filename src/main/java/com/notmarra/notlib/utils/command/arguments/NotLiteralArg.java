package com.notmarra.notlib.utils.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class NotLiteralArg extends NotArgument<String> {
    public NotLiteralArg(String name) {
        super(name);
    }

    public static NotLiteralArg of(String name) { return new NotLiteralArg(name); }
    public static NotLiteralArg of(String name, Consumer<NotArgument<String>> executor) {
        return (NotLiteralArg)NotLiteralArg.of(name).onExecute(executor);
    }

    public LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(this.name);
    }

    @Override
    public String get() {
        return this.name;
    }
}

package com.notmarra.notlib.utils.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class NotGreedyStringArg extends NotArgument<String> {
    public NotGreedyStringArg(String name) {
        super(name);
    }

    public static NotGreedyStringArg of(String name) { return new NotGreedyStringArg(name); }
    public static NotGreedyStringArg of(String name, Consumer<NotArgument<String>> executor) {
        return (NotGreedyStringArg)NotGreedyStringArg.of(name).onExecute(executor);
    }
    
    @Override
    public RequiredArgumentBuilder<CommandSourceStack, String> construct() {
        return Commands.argument(this.name, StringArgumentType.greedyString());
    }

    @Override
    public String get() {
        return StringArgumentType.getString(this.ctx, this.name);
    }
}
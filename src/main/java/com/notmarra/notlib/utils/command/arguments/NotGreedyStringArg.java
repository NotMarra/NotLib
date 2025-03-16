package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class NotGreedyStringArg extends NotArgument<String> {

    public NotGreedyStringArg(String name) {
        super(name);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, String> construct() {
        return Commands.argument(this.name, StringArgumentType.greedyString());
    }

    @Override
    public String get(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, this.name);
    }
}
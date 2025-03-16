package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class NotLiteralArg extends NotArgument<String> {

    public NotLiteralArg(String name) {
        super(name);
    }

    public LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(this.name);
    }

    @Override
    public String get(CommandContext<CommandSourceStack> ctx) {
        return this.name;
    }
}

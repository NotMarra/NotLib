package com.notmarra.notlib.utils.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class NotBoolArg extends NotArgument<Boolean> {

    public NotBoolArg(String name) {
        super(name);
    }

    public static NotBoolArg of(String name) { return new NotBoolArg(name); }
    public static NotBoolArg of(String name, Object description) {
        return (NotBoolArg)NotBoolArg.of(name).setDescription(description);
    }
    public static NotBoolArg of(String name, Consumer<NotArgument<Boolean>> executor) {
        return (NotBoolArg)NotBoolArg.of(name).onExecute(executor);
    }

    public RequiredArgumentBuilder<CommandSourceStack, Boolean> construct() {
        return Commands.argument(this.name, BoolArgumentType.bool());
    }

    @Override
    public Boolean get() {
        return BoolArgumentType.getBool(this.ctx, this.name);
    }
}

package dev.notmarra.notlib.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class BoolArg extends Argument<Boolean> {

    public BoolArg(String name) {
        super(name);
    }

    public static BoolArg of(String name) { return new BoolArg(name); }
    public static BoolArg of(String name, Object description) {
        return (BoolArg) BoolArg.of(name).setDescription(description);
    }
    public static BoolArg of(String name, Consumer<Argument<Boolean>> executor) {
        return (BoolArg) BoolArg.of(name).onExecute(executor);
    }

    public RequiredArgumentBuilder<CommandSourceStack, Boolean> construct() {
        return Commands.argument(this.name, BoolArgumentType.bool());
    }

    @Override
    public Boolean get() {
        return BoolArgumentType.getBool(this.ctx, this.name);
    }
}

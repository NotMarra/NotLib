package dev.notmarra.notlib.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class GreedyStringArg extends Argument<String> {
    public GreedyStringArg(String name) {
        super(name);
    }

    public static GreedyStringArg of(String name) { return new GreedyStringArg(name); }
    public static GreedyStringArg of(String name, Object description) {
        return (GreedyStringArg) GreedyStringArg.of(name).setDescription(description);
    }
    public static GreedyStringArg of(String name, Consumer<Argument<String>> executor) {
        return (GreedyStringArg) GreedyStringArg.of(name).onExecute(executor);
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
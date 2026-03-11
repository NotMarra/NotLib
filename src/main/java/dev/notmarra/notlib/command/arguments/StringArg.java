package dev.notmarra.notlib.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class StringArg extends Argument<String> {
    public StringArg(String name) {
        super(name);
    }

    public static StringArg of(String name) { return new StringArg(name); }
    public static StringArg of(String name, Object description) {
        return (StringArg) StringArg.of(name).setDescription(description);
    }
    public static StringArg of(String name, Consumer<Argument<String>> executor) {
        return (StringArg) StringArg.of(name).onExecute(executor);
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
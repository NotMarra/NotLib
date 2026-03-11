package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class IntArg extends Argument<Integer> {
    @Nullable private Integer min;
    @Nullable private Integer max;

    public IntArg(String name) {
        super(name);
    }

    public static IntArg of(String name) { return new IntArg(name); }
    public static IntArg of(String name, Object description) {
        return (IntArg) IntArg.of(name).setDescription(description);
    }
    public static IntArg of(String name, Consumer<Argument<Integer>> executor) {
        return (IntArg) IntArg.of(name).onExecute(executor);
    }

    public IntArg setMin(int min) {
        this.min = min;
        return this;
    }

    public IntArg setMax(int max) {
        this.max = max;
        return this;
    }

    public IntArg setRange(int min, int max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Integer> construct() {
        if (this.min != null && this.max != null) {
            return Commands.argument(this.name, IntegerArgumentType.integer(this.min, this.max));
        } else if (this.min != null) {
            return Commands.argument(this.name, IntegerArgumentType.integer(this.min));
        } else if (this.max != null) {
            return Commands.argument(this.name, IntegerArgumentType.integer(this.max));
        } else {
            return Commands.argument(this.name, IntegerArgumentType.integer());
        }
    }

    @Override
    public Integer get() {
        return IntegerArgumentType.getInteger(this.ctx, this.name);
    }
}

package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class DoubleArg extends Argument<Double> {
    @Nullable private Double min;
    @Nullable private Double max;

    public DoubleArg(String name) {
        super(name);
    }

    public static DoubleArg of(String name) { return new DoubleArg(name); }
    public static DoubleArg of(String name, Object description) {
        return (DoubleArg) DoubleArg.of(name).setDescription(description);
    }
    public static DoubleArg of(String name, Consumer<Argument<Double>> executor) {
        return (DoubleArg) DoubleArg.of(name).onExecute(executor);
    }

    public DoubleArg setMin(double min) {
        this.min = min;
        return this;
    }

    public DoubleArg setMax(double max) {
        this.max = max;
        return this;
    }

    public DoubleArg setRange(double min, double max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Double> construct() {
        if (this.min != null && this.max != null) {
            return Commands.argument(this.name, DoubleArgumentType.doubleArg(this.min, this.max));
        } else if (this.min != null) {
            return Commands.argument(this.name, DoubleArgumentType.doubleArg(this.min));
        } else if (this.max != null) {
            return Commands.argument(this.name, DoubleArgumentType.doubleArg(this.max));
        } else {
            return Commands.argument(this.name, DoubleArgumentType.doubleArg());
        }
    }


    @Override
    public Double get() {
        return DoubleArgumentType.getDouble(this.ctx, this.name);
    }
}

package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class NotDoubleArg extends NotArgument<Double> {
    @Nullable private Double min;
    @Nullable private Double max;

    public NotDoubleArg(String name) {
        super(name);
    }

    public static NotDoubleArg of(String name) { return new NotDoubleArg(name); }
    public static NotDoubleArg of(String name, Object description) {
        return (NotDoubleArg)NotDoubleArg.of(name).setDescription(description);
    }
    public static NotDoubleArg of(String name, Consumer<NotArgument<Double>> executor) {
        return (NotDoubleArg)NotDoubleArg.of(name).onExecute(executor);
    }

    public NotDoubleArg setMin(double min) {
        this.min = min;
        return this;
    }

    public NotDoubleArg setMax(double max) {
        this.max = max;
        return this;
    }

    public NotDoubleArg setRange(double min, double max) {
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

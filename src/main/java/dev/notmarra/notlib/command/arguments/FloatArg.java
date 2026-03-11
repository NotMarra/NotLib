package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class FloatArg extends Argument<Float> {
    @Nullable private Float min;
    @Nullable private Float max;

    public FloatArg(String name) {
        super(name);
    }

    public static FloatArg of(String name) { return new FloatArg(name); }
    public static FloatArg of(String name, Object description) {
        return (FloatArg) FloatArg.of(name).setDescription(description);
    }
    public static FloatArg of(String name, Consumer<Argument<Float>> executor) {
        return (FloatArg) FloatArg.of(name).onExecute(executor);
    }

    public FloatArg setMin(float min) {
        this.min = min;
        return this;
    }

    public FloatArg setMax(float max) {
        this.max = max;
        return this;
    }

    public FloatArg setRange(float min, float max) {
        this.min = min;
        this.max = max;
        return this;
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, Float> construct() {
        if (this.min != null && this.max != null) {
            return Commands.argument(this.name, FloatArgumentType.floatArg(this.min, this.max));
        } else if (this.min != null) {
            return Commands.argument(this.name, FloatArgumentType.floatArg(this.min));
        } else if (this.max != null) {
            return Commands.argument(this.name, FloatArgumentType.floatArg(this.max));
        } else {
            return Commands.argument(this.name, FloatArgumentType.floatArg());
        }
    }

    @Override
    public Float get() {
        return FloatArgumentType.getFloat(this.ctx, this.name);
    }
}
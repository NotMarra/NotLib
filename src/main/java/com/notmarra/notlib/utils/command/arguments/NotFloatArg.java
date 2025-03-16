package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import javax.annotation.Nullable;

public class NotFloatArg extends NotArgument<Float> {
    @Nullable private Float min;
    @Nullable private Float max;

    public NotFloatArg(String name) {
        super(name);
    }

    public NotFloatArg setMin(float min) {
        this.min = min;
        return this;
    }

    public NotFloatArg setMax(float max) {
        this.max = max;
        return this;
    }

    public NotFloatArg setRange(float min, float max) {
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
    public Float get(CommandContext<CommandSourceStack> ctx) {
        return FloatArgumentType.getFloat(ctx, this.name);
    }
}
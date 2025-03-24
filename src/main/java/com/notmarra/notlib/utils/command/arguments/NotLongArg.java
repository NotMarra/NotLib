package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class NotLongArg extends NotArgument<Long> {
    @Nullable private Long min;
    @Nullable private Long max;

    public NotLongArg(String name) {
        super(name);
    }

    public static NotLongArg of(String name) { return new NotLongArg(name); }
    public static NotLongArg of(String name, Consumer<NotArgument<Long>> executor) {
        return (NotLongArg)NotLongArg.of(name).onExecute(executor);
    }

    public NotLongArg setMin(long min) {
        this.min = min;
        return this;
    }

    public NotLongArg setMax(long max) {
        this.max = max;
        return this;
    }

    public NotLongArg setRange(long min, long max) {
        this.min = min;
        this.max = max;
        return this;
    }

    public RequiredArgumentBuilder<CommandSourceStack, Long> construct() {
        if (this.min != null && this.max != null) {
            return Commands.argument(this.name, LongArgumentType.longArg(this.min, this.max));
        } else if (this.min != null) {
            return Commands.argument(this.name, LongArgumentType.longArg(this.min));
        } else if (this.max != null) {
            return Commands.argument(this.name, LongArgumentType.longArg(this.max));
        } else {
            return Commands.argument(this.name, LongArgumentType.longArg());
        }
    }

    @Override
    public Long get() {
        return LongArgumentType.getLong(this.ctx, this.name);
    }
}


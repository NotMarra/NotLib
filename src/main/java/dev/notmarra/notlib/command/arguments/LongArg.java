package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.arguments.LongArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.function.Consumer;

import javax.annotation.Nullable;

public class LongArg extends Argument<Long> {
    @Nullable private Long min;
    @Nullable private Long max;

    public LongArg(String name) {
        super(name);
    }

    public static LongArg of(String name) { return new LongArg(name); }
    public static LongArg of(String name, Object description) {
        return (LongArg) LongArg.of(name).setDescription(description);
    }
    public static LongArg of(String name, Consumer<Argument<Long>> executor) {
        return (LongArg) LongArg.of(name).onExecute(executor);
    }

    public LongArg setMin(long min) {
        this.min = min;
        return this;
    }

    public LongArg setMax(long max) {
        this.max = max;
        return this;
    }

    public LongArg setRange(long min, long max) {
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


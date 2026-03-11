package dev.notmarra.notlib.command.arguments;

import java.util.function.Consumer;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

public class LiteralArg extends Argument<String> {
    public LiteralArg(String name) {
        super(name);
        this.isLiteral = true;
    }

    public static LiteralArg of(String name) { return new LiteralArg(name); }
    public static LiteralArg of(String name, Object description) {
        return (LiteralArg) LiteralArg.of(name).setDescription(description);
    }
    public static LiteralArg of(String name, Consumer<Argument<String>> executor) {
        return (LiteralArg) LiteralArg.of(name).onExecute(executor);
    }

    public LiteralArgumentBuilder<CommandSourceStack> construct() {
        return Commands.literal(this.name);
    }

    @Override
    public String get() {
        return this.name;
    }
}

package dev.notmarra.notlib.command.arguments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;

import dev.notmarra.notlib.chat.Text;
import dev.notmarra.notlib.command.Base;
import dev.notmarra.notlib.command.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public abstract class Argument<T> extends Base<Argument<T>> {
    public boolean isLiteral = false;

    public Argument(String name) {
        super(name);
    }

    /** Returns the root {@link Command} by walking up the parent chain. */
    public Command cmd() {
        Base<?> parent = this;
        while (parent.parent != null) parent = parent.parent;
        return (Command) parent;
    }

    /** Builds the Brigadier argument node. Implement to return the appropriate builder. */
    public abstract ArgumentBuilder<CommandSourceStack, ?> construct();

    /**
     * Returns the value of this argument from the current command context.
     * Only call this inside an executor.
     */
    public abstract T get();

    /** Returns the dot-notation path to this argument from the root, e.g. {@code "pay.amount"}. */
    public String getPath() {
        List<String> path = new ArrayList<>();
        path.add(name);
        Base<?> current = parent;
        while (current != null) {
            if (!(current instanceof Command)) path.add(current.name);
            current = current.parent;
        }
        Collections.reverse(path);
        return String.join(".", path);
    }

    /** Returns the help text for the subtree rooted at this argument. */
    public Text getHelp() {
        return cmd().getHelpFor(List.of(getPath()));
    }

    public String getSignature() {
        List<String> path = new ArrayList<>();
        path.add(name);
        Base<?> current = parent;
        while (current != null) {
            path.add(current.name);
            current = current.parent;
        }
        Collections.reverse(path);
        String command = path.get(0);
        List<String> subArgs = path.subList(1, path.size() - 1);
        return "/" + command + " " + String.join(" ", subArgs.stream().map(x -> "<" + x + ">").toList());
    }

    public String getSignatureWith(String... suffixArgs) {
        return getSignature() + " " + String.join(" ", List.of(suffixArgs).stream().map(x -> "<" + x + ">").toList());
    }

    @Override
    public Base<Argument<T>> onExecute(Consumer<Argument<T>> executor) {
        return super.onExecute(executor);
    }

    @SuppressWarnings({ "unchecked" })
    public CommandNode<CommandSourceStack> build() {
        ArgumentBuilder<CommandSourceStack, ?> arg = construct();

        for (String subArg : this.arguments.keySet()) {
            arg.then(this.arguments.get(subArg).build());
        }

        if (arg instanceof RequiredArgumentBuilder && !this.suggestions.isEmpty()) {
            ((RequiredArgumentBuilder<CommandSourceStack, ?>) arg).suggests(
                    (ctx, suggestionsBuilder) -> {
                        this.suggestions.forEach(suggestionsBuilder::suggest);
                        return suggestionsBuilder.buildFuture();
                    }
            );
        }

        arg.requires(source -> permission == null || source.getSender().hasPermission(permission));

        if (this.executor != null) {
            arg.executes(ctx -> {
                cmd().setContext(ctx);
                executor.accept(this);
                return 1;
            });
        } else {
            arg.executes(ctx -> {
                cmd().setContext(ctx);
                getHelp().sendTo(getPlayer());
                return 1;
            });
        }

        return arg.build();
    }
}
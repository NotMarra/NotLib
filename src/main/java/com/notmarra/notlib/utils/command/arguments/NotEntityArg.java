package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;

import java.util.function.Consumer;

import org.bukkit.entity.Entity;

public class NotEntityArg extends NotArgument<Entity> {
    public NotEntityArg(String name) {
        super(name);
    }

    public static NotEntityArg of(String name) { return new NotEntityArg(name); }
    public static NotEntityArg of(String name, Object description) {
        return (NotEntityArg)NotEntityArg.of(name).setDescription(description);
    }
    public static NotEntityArg of(String name, Consumer<NotArgument<Entity>> executor) {
        return (NotEntityArg)NotEntityArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.entity());
    }

    @Override
    public Entity get() {
        try {
            final EntitySelectorArgumentResolver resolver = ctx.getArgument(this.name, EntitySelectorArgumentResolver.class);
            return resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}

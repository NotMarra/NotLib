package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.function.Consumer;

public class NotEntitiesArg extends NotArgument<List<Entity>> {
    public NotEntitiesArg(String name) {
        super(name);
    }

    public static NotEntitiesArg of(String name) { return new NotEntitiesArg(name); }
    public static NotEntitiesArg of(String name, Object description) {
        return (NotEntitiesArg)NotEntitiesArg.of(name).setDescription(description);
    }
    public static NotEntitiesArg of(String name, Consumer<NotArgument<List<Entity>>> executor) {
        return (NotEntitiesArg)NotEntitiesArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.entities());
    }

    @Override
    public List<Entity> get() {
        try {
            final EntitySelectorArgumentResolver resolver = ctx.getArgument(this.name, EntitySelectorArgumentResolver.class);
            return resolver.resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            return List.of();
        }
    }
}

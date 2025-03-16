package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import org.bukkit.entity.Entity;

import java.util.List;

public class NotEntityArg extends NotArgument<List<Entity>> {
    public NotEntityArg(String name) {
        super(name);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.entity());
    }

    @Override
    public List<Entity> get(CommandContext<CommandSourceStack> ctx) {
        try {
            final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument(this.name, EntitySelectorArgumentResolver.class);
            return entitySelectorArgumentResolver.resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            return List.of();
        }
    }
}

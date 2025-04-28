package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

public class NotPlayerArg extends NotArgument<Player> {
    public NotPlayerArg(String name) {
        super(name);
    }

    public static NotPlayerArg of(String name) { return new NotPlayerArg(name); }
    public static NotPlayerArg of(String name, Object description) {
        return (NotPlayerArg)NotPlayerArg.of(name).setDescription(description);
    }
    public static NotPlayerArg of(String name, Consumer<NotArgument<Player>> executor) {
        return (NotPlayerArg)NotPlayerArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.player());
    }

    @Override
    public Player get() {
        try {
            final PlayerSelectorArgumentResolver resolver = ctx.getArgument(this.name, PlayerSelectorArgumentResolver.class);
            return resolver.resolve(ctx.getSource()).getFirst();
        } catch (CommandSyntaxException e) {
            return null;
        }
    }
}

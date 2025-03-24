package com.notmarra.notlib.utils.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class NotPlayersArg extends NotArgument<List<Player>> {
    public NotPlayersArg(String name) {
        super(name);
    }

    public static NotPlayersArg of(String name) { return new NotPlayersArg(name); }
    public static NotPlayersArg of(String name, Consumer<NotArgument<List<Player>>> executor) {
        return (NotPlayersArg)NotPlayersArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, PlayerSelectorArgumentResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.players());
    }

    @Override
    public List<Player> get() {
        try {
            final PlayerSelectorArgumentResolver resolver = ctx.getArgument(this.name, PlayerSelectorArgumentResolver.class);
            return resolver.resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            return List.of();
        }
    }
}

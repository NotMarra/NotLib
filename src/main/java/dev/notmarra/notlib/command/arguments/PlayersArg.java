package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;

import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.Consumer;

public class PlayersArg extends Argument<List<Player>> {
    public PlayersArg(String name) {
        super(name);
    }

    public static PlayersArg of(String name) { return new PlayersArg(name); }
    public static PlayersArg of(String name, Object description) {
        return (PlayersArg) PlayersArg.of(name).setDescription(description);
    }
    public static PlayersArg of(String name, Consumer<Argument<List<Player>>> executor) {
        return (PlayersArg) PlayersArg.of(name).onExecute(executor);
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

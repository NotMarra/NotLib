package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;

import java.util.function.Consumer;

import org.bukkit.entity.Player;

public class PlayerArg extends Argument<Player> {
    public PlayerArg(String name) {
        super(name);
    }

    public static PlayerArg of(String name) { return new PlayerArg(name); }
    public static PlayerArg of(String name, Object description) {
        return (PlayerArg) PlayerArg.of(name).setDescription(description);
    }
    public static PlayerArg of(String name, Consumer<Argument<Player>> executor) {
        return (PlayerArg) PlayerArg.of(name).onExecute(executor);
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

package dev.notmarra.notlib.command.arguments;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlayerProfilesArg extends Argument<List<PlayerProfile>> {
    public PlayerProfilesArg(String name) {
        super(name);
    }

    public static PlayerProfilesArg of(String name) { return new PlayerProfilesArg(name); }
    public static PlayerProfilesArg of(String name, Object description) {
        return (PlayerProfilesArg) PlayerProfilesArg.of(name).setDescription(description);
    }
    public static PlayerProfilesArg of(String name, Consumer<Argument<List<PlayerProfile>>> executor) {
        return (PlayerProfilesArg) PlayerProfilesArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, PlayerProfileListResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.playerProfiles());
    }

    @Override
    public List<PlayerProfile> get() {
        try {
            final PlayerProfileListResolver resolver = ctx.getArgument(this.name, PlayerProfileListResolver.class);
            return new ArrayList<>(resolver.resolve(ctx.getSource()));
        } catch (CommandSyntaxException e) {
            return List.of();
        }
    }
}

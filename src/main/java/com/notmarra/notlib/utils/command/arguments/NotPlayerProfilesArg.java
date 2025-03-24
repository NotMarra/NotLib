package com.notmarra.notlib.utils.command.arguments;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class NotPlayerProfilesArg extends NotArgument<Collection<PlayerProfile>> {
    public NotPlayerProfilesArg(String name) {
        super(name);
    }

    public static NotPlayerProfilesArg of(String name) { return new NotPlayerProfilesArg(name); }
    public static NotPlayerProfilesArg of(String name, Consumer<NotArgument<Collection<PlayerProfile>>> executor) {
        return (NotPlayerProfilesArg)NotPlayerProfilesArg.of(name).onExecute(executor);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, PlayerProfileListResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.playerProfiles());
    }

    @Override
    public Collection<PlayerProfile> get() {
        try {
            final PlayerProfileListResolver resolver = ctx.getArgument(this.name, PlayerProfileListResolver.class);
            return resolver.resolve(ctx.getSource());
        } catch (CommandSyntaxException e) {
            return List.of();
        }
    }
}

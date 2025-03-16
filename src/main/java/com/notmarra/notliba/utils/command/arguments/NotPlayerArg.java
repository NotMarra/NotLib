package com.notmarra.notliba.utils.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class NotPlayerArg extends NotArgument<List<Player>> {
    public NotPlayerArg(String name) {
        super(name);
    }

    @Override
    public RequiredArgumentBuilder<CommandSourceStack, EntitySelectorArgumentResolver> construct() {
        return Commands.argument(this.name, ArgumentTypes.entity());
    }

    @Override
    public List<Player> get(CommandContext<CommandSourceStack> ctx) {
        try {
            final EntitySelectorArgumentResolver entitySelectorArgumentResolver = ctx.getArgument(this.name, EntitySelectorArgumentResolver.class);
            List<Entity> entities = entitySelectorArgumentResolver.resolve(ctx.getSource());
            List<Player> players = new ArrayList<>();
            for (Entity entity : entities) {
                if (entity instanceof Player player) {
                    players.add(player);
                }
            }
            return players;
        } catch (CommandSyntaxException e) {
            return List.of();
        }
    }
}

package dev.notmarra.notlib.command.arguments;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;

import java.util.function.Consumer;

import org.bukkit.entity.Entity;

public class EntityArg extends Argument<Entity> {
    public EntityArg(String name) {
        super(name);
    }

    public static EntityArg of(String name) { return new EntityArg(name); }
    public static EntityArg of(String name, Object description) {
        return (EntityArg) EntityArg.of(name).setDescription(description);
    }
    public static EntityArg of(String name, Consumer<Argument<Entity>> executor) {
        return (EntityArg) EntityArg.of(name).onExecute(executor);
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

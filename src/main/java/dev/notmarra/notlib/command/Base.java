package dev.notmarra.notlib.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import dev.notmarra.notlib.command.arguments.*;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import javax.annotation.Nullable;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Base<T extends Base<T>> {
    public Base<?> parent;
    public final String name;
    public Object description;
    public HashMap<String, Argument<Object>> arguments = new HashMap<>();
    public Consumer<T> executor;
    public List<String> suggestions = List.of();
    @Nullable public String permission;
    public CommandContext<CommandSourceStack> ctx;

    public Base(String name) {
        this.name = name;
    }

    public Player getPlayer() {
        Entity entity = getEntity();
        return entity instanceof Player ? (Player) entity : null;
    }

    public Entity getEntity() {
        return this.ctx.getSource().getExecutor();
    }

    public CommandSender getSender() {
        return this.ctx.getSource().getSender();
    }

    public Base<T> setContext(CommandContext<CommandSourceStack> ctx) {
        this.ctx = ctx;
        for (String arg : this.arguments.keySet()) {
            this.arguments.get(arg).setContext(ctx);
        }
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public Argument addArg(Argument arg) {
        arg.parent = this;
        this.arguments.put(arg.name, arg);
        return arg;
    }

    public Argument<?> getArg(String name) {
        List<String> path = List.of(name.split("\\."));
        Argument<?> arg = this.arguments.get(path.getFirst());
        for (int i = 1; i < path.size(); i++) {
            arg = arg.arguments.get(path.get(i));
        }
        return arg;
    }

    public Base<T> onExecute(Consumer<T> executor) {
        this.executor = executor;
        return this;
    }

    // NOTE: description can be ChatF, Component, String or other...
    public Base<T> setDescription(Object description) {
        this.description = description;
        return this;
    }

    public Base<T> setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
        return this;
    }

    public Base<T> setPermission(String permission) {
        this.permission = permission;
        return this;
    }

    public @Nullable String getPermission() {
        return this.permission;
    }

    // --- Argument helpers (one method per type, configure via fluent chaining) ---

    public BoolArg boolArg(String name)             { return (BoolArg) addArg(new BoolArg(name)); }
    public DoubleArg doubleArg(String name)         { return (DoubleArg) addArg(new DoubleArg(name)); }
    public EntitiesArg entitiesArg(String name)     { return (EntitiesArg) addArg(new EntitiesArg(name)); }
    public EntityArg entityArg(String name)         { return (EntityArg) addArg(new EntityArg(name)); }
    public FloatArg floatArg(String name)           { return (FloatArg) addArg(new FloatArg(name)); }
    public GreedyStringArg greedyStringArg(String name) { return (GreedyStringArg) addArg(new GreedyStringArg(name)); }
    public IntArg intArg(String name)               { return (IntArg) addArg(new IntArg(name)); }
    public LiteralArg literalArg(String name)       { return (LiteralArg) addArg(new LiteralArg(name)); }
    public LongArg longArg(String name)             { return (LongArg) addArg(new LongArg(name)); }
    public PlayerArg playerArg(String name)         { return (PlayerArg) addArg(new PlayerArg(name)); }
    public PlayerProfilesArg playerProfilesArg(String name) { return (PlayerProfilesArg) addArg(new PlayerProfilesArg(name)); }
    public PlayersArg playersArg(String name)       { return (PlayersArg) addArg(new PlayersArg(name)); }
    public StringArg stringArg(String name)         { return (StringArg) addArg(new StringArg(name)); }

    public Map<String, Object> getValues() {
        Map<String, Object> values = new HashMap<>();
        for (String arg : this.arguments.keySet()) {
            values.put(arg, this.arguments.get(arg).get());
            values.putAll(this.arguments.get(arg).getValues());
        }
        return values;
    }

    public abstract CommandNode<CommandSourceStack> build();
}
package dev.notmarra.notlib.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import dev.notmarra.notlib.command.arguments.Argument;
import dev.notmarra.notlib.command.arguments.BoolArg;
import dev.notmarra.notlib.command.arguments.DoubleArg;
import dev.notmarra.notlib.command.arguments.EntitiesArg;
import dev.notmarra.notlib.command.arguments.EntityArg;
import dev.notmarra.notlib.command.arguments.FloatArg;
import dev.notmarra.notlib.command.arguments.GreedyStringArg;
import dev.notmarra.notlib.command.arguments.IntArg;
import dev.notmarra.notlib.command.arguments.LiteralArg;
import dev.notmarra.notlib.command.arguments.LongArg;
import dev.notmarra.notlib.command.arguments.PlayerArg;
import dev.notmarra.notlib.command.arguments.PlayerProfilesArg;
import dev.notmarra.notlib.command.arguments.PlayersArg;
import dev.notmarra.notlib.command.arguments.StringArg;

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
        return entity instanceof Player ? (Player)entity : null;
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
    public Base<T> addArg(Argument arg) {
        arg.parent = this;
        this.arguments.put(arg.name, arg);
        return this;
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

    public BoolArg boolArg(String name) { BoolArg arg = BoolArg.of(name); this.addArg(arg); return arg; }
    public BoolArg boolArg(String name, Object description) { BoolArg arg = BoolArg.of(name, description); this.addArg(arg); return arg; }
    public BoolArg boolArg(String name, Object description, Consumer<Argument<Boolean>> executor) { BoolArg arg = (BoolArg)BoolArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public BoolArg boolArg(String name, Consumer<Argument<Boolean>> executor) { BoolArg arg = BoolArg.of(name, executor); this.addArg(arg); return arg; }

    public DoubleArg doubleArg(String name) { DoubleArg arg = DoubleArg.of(name); this.addArg(arg); return arg; }
    public DoubleArg doubleArg(String name, Object description) { DoubleArg arg = DoubleArg.of(name, description); this.addArg(arg); return arg; }
    public DoubleArg doubleArg(String name, Object description, Consumer<Argument<Double>> executor) { DoubleArg arg = (DoubleArg)DoubleArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public DoubleArg doubleArg(String name, Consumer<Argument<Double>> executor) { DoubleArg arg = DoubleArg.of(name, executor); this.addArg(arg); return arg; }

    public EntitiesArg entitiesArg(String name) { EntitiesArg arg = EntitiesArg.of(name); this.addArg(arg); return arg; }
    public EntitiesArg entitiesArg(String name, Object description) { EntitiesArg arg = EntitiesArg.of(name, description); this.addArg(arg); return arg; }
    public EntitiesArg entitiesArg(String name, Object description, Consumer<Argument<List<Entity>>> executor) { EntitiesArg arg = (EntitiesArg)EntitiesArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public EntitiesArg entitiesArg(String name, Consumer<Argument<List<Entity>>> executor) { EntitiesArg arg = EntitiesArg.of(name, executor); this.addArg(arg); return arg; }

    public EntityArg entityArg(String name) { EntityArg arg = EntityArg.of(name); this.addArg(arg); return arg; }
    public EntityArg entityArg(String name, Object description) { EntityArg arg = EntityArg.of(name, description); this.addArg(arg); return arg; }
    public EntityArg entityArg(String name, Object description, Consumer<Argument<Entity>> executor) { EntityArg arg = (EntityArg)EntityArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public EntityArg entityArg(String name, Consumer<Argument<Entity>> executor) { EntityArg arg = EntityArg.of(name, executor); this.addArg(arg); return arg; }

    public FloatArg floatArg(String name) { FloatArg arg = FloatArg.of(name); this.addArg(arg); return arg; }
    public FloatArg floatArg(String name, Object description) { FloatArg arg = FloatArg.of(name, description); this.addArg(arg); return arg; }
    public FloatArg floatArg(String name, Object description, Consumer<Argument<Float>> executor) { FloatArg arg = (FloatArg)FloatArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public FloatArg floatArg(String name, Consumer<Argument<Float>> executor) { FloatArg arg = FloatArg.of(name, executor); this.addArg(arg); return arg; }

    public GreedyStringArg greedyStringArg(String name) { GreedyStringArg arg = GreedyStringArg.of(name); this.addArg(arg); return arg; }
    public GreedyStringArg greedyStringArg(String name, Object description) { GreedyStringArg arg = GreedyStringArg.of(name, description); this.addArg(arg); return arg; }
    public GreedyStringArg greedyStringArg(String name, Object description, Consumer<Argument<String>> executor) { GreedyStringArg arg = (GreedyStringArg)GreedyStringArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public GreedyStringArg greedyStringArg(String name, Consumer<Argument<String>> executor) { GreedyStringArg arg = GreedyStringArg.of(name, executor); this.addArg(arg); return arg; }

    public IntArg intArg(String name) { IntArg arg = IntArg.of(name); this.addArg(arg); return arg; }
    public IntArg intArg(String name, Object description) { IntArg arg = IntArg.of(name, description); this.addArg(arg); return arg; }
    public IntArg intArg(String name, Object description, Consumer<Argument<Integer>> executor) { IntArg arg = (IntArg)IntArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public IntArg intArg(String name, Consumer<Argument<Integer>> executor) { IntArg arg = IntArg.of(name, executor); this.addArg(arg); return arg; }

    public LiteralArg literalArg(String name) { LiteralArg arg = LiteralArg.of(name); this.addArg(arg); return arg; }
    public LiteralArg literalArg(String name, Object description) { LiteralArg arg = LiteralArg.of(name, description); this.addArg(arg); return arg; }
    public LiteralArg literalArg(String name, Object description, Consumer<Argument<String>> executor) { LiteralArg arg = (LiteralArg)LiteralArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public LiteralArg literalArg(String name, Consumer<Argument<String>> executor) { LiteralArg arg = LiteralArg.of(name, executor); this.addArg(arg); return arg; }

    public LongArg longArg(String name) { LongArg arg = LongArg.of(name); this.addArg(arg); return arg; }
    public LongArg longArg(String name, Object description) { LongArg arg = LongArg.of(name, description); this.addArg(arg); return arg; }
    public LongArg longArg(String name, Object description, Consumer<Argument<Long>> executor) { LongArg arg = (LongArg)LongArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public LongArg longArg(String name, Consumer<Argument<Long>> executor) { LongArg arg = LongArg.of(name, executor); this.addArg(arg); return arg; }

    public PlayerArg playerArg(String name) { PlayerArg arg = PlayerArg.of(name); this.addArg(arg); return arg; }
    public PlayerArg playerArg(String name, Object description) { PlayerArg arg = PlayerArg.of(name, description); this.addArg(arg); return arg; }
    public PlayerArg playerArg(String name, Object description, Consumer<Argument<Player>> executor) { PlayerArg arg = (PlayerArg)PlayerArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public PlayerArg playerArg(String name, Consumer<Argument<Player>> executor) { PlayerArg arg = PlayerArg.of(name, executor); this.addArg(arg); return arg; }

    public PlayerProfilesArg playerProfilesArg(String name) { PlayerProfilesArg arg = PlayerProfilesArg.of(name); this.addArg(arg); return arg; }
    public PlayerProfilesArg playerProfilesArg(String name, Object description) { PlayerProfilesArg arg = PlayerProfilesArg.of(name, description); this.addArg(arg); return arg; }
    public PlayerProfilesArg playerProfilesArg(String name, Object description, Consumer<Argument<List<PlayerProfile>>> executor) { PlayerProfilesArg arg = (PlayerProfilesArg)PlayerProfilesArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public PlayerProfilesArg playerProfilesArg(String name, Consumer<Argument<List<PlayerProfile>>> executor) { PlayerProfilesArg arg = PlayerProfilesArg.of(name, executor); this.addArg(arg); return arg; }

    public PlayersArg playersArg(String name) { PlayersArg arg = PlayersArg.of(name); this.addArg(arg); return arg; }
    public PlayersArg playersArg(String name, Object description) { PlayersArg arg = PlayersArg.of(name, description); this.addArg(arg); return arg; }
    public PlayersArg playersArg(String name, Object description, Consumer<Argument<List<Player>>> executor) { PlayersArg arg = (PlayersArg)PlayersArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public PlayersArg playersArg(String name, Consumer<Argument<List<Player>>> executor) { PlayersArg arg = PlayersArg.of(name, executor); this.addArg(arg); return arg; }

    public StringArg stringArg(String name) { StringArg arg = StringArg.of(name); this.addArg(arg); return arg; }
    public StringArg stringArg(String name, Object description) { StringArg arg = StringArg.of(name, description); this.addArg(arg); return arg; }
    public StringArg stringArg(String name, Object description, Consumer<Argument<String>> executor) { StringArg arg = (StringArg)StringArg.of(name, description).onExecute(executor); this.addArg(arg); return arg; }
    public StringArg stringArg(String name, Consumer<Argument<String>> executor) { StringArg arg = StringArg.of(name, executor); this.addArg(arg); return arg; }

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

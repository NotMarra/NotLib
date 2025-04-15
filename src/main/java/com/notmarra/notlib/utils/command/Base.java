package com.notmarra.notlib.utils.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.notmarra.notlib.utils.command.arguments.NotArgument;
import com.notmarra.notlib.utils.command.arguments.NotBoolArg;
import com.notmarra.notlib.utils.command.arguments.NotDoubleArg;
import com.notmarra.notlib.utils.command.arguments.NotEntitiesArg;
import com.notmarra.notlib.utils.command.arguments.NotEntityArg;
import com.notmarra.notlib.utils.command.arguments.NotFloatArg;
import com.notmarra.notlib.utils.command.arguments.NotGreedyStringArg;
import com.notmarra.notlib.utils.command.arguments.NotIntArg;
import com.notmarra.notlib.utils.command.arguments.NotLiteralArg;
import com.notmarra.notlib.utils.command.arguments.NotLongArg;
import com.notmarra.notlib.utils.command.arguments.NotPlayerArg;
import com.notmarra.notlib.utils.command.arguments.NotPlayerProfilesArg;
import com.notmarra.notlib.utils.command.arguments.NotPlayersArg;
import com.notmarra.notlib.utils.command.arguments.NotStringArg;

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
    public HashMap<String, NotArgument<Object>> arguments = new HashMap<>();
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
    public Base<T> addArg(NotArgument arg) {
        arg.parent = this;
        this.arguments.put(arg.name, arg);
        return this;
    }

    public NotArgument<?> getArg(String name) {
        List<String> path = List.of(name.split("\\."));
        NotArgument<?> arg = this.arguments.get(path.get(0));
        for (int i = 1; i < path.size(); i++) {
            arg = arg.arguments.get(path.get(i));
        }
        return arg;
    }

    public Base<T> onExecute(Consumer<T> executor) {
        this.executor = executor;
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

    public NotBoolArg boolArg(String name) { NotBoolArg arg = NotBoolArg.of(name); this.addArg(arg); return arg; }
    public NotBoolArg boolArg(String name, Consumer<NotArgument<Boolean>> executor) { NotBoolArg arg = NotBoolArg.of(name, executor); this.addArg(arg); return arg; }

    public NotDoubleArg doubleArg(String name) { NotDoubleArg arg = NotDoubleArg.of(name); this.addArg(arg); return arg; }
    public NotDoubleArg doubleArg(String name, Consumer<NotArgument<Double>> executor) { NotDoubleArg arg = NotDoubleArg.of(name, executor); this.addArg(arg); return arg; }

    public NotEntitiesArg entitiesArg(String name) { NotEntitiesArg arg = NotEntitiesArg.of(name); this.addArg(arg); return arg; }
    public NotEntitiesArg entitiesArg(String name, Consumer<NotArgument<List<Entity>>> executor) { NotEntitiesArg arg = NotEntitiesArg.of(name, executor); this.addArg(arg); return arg; }

    public NotEntityArg entityArg(String name) { NotEntityArg arg = NotEntityArg.of(name); this.addArg(arg); return arg; }
    public NotEntityArg entityArg(String name, Consumer<NotArgument<Entity>> executor) { NotEntityArg arg = NotEntityArg.of(name, executor); this.addArg(arg); return arg; }

    public NotFloatArg floatArg(String name) { NotFloatArg arg = NotFloatArg.of(name); this.addArg(arg); return arg; }
    public NotFloatArg floatArg(String name, Consumer<NotArgument<Float>> executor) { NotFloatArg arg = NotFloatArg.of(name, executor); this.addArg(arg); return arg; }

    public NotGreedyStringArg greedyStringArg(String name) { NotGreedyStringArg arg = NotGreedyStringArg.of(name); this.addArg(arg); return arg; }
    public NotGreedyStringArg greedyStringArg(String name, Consumer<NotArgument<String>> executor) { NotGreedyStringArg arg = NotGreedyStringArg.of(name, executor); this.addArg(arg); return arg; }

    public NotIntArg intArg(String name) { NotIntArg arg = NotIntArg.of(name); this.addArg(arg); return arg; }
    public NotIntArg intArg(String name, Consumer<NotArgument<Integer>> executor) { NotIntArg arg = NotIntArg.of(name, executor); this.addArg(arg); return arg; }

    public NotLiteralArg literalArg(String name) { NotLiteralArg arg = NotLiteralArg.of(name); this.addArg(arg); return arg; }
    public NotLiteralArg literalArg(String name, Consumer<NotArgument<String>> executor) { NotLiteralArg arg = NotLiteralArg.of(name, executor); this.addArg(arg); return arg; }

    public NotLongArg longArg(String name) { NotLongArg arg = NotLongArg.of(name); this.addArg(arg); return arg; }
    public NotLongArg longArg(String name, Consumer<NotArgument<Long>> executor) { NotLongArg arg = NotLongArg.of(name, executor); this.addArg(arg); return arg; }

    public NotPlayerArg playerArg(String name) { NotPlayerArg arg = NotPlayerArg.of(name); this.addArg(arg); return arg; }
    public NotPlayerArg playerArg(String name, Consumer<NotArgument<Player>> executor) { NotPlayerArg arg = NotPlayerArg.of(name, executor); this.addArg(arg); return arg; }

    public NotPlayerProfilesArg playerProfilesArg(String name) { NotPlayerProfilesArg arg = NotPlayerProfilesArg.of(name); this.addArg(arg); return arg; }
    public NotPlayerProfilesArg playerProfilesArg(String name, Consumer<NotArgument<List<PlayerProfile>>> executor) { NotPlayerProfilesArg arg = NotPlayerProfilesArg.of(name, executor); this.addArg(arg); return arg; }

    public NotPlayersArg playersArg(String name) { NotPlayersArg arg = NotPlayersArg.of(name); this.addArg(arg); return arg; }
    public NotPlayersArg playersArg(String name, Consumer<NotArgument<List<Player>>> executor) { NotPlayersArg arg = NotPlayersArg.of(name, executor); this.addArg(arg); return arg; }

    public NotStringArg stringArg(String name) { NotStringArg arg = NotStringArg.of(name); this.addArg(arg); return arg; }
    public NotStringArg stringArg(String name, Consumer<NotArgument<String>> executor) { NotStringArg arg = NotStringArg.of(name, executor); this.addArg(arg); return arg; }

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

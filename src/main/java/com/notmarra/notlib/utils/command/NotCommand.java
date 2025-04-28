package com.notmarra.notlib.utils.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotConverter;
import com.notmarra.notlib.utils.command.arguments.NotArgument;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class NotCommand extends Base<NotCommand> {
    public NotCommand(String name) { super(name); }
    public static NotCommand of(String name) { return new NotCommand(name); }
    public static NotCommand of(String name, Object description) { return (NotCommand)NotCommand.of(name).setDescription(description); }
    public static NotCommand of(String name, Consumer<NotCommand> executor) { return (NotCommand)NotCommand.of(name).onExecute(executor); }

    public Object get(String path) {
        Base<?> current = this; 

        for (String part : path.split("\\.")) {
            if (current.arguments.containsKey(part)) {
                current = current.arguments.get(part);
            } else {
                return null;
            }
        }

        if (current instanceof NotArgument) {
            return ((NotArgument<?>) current).get();
        }

        return null;
    }

    public ChatF getHelpFor(List<String> filter) {
        ChatF help = ChatF.empty();
        
        help.append(ChatF.ofBold("/" + this.name, ChatF.C_YELLOW));
        
        if (this.description != null) {
            help.append(ChatF.of(" - ").append(this.description));
        }
        
        if (!this.arguments.isEmpty()) {
            HashMap<String, NotArgument<Object>> filtered = new HashMap<>();
            for (NotArgument<Object> arg : this.arguments.values()) {
                String argPath = arg.getPath();
                if (!filter.isEmpty() && filter.stream().anyMatch(x -> argPath.startsWith(x) || x.startsWith(argPath))) {
                    filtered.put(arg.name, arg);
                }
            }

            appendArgumentsTree(help, filtered, filter, 0, "");
        }

        return help;
    }

    public ChatF getHelp() {
        ChatF help = ChatF.empty();
        
        help.append(ChatF.ofBold("/" + this.name, ChatF.C_YELLOW));
        
        if (this.description != null) {
            help.append(ChatF.of(" - ").append(this.description));
        }
        
        if (!this.arguments.isEmpty()) {
            appendArgumentsTree(help, this.arguments, List.of(), 0, "");
        }
        
        return help;
    }
    
    private void appendArgumentsTree(ChatF help, HashMap<String, NotArgument<Object>> args, List<String> filter, int depth, String prefix) {
        List<Map.Entry<String, NotArgument<Object>>> sortedArgs = new ArrayList<>(args.entrySet());
        sortedArgs.sort(Map.Entry.comparingByKey());

        sortedArgs.removeIf(x -> {
            String argPath = x.getValue().getPath();
            return !filter.isEmpty() && !filter.stream().anyMatch(y -> argPath.startsWith(y) || y.startsWith(argPath));
        });
        
        for (int i = 0; i < sortedArgs.size(); i++) {
            NotArgument<Object> arg = sortedArgs.get(i).getValue();

            boolean isLast = (i == sortedArgs.size() - 1);
            
            String branchSymbol = isLast ? "└" : "├";
            String nextPrefix = prefix + (isLast ? "  " : "│");
            
            help.append(ChatF.newline().append(prefix + branchSymbol, ChatF.C_GRAY));

            String argName = arg.name;
            if (!arg.isLiteral) argName = "<" + argName + ">";
            help.append(ChatF.ofBold(argName, ChatF.C_GREEN));
            
            if (arg.description != null) {
                help.append(ChatF.of(" - ").append(arg.description));
            }
            
            if (!arg.arguments.isEmpty()) {
                appendArgumentsTree(help, arg.arguments, filter, depth + 1, nextPrefix);
            }
        }
    }

    private <T> T _getValue(String path, Class<T> clazz) {
        Object value = get(path);
        return value != null && clazz.isInstance(value) ? clazz.cast(value) : null;
    }
    private <T> List<T> _getListValue(String path, Class<T> clazz) {
        Object value = get(path);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                .filter(o -> clazz.isInstance(o))
                .map(o -> clazz.cast(o))
                .toList();
        }
        return List.of();
    }
    public String getStringV(String path) { return NotConverter.toString(get(path)); }
    public Integer getIntegerV(String path) { return NotConverter.toInteger(get(path)); }
    public Long getLongV(String path) { return NotConverter.toLong(get(path)); }
    public Float getFloatV(String path) { return NotConverter.toFloat(get(path)); }
    public Double getDoubleV(String path) { return NotConverter.toDouble(get(path)); }
    public Boolean getBooleanV(String path) { return NotConverter.toBoolean(get(path)); }
    public Player getPlayerV(String path) { return _getValue(path, Player.class); }
    public List<Player> getPlayersV(String path) { return _getListValue(path, Player.class); }
    public Entity getEntityV(String path) { return _getValue(path, Entity.class); }
    public List<Entity> getEntitiesV(String path) { return _getListValue(path, Entity.class); }
    public List<PlayerProfile> getPlayerProfilesV(String path) { return _getListValue(path, PlayerProfile.class); }

    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(this.name);
        for (String arg : this.arguments.keySet()) {
            cmd.then(this.arguments.get(arg).build());
        }
        cmd.requires(source -> {
            if (this.permission != null) {
                return source.getSender().hasPermission(this.permission);
            }
            return true;
        });
        if (executor != null) {
            cmd.executes(ctx -> {
                setContext(ctx);
                executor.accept(this);
                return 1;
            });
        }
        return cmd.build();
    }
}

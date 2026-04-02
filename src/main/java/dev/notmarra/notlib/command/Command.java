package dev.notmarra.notlib.command;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.notmarra.notlib.chat.Colors;
import dev.notmarra.notlib.chat.Text;
import dev.notmarra.notlib.command.arguments.Argument;
import dev.notmarra.notlib.utils.Converter;
import dev.notmarra.notlib.extensions.CommandGroup;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class Command extends Base<Command> {

    public Command(String name) { super(name); }

    /** Creates a command with the given name. */
    public static Command of(String name) { return new Command(name); }

    /** Creates a command with a name and description. */
    public static Command of(String name, Object description) {
        return Command.of(name).setDescription(description);
    }

    // Covariant overrides — ensure fluent methods return Command, not Base<Command>
    @Override public Command onExecute(java.util.function.Consumer<Command> executor) { return (Command) super.onExecute(executor); }
    @Override public Command setDescription(Object description)                       { return (Command) super.setDescription(description); }
    @Override public Command setPermission(String permission)                         { return (Command) super.setPermission(permission); }
    @Override public Command setSuggestions(java.util.List<String> suggestions)       { return (Command) super.setSuggestions(suggestions); }

    // --- Value accessors ---

    /**
     * Returns the raw value of the argument at the given dot-notation path,
     * e.g. {@code "pay.amount"}. Returns {@code null} if the path does not exist.
     */
    public Object get(String path) {
        Base<?> current = this;
        for (String part : path.split("\\.")) {
            if (current.arguments.containsKey(part)) {
                current = current.arguments.get(part);
            } else {
                return null;
            }
        }
        return current instanceof Argument ? ((Argument<?>) current).get() : null;
    }

    public String  getStringV(String path)  { return Converter.toString(get(path)); }
    public Integer getIntegerV(String path) { return Converter.toInteger(get(path)); }
    public Long    getLongV(String path)    { return Converter.toLong(get(path)); }
    public Float   getFloatV(String path)   { return Converter.toFloat(get(path)); }
    public Double  getDoubleV(String path)  { return Converter.toDouble(get(path)); }
    public Boolean getBooleanV(String path) { return Converter.toBoolean(get(path)); }

    public Player getPlayerV(String path)   { return _getValue(path, Player.class); }
    public Entity getEntityV(String path)   { return _getValue(path, Entity.class); }

    public List<Player>        getPlayersV(String path)        { return _getListValue(path, Player.class); }
    public List<Entity>        getEntitiesV(String path)       { return _getListValue(path, Entity.class); }
    public List<PlayerProfile> getPlayerProfilesV(String path) { return _getListValue(path, PlayerProfile.class); }

    private <V> V _getValue(String path, Class<V> clazz) {
        Object value = get(path);
        return value != null && clazz.isInstance(value) ? clazz.cast(value) : null;
    }

    private <V> List<V> _getListValue(String path, Class<V> clazz) {
        Object value = get(path);
        if (value instanceof List) {
            return ((List<?>) value).stream()
                    .filter(clazz::isInstance)
                    .map(clazz::cast)
                    .toList();
        }
        return List.of();
    }

    // --- Help ---

    public Text getHelp() {
        Text help = Text.empty();
        help.append(Text.ofBold("/" + this.name, Colors.YELLOW.get()));
        if (this.description != null) help.append(Text.of(" - ").append(this.description));
        if (!this.arguments.isEmpty()) appendArgumentsTree(help, this.arguments, List.of(), 0, "");
        return help;
    }

    public Text getHelpFor(List<String> filter) {
        Text help = Text.empty();
        help.append(Text.ofBold("/" + this.name, Colors.YELLOW.get()));
        if (this.description != null) help.append(Text.of(" - ").append(this.description));
        if (!this.arguments.isEmpty()) {
            HashMap<String, Argument<Object>> filtered = new HashMap<>();
            for (Argument<Object> arg : this.arguments.values()) {
                String argPath = arg.getPath();
                if (!filter.isEmpty() && filter.stream().anyMatch(x -> argPath.startsWith(x) || x.startsWith(argPath))) {
                    filtered.put(arg.name, arg);
                }
            }
            appendArgumentsTree(help, filtered, filter, 0, "");
        }
        return help;
    }

    private void appendArgumentsTree(Text help, HashMap<String, Argument<Object>> args, List<String> filter, int depth, String prefix) {
        List<Map.Entry<String, Argument<Object>>> sorted = new ArrayList<>(args.entrySet());
        sorted.sort(Map.Entry.comparingByKey());
        sorted.removeIf(x -> {
            String argPath = x.getValue().getPath();
            return !filter.isEmpty() && filter.stream().noneMatch(y -> argPath.startsWith(y) || y.startsWith(argPath));
        });

        for (int i = 0; i < sorted.size(); i++) {
            Argument<Object> arg = sorted.get(i).getValue();
            boolean isLast = (i == sorted.size() - 1);
            String branch = isLast ? "└" : "├";
            String nextPrefix = prefix + (isLast ? "  " : "│");

            help.append(Text.newline().append(prefix + branch, Colors.GRAY.get()));

            String argName = arg.isLiteral ? arg.name : "<" + arg.name + ">";
            help.append(Text.ofBold(argName, Colors.GREEN.get()));
            if (arg.description != null) help.append(Text.of(" - ").append(arg.description));
            if (!arg.arguments.isEmpty()) appendArgumentsTree(help, arg.arguments, filter, depth + 1, nextPrefix);
        }
    }

    // --- Build ---

    /**
     * Builds the Brigadier {@link LiteralCommandNode} ready for registration.
     * Called automatically by {@link CommandGroup#register()}.
     */
    public LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(this.name);
        for (String arg : this.arguments.keySet()) {
            cmd.then(this.arguments.get(arg).build());
        }
        cmd.requires(source -> permission == null || source.getSender().hasPermission(permission));
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
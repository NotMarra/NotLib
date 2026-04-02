package dev.notmarra.notlib.extensions;

import dev.notmarra.notlib.command.Command;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.List;

/**
 * A container for commands registered into Minecraft via the Paper Brigadier API.
 *
 * <p>Extend this class, implement {@link #notCommands()}, and call
 * {@link #register()} in your plugin's {@code onEnable()}.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * public class AdminCommands extends CommandGroup {
 *     public AdminCommands(NotPlugin plugin) { super(plugin); }
 *
 *     @Override
 *     public List<Command> notCommands() {
 *         return List.of(
 *             Command.of("kick", "Kick a player")
 *                 .setPermission("admin.kick")
 *                 .playerArg("target")
 *                     .onExecute(arg -> {
 *                         arg.cmd().getPlayerV("target").kick(Component.text("You were kicked."));
 *                     })
 *         );
 *     }
 * }
 *
 * // In onEnable():
 * new AdminCommands(this).register();
 * }</pre>
 *
 * <p>If your group also needs config, extend {@link ConfigurableCommandGroup} instead.
 */
public abstract class CommandGroup {
    protected final NotPlugin plugin;
    private boolean isRegistered = false;

    public CommandGroup(NotPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Returns the list of commands this group registers.
     *
     * @return list of {@link Command} instances
     */
    public abstract List<Command> notCommands();

    /**
     * Called just before commands are registered with the server.
     * Override to set up any dependencies or state your commands need.
     */
    public void onRegister() {}

    /**
     * Registers all commands from {@link #notCommands()} into the Paper Brigadier API.
     * Safe to call multiple times — registration only happens once.
     */
    public void register() {
        if (isRegistered) {
            plugin.getLogger().warning(getClass().getSimpleName() + " is already registered, skipping.");
            return;
        }
        isRegistered = true;
        onRegister();
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands ->
                notCommands().forEach(cmd -> commands.registrar().register(cmd.build()))
        );
    }
}
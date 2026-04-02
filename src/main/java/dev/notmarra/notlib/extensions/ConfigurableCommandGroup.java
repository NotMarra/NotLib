package dev.notmarra.notlib.extensions;

import dev.notmarra.notlib.command.Command;

import java.util.List;

/**
 * A {@link CommandGroup} that also participates in NotLib's config system.
 *
 * <p>Use this instead of {@link CommandGroup} when your commands need to read
 * values from a config file. Extends {@link Configurable} directly, so the
 * {@code plugin} field, {@link #getConfig}, {@link #isEnabled}, {@link #onConfigReload}
 * and all other config helpers are available out of the box.
 *
 * <p>Registration is skipped automatically if {@link #isEnabled()} returns {@code false}.
 *
 * <h3>Example:</h3>
 * <pre>{@code
 * public class ShopCommands extends ConfigurableCommandGroup {
 *     public ShopCommands(NotPlugin plugin) { super(plugin); }
 *
 *     @Override
 *     public List<String> getConfigPaths() {
 *         return List.of("shop.yml");
 *     }
 *
 *     @Override
 *     public List<Command> notCommands() {
 *         int maxItems = getFirstConfig().getInt("max-items");
 *         return List.of(
 *             Command.of("shop")
 *                 .onExecute(arg -> { ... })
 *         );
 *     }
 * }
 * }</pre>
 */
public abstract class ConfigurableCommandGroup extends Configurable {
    private boolean isRegistered = false;

    public ConfigurableCommandGroup(NotPlugin plugin) {
        super(plugin); // Configurable owns the plugin field
    }

    /** Returns the list of commands this group registers. */
    public abstract List<Command> notCommands();

    /**
     * Called just before commands are registered with the server.
     * Override to set up any state your commands need.
     */
    public void onRegister() {}

    /**
     * Registers all commands from {@link #notCommands()} into the Paper Brigadier API.
     * Skips registration if {@link #isEnabled()} returns {@code false}.
     * Safe to call multiple times — registration only happens once.
     */
    public void register() {
        if (isRegistered) {
            plugin.getLogger().warning(getClass().getSimpleName() + " is already registered, skipping.");
            return;
        }
        isRegistered = true;

        if (!isEnabled()) {
            plugin.getLogger().info(getClass().getSimpleName() + " is disabled in config, skipping registration.");
            return;
        }

        onRegister();
        plugin.getLifecycleManager().registerEventHandler(
                io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS,
                commands -> notCommands().forEach(cmd -> commands.registrar().register(cmd.build()))
        );
    }
}
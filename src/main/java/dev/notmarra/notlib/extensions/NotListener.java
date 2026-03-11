package dev.notmarra.notlib.extensions;

import dev.notmarra.notlib.command.Command;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Server;
import org.bukkit.event.Listener;

import java.util.List;

public abstract class NotListener extends Configurable implements Listener {
    private boolean isRegistered = false;

    public NotListener(NotPlugin plugin) { super(plugin); }

    public abstract String getId();

    public void onRegister() {}

    public List<Command> notCommands() { return List.of(); }

    public Server getServer() { return plugin.getServer(); }

    @Override
    public List<String> getConfigPaths() { return List.of(); }

    public void register() {
        if (isRegistered) return;
        isRegistered = true;

        if (!isEnabled()) return;
        this.onRegister();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            notCommands().forEach(cmd -> commands.registrar().register(cmd.build()));
        });
    }
}

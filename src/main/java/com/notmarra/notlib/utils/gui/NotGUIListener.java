package com.notmarra.notlib.utils.gui;

import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public class NotGUIListener implements Listener {
    public final JavaPlugin plugin;

    private final Map<UUID, NotGUI> openGUIs = new HashMap<>();

    private boolean isRegistered = false;

    public NotGUIListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void openGUI(Player player, NotGUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory());
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        NotGUI gui = openGUIs.get(player.getUniqueId());

        if (gui != null && clickedInventory.equals(gui.getBuiltInventory())) {
            gui.handleClick(event);
            event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        Inventory closedInventory = event.getInventory();
        NotGUI gui = openGUIs.get(player.getUniqueId());

        if (gui != null && closedInventory.equals(gui.getBuiltInventory())) {
            openGUIs.remove(player.getUniqueId());
        }
    }

    private List<NotCommand> getNotCommands() {
        return List.of(

        );
    }

    public void register() {
        if (isRegistered) return;
        isRegistered = true;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            for (NotCommand cmd : getNotCommands()) {
                commands.registrar().register(cmd.build());
            }
        });
    }
}
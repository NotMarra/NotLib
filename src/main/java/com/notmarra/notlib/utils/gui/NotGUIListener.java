package com.notmarra.notlib.utils.gui;

import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.command.NotCommand;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class NotGUIListener implements Listener {
    public final JavaPlugin plugin;

    private final Map<UUID, NotInvHolder> openInventories = new HashMap<>();

    private boolean isRegistered = false;

    public NotGUIListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean handleInventoryClick(Player player, NotInvHolder holder, int slot) {
        UUID playerId = player.getUniqueId();
        
        Inventory clickedInventory = holder.getInventory();
        int size = clickedInventory.getSize();
        
        if (slot == size - 1) {
            // playerColors.remove(playerId);
            ChatF.empty()
                .appendBold("Your chat color has been reset!", ChatF.C_GREEN)
                .sendTo(player);
            player.closeInventory();
            return true;
        }
        
        ItemStack clicked = clickedInventory.getItem(slot);
        
        if (clicked != null) {
            // for (Map.Entry<ChatColor, Material> entry : holder.getColorMaterials().entrySet()) {
            //     if (clicked.getType() == entry.getValue()) {
            //         ChatColor selectedColor = entry.getKey();
            //         playerColors.put(playerId, selectedColor);
            //         ChatF.empty()
            //             .appendBold("Your chat color has been set to " + selectedColor.name() + "!", selectedColor.getColor())
            //             .sendTo(player);
            //         player.closeInventory();
            //         return true;
            //     }
            // }
        }
        
        return true;
    }
    
    public boolean handleInventoryClose(Player player) {
        UUID playerId = player.getUniqueId();
        // return openInventories.remove(playerId) != null;
        return true;
    }
    
    public Component applyColorToMessage(Player player, String message) {
        UUID playerId = player.getUniqueId();
        
        // if (playerColors.containsKey(playerId)) {
        //     ChatColor color = playerColors.get(playerId);
        //     return ChatF.of(message, color.getColor()).build();
        // }
        
        return ChatF.of(message).build();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        NotInvHolder holder = openInventories.get(player.getUniqueId());

        if (holder != null && holder.getInventory().equals(clickedInventory)) {
            event.setCancelled(true);
            handleInventoryClick(player, holder, event.getSlot());
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Inventory closedInventory = event.getInventory();
        NotInvHolder holder = openInventories.get(player.getUniqueId());

        if (holder != null && holder.getInventory().equals(closedInventory)) {
            handleInventoryClose(player);
        }
    }

    private List<NotCommand> getNotCommands() {
        return List.of(

        );
    }

    public FileConfiguration getPluginConfig() {
        return plugin.getConfig();
    }

    public Server getServer() {
        return plugin.getServer();
    }

    public Logger getLogger() {
        return plugin.getLogger();
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
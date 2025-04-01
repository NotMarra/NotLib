package com.notmarra.notlib.utils.gui;

import com.notmarra.notlib.extensions.NotListener;
import com.notmarra.notlib.extensions.NotPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class NotGUIListener extends NotListener {
    public static final String ID = "notguilistener";

    private final Map<UUID, NotGUI> openGUIs = new HashMap<>();

    public NotGUIListener(NotPlugin plugin) { super(plugin); }

    @Override
    public String getId() { return ID; }

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

        if (gui != null && clickedInventory != null && clickedInventory.equals(gui.getBuiltInventory())) {
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
            if (gui.onClose != null) gui.onClose.accept(event);
            openGUIs.remove(player.getUniqueId());
        }
    }
}
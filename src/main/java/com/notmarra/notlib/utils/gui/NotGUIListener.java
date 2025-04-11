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
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
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
        
        if (gui == null) return;
        if (clickedInventory == null) return;

        if (clickedInventory.getHolder() == gui) {
            event.setCancelled(gui.handleClick(event));
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

    // TODO: this
    // @EventHandler
    // public void onInventoryMoveItemEvent(InventoryMoveItemEvent event) {
    //     if (!(event.getInitiator() instanceof Player)) return;

    //     Player player = (Player) event.getInitiator();
    //     NotGUI gui = openGUIs.get(player.getUniqueId());
    //     if (gui == null) return;

    //     Inventory srcInventory = event.getSource();
    //     Inventory destInventory = event.getDestination();

    //     UUID itemUUID = gui.getItemIdFromItemStack(event.getItem());
    //     if (itemUUID == null) return;
    //     NotGUIItem item = gui.getNotItem(itemUUID);
    //     if (item == null) return;
    //     // buttons are not moveable
    //     if (item.action() != null) {
    //         event.setCancelled(true);
    //         return;
    //     }

    //     // you are moving stuff from/in/inside the gui, which is not allowed
    //     if (destInventory.equals(gui.getBuiltInventory())) {
    //         event.setCancelled(true);
    //         return;
    //     }

    //     event.setCancelled(true);
    // }
    
    // @EventHandler
    // public void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
    //     if (!(event.getInventory().getHolder() instanceof Player)) return;

    //     Player player = (Player) event.getInventory().getHolder();
    //     NotGUI gui = openGUIs.get(player.getUniqueId());

    //     if (gui != null && event.getInventory().equals(gui.getBuiltInventory())) {
    //         event.setCancelled(true);
    //     }
    // }
}
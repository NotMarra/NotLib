package com.notmarra.notlib.utils.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotSize;
import com.notmarra.notlib.utils.NotVector2;

import net.kyori.adventure.text.Component;

public class NotGUI implements InventoryHolder {
    private NotLib plugin;
    @Nullable private InventoryType guiType; // null is double-chest
    private Component guiTitle;
    private NotGUIContainer rootContainer;

    private Inventory builtInventory;
    private Map<Integer, Consumer<InventoryClickEvent>> clickHandlers;

    public NotGUI() {
        plugin = NotLib.getInstance();
        guiTitle = ChatF.of("NotGUI").build();
        rootContainer = new NotGUIContainer(this);
        clickHandlers = new HashMap<>();
    }

    public NotGUI type(InventoryType type) { guiType = type; return this; }
    public NotGUI title(String title) { return title(ChatF.of(title)); }
    public NotGUI title(ChatF title) { return title(title.build()); }
    public NotGUI title(Component title) { this.guiTitle = title; return this; }
    public NotGUI position(int x) { rootContainer.position(x); return this; }
    public NotGUI position(int x, int y) { rootContainer.position(x, y); return this; }
    public NotGUI position(NotVector2 position) { rootContainer.position(position); return this; }
    public NotGUI size(int width) { rootContainer.size(width); return this; }
    public NotGUI size(int width, int height) { rootContainer.size(width, height); return this; }
    public NotGUI size(NotSize size) { rootContainer.size(size); return this; }
    public NotGUI rows(int rows) {
        if (guiType != null && guiType != InventoryType.CHEST) {
            throw new IllegalArgumentException("Cannot set rows on non-chest GUIs");
        }
        if (guiType == null) {
            if (rows < 1 || rows > 6) {
                throw new IllegalArgumentException("Invalid number of rows for double-chest GUI, must be between 1 and 6");
            }
        } else {
            if (rows < 1 || rows > 3) {
                throw new IllegalArgumentException("Invalid number of rows for " + guiType + " GUI, must be between 1 and 3");
            }
        }
        return size(9, rows);
    }

    public NotGUIContainer createContainer() {
        NotGUIContainer container = new NotGUIContainer(this, rootContainer);
        rootContainer.addChild(container);
        return container;
    }

    public NotGUIContainer createContainer(int width, int height) {
        return createContainer().size(width, height);
    }

    public NotGUIContainer createContainer(NotSize size) {
        return createContainer().size(size);
    }

    public NotGUIContainer createContainer(int x, int y, int width, int height) {
        return createContainer().position(x, y).size(width, height);
    }

    public NotGUIContainer createContainer(NotVector2 position, NotSize size) {
        return createContainer().position(position).size(size);
    }

    public NotGUI addItem(NotGUIItem item, int slot) {
        rootContainer.addItem(item, slot);
        return this;
    }
    
    public NotGUI addItem(NotGUIItem item, int x, int y) {
        rootContainer.addItem(item, x, y);
        return this;
    }
    
    public NotGUI addButton(Material material, String name, int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
        return this;
    }
    
    public NotGUI addButton(Material material, String name, int x, int y, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }
    
    public NotGUIItem createItem(Material material) {
        return new NotGUIItem(this, rootContainer, material);
    }

    public NotGUIAnimation createAnimation(long durationTicks, long frames) {
        return new NotGUIAnimation(this, durationTicks, frames);
    }

    public Inventory getBuiltInventory() {
        if (builtInventory == null) {
            builtInventory = build();
        }
        return builtInventory;
    }

    @Override
    public Inventory getInventory() {
        return getBuiltInventory();
    }

    public NotLib getPlugin() {
        return plugin;
    }

    public void refresh() {
        Inventory newInventory = build();

        if (builtInventory != null) {
            builtInventory.clear();
            for (int i = 0; i < newInventory.getSize(); i++) {
                builtInventory.setItem(i, newInventory.getItem(i));
            }
        } else {
            builtInventory = newInventory;
        }
    }

    public void animate(long durationTicks, int frames, Consumer<Float> updateFunction) {
        createAnimation(durationTicks, frames).start(updateFunction);
    }

    private void renderContainer(NotGUIContainer container, Inventory inventory, NotVector2 parentOffset) {
        NotVector2 containerPosition = container.getPosition();
        
        NotSize containerSize = container.getSize();

        plugin.getLogger().info("Rendering container with pos: " + containerPosition.toString() + " and size: " + containerSize.toString());
        
        NotVector2 absolutePosition = new NotVector2(
            parentOffset.x + containerPosition.x,
            parentOffset.y + containerPosition.y
        );
        
        // Render this container's items
        Map<Integer, ItemStack> items = container.getAllItems();
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            int localSlot = entry.getKey();
            ItemStack item = entry.getValue();
            
            // Calculate absolute position
            int x = localSlot % containerSize.width;
            int y = localSlot / containerSize.width;
            
            int absoluteX = absolutePosition.x + x;
            int absoluteY = absolutePosition.y + y;
            
            // Calculate inventory slot
            int inventorySlot = absoluteY * 9 + absoluteX;
            
            // Place item if within bounds
            if (inventorySlot >= 0 && inventorySlot < inventory.getSize()) {
                inventory.setItem(inventorySlot, item);
            }
        }
        
        // Render child containers
        for (NotGUIContainer child : container.getChildren()) {
            renderContainer(child, inventory, absolutePosition);
        }
    }

    public boolean handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        
        // Check if we have a direct handler for this slot
        Consumer<InventoryClickEvent> handler = clickHandlers.get(slot);
        if (handler != null) {
            handler.accept(event);
            return true;
        }
        
        // Convert to root container coordinates
        int x = slot % 9;
        int y = slot / 9;

        NotVector2 rootPosition = rootContainer.getPosition();
        NotSize rootSize = rootContainer.getSize();
        
        // Check if this is within the root container
        if (x >= rootPosition.x && 
            x < rootPosition.x + rootSize.width &&
            y >= rootPosition.y && 
            y < rootPosition.y + rootSize.height
        ) {
            
            // Calculate local slot for root container
            int localX = x - rootPosition.x;
            int localY = y - rootPosition.y;
            int localSlot = localY * rootSize.width + localX;
            
            // Handle in root container
            return rootContainer.handleClick(event, localSlot);
        }
        
        return false;
    }

    public Inventory build() {
        Inventory inventory;

        if (guiType != null) {
            inventory = plugin.getServer().createInventory(this, guiType, guiTitle);
        } else {
            inventory = plugin.getServer().createInventory(this, rootContainer.totalSize(), guiTitle);
        }

        renderContainer(rootContainer, inventory, NotVector2.zero());

        return inventory;
    }

    public void open(Player player) {
        NotGUIListener listener = plugin.getGUIListener();
        if (listener == null) {
            throw new IllegalStateException("GUI listener not registered");
        }
        listener.openGUI(player, this);
    }

    public static NotGUI create() { return new NotGUI(); }
    public static NotGUI create(String title) { return create().title(title); }
    public static NotGUI create(ChatF title) { return create().title(title); }
    public static NotGUI create(Component title) { return create().title(title); }
}

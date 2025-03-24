package com.notmarra.notlib.utils.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.utils.NotSize;
import com.notmarra.notlib.utils.NotVector2;

public class NotGUIContainer {
    private NotGUI parentGUI;
    @Nullable private NotGUIContainer parentContainer;
    private NotVector2 containerPosition;
    private NotSize containerSize;
    private List<NotGUIContainer> children;
    private Map<Integer, ItemStack> items;
    private Map<Integer, BiConsumer<InventoryClickEvent, NotGUIContainer>> clickHandlers;

    public NotGUIContainer(NotGUI gui) {
        this(gui, null);
    }

    public NotGUIContainer(NotGUI gui, NotGUIContainer parent) {
        this.parentGUI = gui;
        this.parentContainer = parent;
        containerPosition = NotVector2.zero();
        containerSize = NotSize.zero();
        children = new ArrayList<>();
        items = new HashMap<>();
        clickHandlers = new HashMap<>();
    }

    public NotVector2 getPosition() { return containerPosition; }
    public NotSize getSize() { return containerSize; }
    public List<NotGUIContainer> getChildren() { return children; }

    public NotGUIContainer position(int x) { containerPosition.set(x); return this; }
    public NotGUIContainer position(int x, int y) { containerPosition.set(x, y); return this; }
    public NotGUIContainer position(NotVector2 position) { containerPosition.set(position); return this; }

    public NotGUIContainer size(int width) { containerSize.set(width); return this; }
    public NotGUIContainer size(int width, int height) { containerSize.set(width, height); return this; }
    public NotGUIContainer size(NotSize size) { containerSize.set(size); return this; }

    public NotGUIContainer createContainer() {
        NotGUIContainer container = new NotGUIContainer(gui(), this);
        addChild(container);
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

    public NotGUIContainer addChild(NotGUIContainer child) {
        children.add(child);
        return this;
    }

    public int totalSize() {
        return containerSize.width * containerSize.height;
    }

    public NotGUI gui() {
        return parentGUI;
    }

    public NotGUIContainer container() {
        return parentContainer;
    }

    public NotGUIItem createItem(Material material, int x, int y) {
        NotGUIItem item = new NotGUIItem(parentGUI, this, material);
        addItem(item, x, y);
        return item;
    }

    public NotGUIContainer addItem(NotGUIItem item, int x, int y) {
        if (x < 0 || x >= containerSize.width || y < 0 || y >= containerSize.height) {
            throw new IllegalArgumentException("Position out of bounds for container");
        }
        
        int slot = y * containerSize.width + x;
        return addItem(item, slot);
    }
    
    public NotGUIContainer addItem(NotGUIItem item, int slot) {
        if (slot < 0 || slot >= totalSize()) {
            throw new IllegalArgumentException("Slot out of bounds for container");
        }
        
        ItemStack builtItem = item.build();
        items.put(slot, builtItem);
        updateInventory(slot, builtItem);
        
        return this;
    }
    
    public NotGUIContainer addButton(Material material, String name, int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        NotGUIItem item = new NotGUIItem(parentGUI, this, material).name(name);
        addItem(item, slot);
        registerClickHandler(slot, action);
        return this;
    }
    
    public NotGUIContainer addButton(Material material, String name, int x, int y, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        int slot = y * containerSize.width + x;
        return addButton(material, name, slot, action);
    }
    
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }
    
    private void updateInventory(int slot, ItemStack item) {
        Inventory builtInventory = parentGUI.getBuiltInventory();
        if (builtInventory != null) {
            // Calculate absolute slot
            int x = slot % containerSize.width;
            int y = slot / containerSize.width;
            
            int absoluteX = containerPosition.x + x;
            int absoluteY = containerPosition.y + y;
            
            int inventorySlot = absoluteY * 9 + absoluteX;
            
            if (inventorySlot >= 0 && inventorySlot < builtInventory.getSize()) {
                NotLib.getInstance().getLogger().info("Placing item " + item.getType() + " at slot " + inventorySlot);
                builtInventory.setItem(inventorySlot, item);
            }
        }
    }
    
    public NotGUIContainer registerClickHandler(int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        clickHandlers.put(slot, action);
        return this;
    }
    
    public boolean handleClick(InventoryClickEvent event, int localSlot) {
        // Check if we have a handler for this slot
        BiConsumer<InventoryClickEvent, NotGUIContainer> handler = clickHandlers.get(localSlot);
        if (handler != null) {
            handler.accept(event, this);
            return true;
        }
        
        // Check if any child containers handle this click
        for (NotGUIContainer child : children) {
            // Calculate if the click is within the child
            int relativeX = localSlot % containerSize.width - child.containerPosition.x;
            int relativeY = localSlot / containerSize.width - child.containerPosition.y;
            
            if (relativeX >= 0 && relativeX < child.containerSize.width && 
                relativeY >= 0 && relativeY < child.containerSize.height) {
                // Calculate local slot for child
                int childLocalSlot = relativeY * child.containerSize.width + relativeX;
                if (child.handleClick(event, childLocalSlot)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public Map<Integer, ItemStack> getAllItems() {
        return new HashMap<>(items);
    }
}

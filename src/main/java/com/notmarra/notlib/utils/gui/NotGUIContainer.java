package com.notmarra.notlib.utils.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.notmarra.notlib.utils.NotSize;
import com.notmarra.notlib.utils.NotVector2;

public class NotGUIContainer {
    private UUID uid;
    private NotGUI parentGUI;
    @Nullable private NotGUIContainer parentContainer;
    private NotVector2 position;
    private NotSize size;
    private List<NotGUIContainer> children;

    private Map<UUID, NotGUIItem> notItems;
    private Map<Integer, ItemStack> items;
    private Map<Integer, UUID> slotToItemId;

    private Map<UUID, BiConsumer<InventoryClickEvent, NotGUIContainer>> clickHandlers;
    private boolean wrapped;

    public NotGUIContainer(NotGUI gui) {
        this(gui, null);
    }

    public NotGUIContainer(NotGUI gui, NotGUIContainer parent) {
        this.uid = UUID.randomUUID();
        this.parentGUI = gui;
        this.parentContainer = parent;
        position = NotVector2.zero();
        size = NotSize.zero();
        children = new ArrayList<>();

        notItems = new HashMap<>();
        items = new HashMap<>();
        slotToItemId = new HashMap<>();
        
        clickHandlers = new HashMap<>();
        wrapped = true;
    }

    public NotVector2 getPosition() { return position; }
    public NotSize getSize() { return size; }
    public List<NotGUIContainer> getChildren() { return children; }
    public List<NotGUIContainer> getMeAndChildren() {
        List<NotGUIContainer> allChildren = new ArrayList<>();
        allChildren.add(this);
        allChildren.addAll(children);
        return allChildren;
    }
    public NotGUIContainer root() {
        NotGUIContainer c = this;
        while (c.parentContainer != null) c = c.parentContainer;
        return c;
    }
    public List<NotGUIContainer> getRootAndItsChildren() { return root().getMeAndChildren(); }

    public UUID id() { return uid; }
    public NotGUI gui() { return parentGUI; }
    public NotGUIContainer parent() { return parentContainer; }

    public NotGUIContainer notWrapped() { this.wrapped = false; return this; }
    public boolean isWrapped() { return wrapped; }

    public NotGUIContainer position(int x) { position.set(x); return this; }
    public NotGUIContainer position(int x, int y) { position.set(x, y); return this; }
    public NotGUIContainer position(NotVector2 position) { position.set(position); return this; }

    public NotGUIContainer size(int width) { this.size = NotSize.of(width); return this; }
    public NotGUIContainer size(int width, int height) { this.size = NotSize.of(width, height); return this; }
    public NotGUIContainer size(NotSize size) { this.size = size; return this; }

    public BiConsumer<InventoryClickEvent, NotGUIContainer> getHandler(UUID itemId) {
        BiConsumer<InventoryClickEvent, NotGUIContainer> handler = clickHandlers.get(itemId);
        if (handler != null) return handler;

        for (NotGUIContainer child : children) {
            handler = child.getHandler(itemId);
            if (handler != null) return handler;
        }

        return null;
    }

    public NotGUIContainer createContainer() {
        NotGUIContainer child = new NotGUIContainer(gui(), this);
        addChild(child);
        return child;
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

    public NotGUIContainer addChild(NotGUIContainer child) { children.add(child); return this; }
    public int totalSize() { return size.width * size.height; }

    public NotGUIItem createItem(Material material, int x, int y) {
        NotGUIItem item = new NotGUIItem(parentGUI, this, material);
        addItem(item, x, y);
        return item;
    }

    public NotGUIContainer addItem(NotGUIItem item, int x, int y) {
        if (x < 0 || x >= size.width || y < 0 || y >= size.height) {
            throw new IllegalArgumentException("Position out of bounds for container");
        }
        
        int slot = y * size.width + x;
        return addItem(item, slot);
    }
    
    public NotGUIContainer addItem(NotGUIItem item, int slot) {
        if (slot < 0 || slot >= totalSize()) {
            gui().getPlugin().getLogger().info("Slot: " + slot + " Total: " + totalSize());
            gui().getPlugin().getLogger().info("Item: " + item.type());
            throw new IllegalArgumentException("Slot out of bounds for container");
        }
        
        ItemStack builtItem = item.build();
        UUID itemId = item.id();

        notItems.put(itemId, item);
        items.put(slot, builtItem);
        slotToItemId.put(slot, itemId);

        setItemToSlot(slot, builtItem);
        
        return this;
    }
    
    public NotGUIContainer addButton(Material material, String name, int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        NotGUIItem item = new NotGUIItem(parentGUI, this, material).name(name);
        addItem(item, slot);
        registerClickHandler(item.id(), action);
        return this;
    }
    
    public NotGUIContainer addButton(Material material, String name, int x, int y, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        int slot = y * size.width + x;
        return addButton(material, name, slot, action);
    }
    
    public ItemStack getItem(int slot) { return items.get(slot); }
    
    private void setItemToSlot(int slot, ItemStack item) {
        Inventory builtInventory = parentGUI.getBuiltInventory();
        if (builtInventory != null) {
            int x = slot % size.width;
            int y = slot / size.width;
            
            int absoluteX = position.x + x;
            int absoluteY = position.y + y;
            int inventorySlot = absoluteY * gui().rowSize() + absoluteX;
            
            if (inventorySlot >= 0 && inventorySlot < builtInventory.getSize()) {
                UUID itemId = getItemIdFromItemStack(item);
                if (itemId != null) slotToItemId.put(slot, itemId);
                builtInventory.setItem(inventorySlot, item);
            } else if (wrapped) {
                int wrappedSlot = inventorySlot % builtInventory.getSize();
                if (wrappedSlot < 0) wrappedSlot += builtInventory.getSize();
                UUID itemId = getItemIdFromItemStack(item);
                if (itemId != null) slotToItemId.put(wrappedSlot, itemId);
                builtInventory.setItem(wrappedSlot, item);             
            }
        }
    }
    
    public NotGUIContainer registerClickHandler(UUID itemId, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        clickHandlers.put(itemId, action);
        return this;
    }

    public UUID getItemIdFromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(gui().getPlugin(), NotGUI.ITEM_UUID_KEY);
        PersistentDataContainer container = meta.getPersistentDataContainer();

        if (!container.has(key, PersistentDataType.STRING)) return null;
        return UUID.fromString(container.get(key, PersistentDataType.STRING));
    }

    public boolean handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        int x = slot % gui().rowSize();
        int y = slot / gui().rowSize();
        int localX = x - position.x;
        int localY = y - position.y;
        int localSlot = localY * size.width + localX;
        return handleItemClick(event, localSlot);
    }
    
    public boolean handleItemClick(InventoryClickEvent event, int localSlot) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return false;
        
        UUID itemId = getItemIdFromItemStack(clickedItem);
        if (itemId == null) itemId = slotToItemId.get(localSlot);

        if (itemId != null) {
            BiConsumer<InventoryClickEvent, NotGUIContainer> handler = clickHandlers.get(itemId);
            if (handler != null) {
                handler.accept(event, this);
                return true;
            }
        }

        for (NotGUIContainer child : children) {
            int relativeX = localSlot % size.width - child.position.x;
            int relativeY = localSlot / size.width - child.position.y;
            int childLocalSlot = relativeY * child.size.width + relativeX;
            if (child.handleItemClick(event, childLocalSlot)) {
                return true;
            }
        }
        
        return false;
    }
    
    public Map<Integer, ItemStack> getAllItems() {
        return new HashMap<>(items);
    }

    public void moveItem(int fromSlot, int toSlot) {
        if (!items.containsKey(fromSlot)) return;
        
        ItemStack item = items.get(fromSlot);
        UUID itemId = slotToItemId.get(fromSlot);

        items.remove(fromSlot);
        slotToItemId.remove(fromSlot);
        
        items.put(toSlot, item);
        slotToItemId.put(toSlot, itemId);
        
        setItemToSlot(toSlot, item);
    }

    public void refresh() {
        if (parentGUI.getBuiltInventory() == null) return;

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            setItemToSlot(entry.getKey(), entry.getValue());
        }
        
        for (NotGUIContainer child : children) {
            child.refresh();
        }
    }

    public void render(Inventory inventory, NotVector2 parentOffset) {
        NotVector2 absolutePosition = new NotVector2(
            parentOffset.x + position.x,
            parentOffset.y + position.y
        );
        
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            int localSlot = entry.getKey();
            ItemStack item = entry.getValue();
            
            int x = localSlot % size.width;
            int y = localSlot / size.width;
            int absoluteX = absolutePosition.x + x;
            int absoluteY = absolutePosition.y + y;
            int inventorySlot = absoluteY * gui().rowSize() + absoluteX;
            
            if (inventorySlot >= 0 && inventorySlot < inventory.getSize()) {
                inventory.setItem(inventorySlot, item);
            } else if (isWrapped()) {
                int wrappedSlot = inventorySlot % inventory.getSize();
                if (wrappedSlot < 0) wrappedSlot += inventory.getSize();
                inventory.setItem(wrappedSlot, item);
            }
        }
        
        for (NotGUIContainer child : children) {
            child.render(inventory, absolutePosition);
        }
    }
}

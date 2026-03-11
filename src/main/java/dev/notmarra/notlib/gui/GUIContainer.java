package dev.notmarra.notlib.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

import dev.notmarra.notlib.chat.Text;
import dev.notmarra.notlib.gui.utils.Size;
import dev.notmarra.notlib.gui.utils.Vector2;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;


public class GUIContainer {
    private final UUID uid;
    private final GUI gui;
    @Nullable private final GUIContainer parentContainer;
    private final Vector2 position;
    private Size size;
    private final List<GUIContainer> children;

    private final Map<UUID, GUIItem> notItems;
    private final Map<Integer, ItemStack> items;

    // private Map<UUID, BiConsumer<InventoryClickEvent, GUIContainer>> clickHandlers;
    private boolean wrapped;

    public GUIContainer(GUI gui) {
        this(gui, null);
    }

    public GUIContainer(GUI gui, @Nullable GUIContainer parent) {
        this.uid = UUID.randomUUID();
        this.gui = gui;
        this.parentContainer = parent;
        position = Vector2.zero();
        size = Size.zero();
        children = new ArrayList<>();

        notItems = new HashMap<>();
        items = new HashMap<>();
        
        // clickHandlers = new HashMap<>();
        wrapped = true;
    }

    public Vector2 pos() { return position; }
    public Size size() { return size; }
    public List<GUIContainer> getChildren() { return children; }
    public List<GUIContainer> getMeAndChildren() {
        List<GUIContainer> allChildren = new ArrayList<>();
        allChildren.add(this);
        allChildren.addAll(children);
        return allChildren;
    }
    public GUIContainer root() {
        GUIContainer c = this;
        while (c.parentContainer != null) c = c.parentContainer;
        return c;
    }
    public List<GUIContainer> getRootAndItsChildren() { return root().getMeAndChildren(); }

    public UUID id() { return uid; }
    public GUI gui() { return gui; }
    public GUIContainer parent() { return parentContainer; }

    public GUIContainer notWrapped() { this.wrapped = false; return this; }
    public boolean isWrapped() { return wrapped; }

    public GUIContainer position(int x) { position.set(x); return this; }
    public GUIContainer position(int x, int y) { position.set(x, y); return this; }
    public GUIContainer position(Vector2 position) { position.set(position); return this; }

    public GUIContainer size(int width) { this.size = Size.of(width); return this; }
    public GUIContainer size(int width, int height) { this.size = Size.of(width, height); return this; }
    public GUIContainer size(Size size) { this.size = size; return this; }

    public BiConsumer<InventoryClickEvent, GUIContainer> getHandler(UUID itemId) {
        GUIItem item = getItem(itemId);
        if (item != null) return item.action();
        return (BiConsumer<InventoryClickEvent, GUIContainer>) (event, container) -> {};
    }

    public GUIContainer createContainer() {
        GUIContainer child = new GUIContainer(gui(), this);
        addChild(child);
        return child;
    }

    public GUIContainer createContainer(int width, int height) {
        return createContainer().size(width, height);
    }

    public GUIContainer createContainer(Size size) {
        return createContainer().size(size);
    }

    public GUIContainer createContainer(int x, int y, int width, int height) {
        return createContainer().position(x, y).size(width, height);
    }

    public GUIContainer createContainer(Vector2 position, Size size) {
        return createContainer().position(position).size(size);
    }

    public GUIContainer addChild(GUIContainer child) { children.add(child); return this; }
    public int totalSize() { return size.width * size.height; }

    public GUIItem createItem(Material material, int x, int y) {
        GUIItem item = new GUIItem(gui, this, material);
        addItem(item, x, y);
        return item;
    }

    public GUIContainer addItem(GUIItem item, int x, int y) {
        if (x < 0 || x >= size.width || y < 0 || y >= size.height) {
            throw new IllegalArgumentException("Position out of bounds for container");
        }
        
        int slot = y * size.width + x;
        return addItem(item, slot);
    }
    
    public GUIContainer addItem(GUIItem item, int slot) {
        if (slot < 0 || slot >= totalSize()) {
            throw new IllegalArgumentException("Slot out of bounds for container");
        }
        
        ItemStack builtItem = item.build();
        UUID itemId = item.id();

        notItems.put(itemId, item);
        items.put(slot, builtItem);

        setItemToSlot(slot, builtItem);
        
        return this;
    }

    public GUIContainer addButton(GUIItem item, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        addItem(item.action(action), slot);
        return this;
    }

    public GUIContainer addButton(GUIItem item, int x, int y, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        addItem(item.action(action), x, y);
        return this;
    }
    
    public GUIContainer addButton(Material material, Text name, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        GUIItem item = new GUIItem(gui, this, material).name(name).action(action);
        addItem(item, slot);
        return this;
    }

    public GUIContainer addButton(Material material, String name, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addButton(material, Text.of(name), slot, action);
    }
    
    public GUIContainer addButton(Material material, String name, int x, int y, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addButton(material, name, y * size.width + x, action);
    }

    public GUIContainer addButton(Material material, Text name, int x, int y, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addButton(material, name, y * size.width + x, action);
    }
    
    public GUIItem getItem(UUID itemId) {
        GUIItem item = notItems.get(itemId);
        if (item != null) return item;
        for (GUIContainer child : children) {
            item = child.getItem(itemId);
            if (item != null) return item;
        }
        return null;
    }
    public ItemStack getItem(int slot) {
        ItemStack item = items.get(slot);
        if (item != null) return item;
        for (GUIContainer child : children) {
            item = child.getItem(slot);
            if (item != null) return item;
        }
        return null;
    }
    
    private void setItemToSlot(int slot, ItemStack item) {
        Inventory builtInventory = gui.getBuiltInventory();

        int x = slot % size.width;
        int y = slot / size.width;
        
        int absoluteX = position.x + x;
        int absoluteY = position.y + y;
        int inventorySlot = absoluteY * gui().rowSize() + absoluteX;
        
        if (inventorySlot >= 0 && inventorySlot < builtInventory.getSize()) {
            builtInventory.setItem(inventorySlot, item);
        } else if (wrapped) {
            int wrappedSlot = inventorySlot % builtInventory.getSize();
            if (wrappedSlot < 0) wrappedSlot += builtInventory.getSize();
            builtInventory.setItem(wrappedSlot, item);             
        }
    }
    
    public UUID getItemIdFromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(gui().getPlugin(), GUI.ITEM_UUID_KEY);
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

    public boolean handleClick(InventoryClickEvent event, GUIItem item) {
        BiConsumer<InventoryClickEvent, GUIContainer> handler = getHandler(item.id());
        if (handler != null) {
            handler.accept(event, this);
            return true;
        }
        return false;
    }

    public boolean handleItemClick(InventoryClickEvent event, int localSlot) {
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null) return false;
        
        UUID itemId = getItemIdFromItemStack(clickedItem);

        if (itemId != null) {
            BiConsumer<InventoryClickEvent, GUIContainer> handler = getHandler(itemId);
            if (handler != null) {
                handler.accept(event, this);
                return true;
            }
        }

        for (GUIContainer child : children) {
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

        items.remove(fromSlot);        
        items.put(toSlot, item);
        
        setItemToSlot(toSlot, item);
    }

    public void refresh() {
        if (gui.getBuiltInventory() == null) return;

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            setItemToSlot(entry.getKey(), entry.getValue());
        }
        
        for (GUIContainer child : children) {
            child.refresh();
        }
    }

    public void render(Inventory inventory, Vector2 parentOffset) {
        Vector2 absolutePosition = new Vector2(
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
        
        for (GUIContainer child : children) {
            child.render(inventory, absolutePosition);
        }
    }
}

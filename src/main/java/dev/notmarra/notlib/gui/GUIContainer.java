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

import net.kyori.adventure.text.Component;

/**
 * A rectangular region inside a {@link GUI} that holds {@link GUIItem}s and
 * can nest further child containers.
 *
 * <h2>Coordinate system</h2>
 * <p>Every container has its own local 0-based grid whose origin sits at
 * {@link #pos()} relative to the parent container (or to the inventory
 * top-left for the root container). Slots are numbered row-first:
 * {@code slot = y * width + x}.</p>
 *
 * <h2>Wrapping</h2>
 * <p>When an item's absolute inventory slot falls outside the inventory bounds,
 * the container wraps it back into range (modulo the inventory size) by default.
 * Call {@link #notWrapped()} to throw away out-of-bounds items instead.</p>
 *
 * <h2>Typical usage</h2>
 * <pre>{@code
 * GUIContainer toolbar = gui.createContainer(0, 0, 9, 1);
 *
 * toolbar.addButton(Material.ARROW, "Back", 0, (event, c) -> gui.open(player));
 *
 * toolbar.addItem(
 *     toolbar.item(Material.DIAMOND).name("&bInfo").lore("Some info"),
 *     4
 * );
 * }</pre>
 */
public class GUIContainer {

    private final UUID uid;
    private final GUI gui;
    @Nullable private final GUIContainer parentContainer;
    private final Vector2 position;
    private Size size;
    private final List<GUIContainer> children;

    /** GUIItem lookup by item UUID (for click routing). */
    private final Map<UUID, GUIItem> notItems;
    /** Built ItemStack lookup by local slot index. */
    private final Map<Integer, ItemStack> items;

    private boolean wrapped;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a root container attached to a GUI with no parent container.
     *
     * @param gui owner GUI
     */
    public GUIContainer(GUI gui) {
        this(gui, null);
    }

    /**
     * Creates a container attached to a GUI, optionally nested inside a parent.
     *
     * @param gui    owner GUI
     * @param parent parent container, or {@code null} for the root container
     */
    public GUIContainer(GUI gui, @Nullable GUIContainer parent) {
        this.uid             = UUID.randomUUID();
        this.gui             = gui;
        this.parentContainer = parent;
        this.position        = Vector2.zero();
        this.size            = Size.zero();
        this.children        = new ArrayList<>();
        this.notItems        = new HashMap<>();
        this.items           = new HashMap<>();
        this.wrapped         = true;
    }

    // -------------------------------------------------------------------------
    // Configuration (fluent)
    // -------------------------------------------------------------------------

    /**
     * Disables slot wrapping so items that fall outside the inventory bounds
     * are simply discarded instead of being placed at a wrapped slot.
     *
     * @return this container
     */
    public GUIContainer notWrapped() { this.wrapped = false; return this; }

    /**
     * Returns whether slot wrapping is active.
     *
     * @return {@code true} if wrapping is enabled (default)
     */
    public boolean isWrapped() { return wrapped; }

    /**
     * Sets the container's position in its parent using the same value for
     * both axes (useful for square-origin positioning).
     *
     * @param x column and row offset (0-based)
     * @return this container
     */
    public GUIContainer position(int x) { position.set(x); return this; }

    /**
     * Sets the container's position in its parent.
     *
     * @param x column offset (0-based)
     * @param y row offset (0-based)
     * @return this container
     */
    public GUIContainer position(int x, int y) { position.set(x, y); return this; }

    /**
     * Sets the container's position from a {@link Vector2}.
     *
     * @param position position vector
     * @return this container
     */
    public GUIContainer position(Vector2 position) { position.set(position); return this; }

    /**
     * Sets both width and height to the same value.
     *
     * @param width width and height in slots
     * @return this container
     */
    public GUIContainer size(int width) { this.size = Size.of(width); return this; }

    /**
     * Sets the width and height independently.
     *
     * @param width  number of columns
     * @param height number of rows
     * @return this container
     */
    public GUIContainer size(int width, int height) { this.size = Size.of(width, height); return this; }

    /**
     * Sets the size from a {@link Size} value object.
     *
     * @param size size value
     * @return this container
     */
    public GUIContainer size(Size size) { this.size = size; return this; }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the position of this container relative to its parent.
     *
     * @return position vector (mutable reference)
     */
    public Vector2 pos() { return position; }

    /**
     * Returns the size of this container.
     *
     * @return size value
     */
    public Size size() { return size; }

    /**
     * Returns the unique identifier of this container.
     *
     * @return UUID
     */
    public UUID id() { return uid; }

    /**
     * Returns the GUI this container belongs to.
     *
     * @return owner GUI
     */
    public GUI gui() { return gui; }

    /**
     * Returns the parent container, or {@code null} for the root container.
     *
     * @return parent container, may be {@code null}
     */
    public GUIContainer parent() { return parentContainer; }

    /**
     * Returns the direct children of this container.
     *
     * @return mutable child list
     */
    public List<GUIContainer> getChildren() { return children; }

    /**
     * Returns a list containing this container followed by all its direct children.
     *
     * @return list of this container and its children
     */
    public List<GUIContainer> getMeAndChildren() {
        List<GUIContainer> all = new ArrayList<>();
        all.add(this);
        all.addAll(children);
        return all;
    }

    /**
     * Walks up the parent chain and returns the topmost (root) container.
     *
     * @return root container (never {@code null})
     */
    public GUIContainer root() {
        GUIContainer c = this;
        while (c.parentContainer != null) c = c.parentContainer;
        return c;
    }

    /**
     * Convenience method: returns the root container together with all its
     * direct children.
     *
     * @return root and its direct children
     */
    public List<GUIContainer> getRootAndItsChildren() { return root().getMeAndChildren(); }

    /**
     * Returns the total number of slots in this container ({@code width × height}).
     *
     * @return total slot count
     */
    public int totalSize() { return size.width * size.height; }

    // -------------------------------------------------------------------------
    // Child containers
    // -------------------------------------------------------------------------

    /**
     * Creates a new child container, registers it as a child of this container,
     * and returns it for further configuration.
     *
     * @return new child container
     */
    public GUIContainer createContainer() {
        GUIContainer child = new GUIContainer(gui(), this);
        addChild(child);
        return child;
    }

    /**
     * Creates a child container with the given dimensions.
     *
     * @param width  number of columns
     * @param height number of rows
     * @return new child container
     */
    public GUIContainer createContainer(int width, int height) {
        return createContainer().size(width, height);
    }

    /**
     * Creates a child container with the given size.
     *
     * @param size size value
     * @return new child container
     */
    public GUIContainer createContainer(Size size) {
        return createContainer().size(size);
    }

    /**
     * Creates a child container at the given position with the given dimensions.
     *
     * @param x      column offset inside this container
     * @param y      row offset inside this container
     * @param width  number of columns
     * @param height number of rows
     * @return new child container
     */
    public GUIContainer createContainer(int x, int y, int width, int height) {
        return createContainer().position(x, y).size(width, height);
    }

    /**
     * Creates a child container at the given position with the given size.
     *
     * @param position position vector inside this container
     * @param size     size value
     * @return new child container
     */
    public GUIContainer createContainer(Vector2 position, Size size) {
        return createContainer().position(position).size(size);
    }

    /**
     * Appends an already-constructed container as a direct child of this one.
     *
     * @param child child container to add
     * @return this container (for chaining)
     */
    public GUIContainer addChild(GUIContainer child) { children.add(child); return this; }

    // -------------------------------------------------------------------------
    // Item factory helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a new {@link GUIItem} builder pre-associated with this container
     * and the parent GUI.
     *
     * <p>Use the returned builder to set the name, lore, texture, etc., then
     * place it in the inventory with {@link #addItem(GUIItem, int)} or
     * {@link #addItem(GUIItem, int, int)}.</p>
     *
     * <pre>{@code
     * container.addItem(container.item(Material.DIAMOND).name("Gem"), 4);
     * }</pre>
     *
     * @param material item material
     * @return new {@link GUIItem} builder bound to this container
     */
    public GUIItem item(Material material) {
        return new GUIItem(gui, this, material);
    }

    /**
     * Creates a named item and places it at the given slot in a single call.
     *
     * @param material item material
     * @param name     display name (MiniMessage / legacy {@code &} codes supported)
     * @param slot     local slot index (0-based, row-first)
     * @return the created {@link GUIItem}
     */
    public GUIItem createItem(Material material, String name, int slot) {
        GUIItem item = new GUIItem(gui, this, material).name(name);
        addItem(item, slot);
        return item;
    }

    /**
     * Creates a nameless item and places it at the given grid position.
     *
     * @param material item material
     * @param x        column (0-based)
     * @param y        row (0-based)
     * @return the created {@link GUIItem}
     * @deprecated Use {@link #item(Material)} for a fluent builder, then {@link #addItem}.
     */
    @Deprecated
    public GUIItem createItem(Material material, int x, int y) {
        GUIItem item = new GUIItem(gui, this, material);
        addItem(item, x, y);
        return item;
    }

    // -------------------------------------------------------------------------
    // Adding items
    // -------------------------------------------------------------------------

    /**
     * Places a {@link GUIItem} at the given grid coordinates.
     * The coordinates are converted to a local slot index and delegated to
     * {@link #addItem(GUIItem, int)}.
     *
     * @param item item to place
     * @param x    column (0-based, must be within {@code [0, width)})
     * @param y    row (0-based, must be within {@code [0, height)})
     * @return this container (for chaining)
     * @throws IllegalArgumentException if {@code x} or {@code y} is out of bounds
     */
    public GUIContainer addItem(GUIItem item, int x, int y) {
        if (x < 0 || x >= size.width || y < 0 || y >= size.height) {
            throw new IllegalArgumentException(
                    "Position (" + x + "," + y + ") out of bounds for container " + size);
        }
        return addItem(item, y * size.width + x);
    }

    /**
     * Places a {@link GUIItem} at the given local slot index.
     *
     * <p>The item is built immediately via {@link GUIItem#build()} and the
     * resulting {@link ItemStack} is written to the backing {@link Inventory}.</p>
     *
     * @param item item to place
     * @param slot local slot index (0-based, row-first, must be within
     *             {@code [0, totalSize())})
     * @return this container (for chaining)
     * @throws IllegalArgumentException if {@code slot} is out of bounds
     */
    public GUIContainer addItem(GUIItem item, int slot) {
        if (slot < 0 || slot >= totalSize()) {
            throw new IllegalArgumentException(
                    "Slot " + slot + " out of bounds for container (size=" + totalSize() + ")");
        }

        ItemStack builtItem = item.build();
        notItems.put(item.id(), item);
        items.put(slot, builtItem);
        setItemToSlot(slot, builtItem);
        return this;
    }

    // -------------------------------------------------------------------------
    // Button convenience methods
    // -------------------------------------------------------------------------

    /**
     * Registers a click handler on the item and places it at the given slot.
     *
     * @param item   item to use as the button
     * @param slot   local slot index
     * @param action click handler
     * @return this container
     */
    public GUIContainer addButton(GUIItem item, int slot,
                                  BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addItem(item.action(action), slot);
    }

    /**
     * Registers a click handler on the item and places it at the given grid position.
     *
     * @param item   item to use as the button
     * @param x      column (0-based)
     * @param y      row (0-based)
     * @param action click handler
     * @return this container
     */
    public GUIContainer addButton(GUIItem item, int x, int y,
                                  BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addItem(item.action(action), x, y);
    }

    /**
     * Creates a button from a material and a {@link Text} name, and places it
     * at the given slot.
     *
     * @param material button material
     * @param name     display name builder
     * @param slot     local slot index
     * @param action   click handler
     * @return this container
     */
    public GUIContainer addButton(Material material, Text name, int slot,
                                  BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addItem(new GUIItem(gui, this, material).name(name).action(action), slot);
    }

    /**
     * Creates a button from a material and a string name, and places it at
     * the given slot.
     *
     * @param material button material
     * @param name     display name (MiniMessage / legacy {@code &} codes supported)
     * @param slot     local slot index
     * @param action   click handler
     * @return this container
     */
    public GUIContainer addButton(Material material, String name, int slot,
                                  BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addButton(material, Text.of(name), slot, action);
    }

    /**
     * Creates a button from a material and a {@link Text} name, and places it
     * at the given grid position.
     *
     * @param material button material
     * @param name     display name builder
     * @param x        column (0-based)
     * @param y        row (0-based)
     * @param action   click handler
     * @return this container
     */
    public GUIContainer addButton(Material material, Text name, int x, int y,
                                  BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addButton(material, name, y * size.width + x, action);
    }

    /**
     * Creates a button from a material and a string name, and places it at
     * the given grid position.
     *
     * @param material button material
     * @param name     display name (MiniMessage / legacy {@code &} codes supported)
     * @param x        column (0-based)
     * @param y        row (0-based)
     * @param action   click handler
     * @return this container
     */
    public GUIContainer addButton(Material material, String name, int x, int y,
                                  BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return addButton(material, Text.of(name), x, y, action);
    }

    // -------------------------------------------------------------------------
    // Lookup
    // -------------------------------------------------------------------------

    /**
     * Looks up a {@link GUIItem} by its UUID, searching this container first,
     * then recursing into children.
     *
     * @param itemId item UUID (as stored in the item's persistent data)
     * @return matching item, or {@code null} if not found
     */
    public GUIItem getItem(UUID itemId) {
        GUIItem item = notItems.get(itemId);
        if (item != null) return item;
        for (GUIContainer child : children) {
            item = child.getItem(itemId);
            if (item != null) return item;
        }
        return null;
    }

    /**
     * Returns the built {@link ItemStack} at the given local slot, searching
     * this container first, then recursing into children.
     *
     * @param slot local slot index
     * @return item stack, or {@code null} if the slot is empty
     */
    public ItemStack getItem(int slot) {
        ItemStack item = items.get(slot);
        if (item != null) return item;
        for (GUIContainer child : children) {
            item = child.getItem(slot);
            if (item != null) return item;
        }
        return null;
    }

    /**
     * Returns the click handler associated with the item identified by the
     * given UUID.  If no handler is registered (or the item is not found),
     * a no-op lambda is returned instead of {@code null}.
     *
     * @param itemId item UUID
     * @return registered handler, or a no-op if none is found
     */
    public BiConsumer<InventoryClickEvent, GUIContainer> getHandler(UUID itemId) {
        GUIItem item = getItem(itemId);
        if (item != null && item.action() != null) return item.action();
        return (event, container) -> {};
    }

    /**
     * Returns a shallow copy of all local {@link ItemStack}s keyed by local slot index.
     *
     * @return copy of the item map
     */
    public Map<Integer, ItemStack> getAllItems() {
        return new HashMap<>(items);
    }

    // -------------------------------------------------------------------------
    // Click handling
    // -------------------------------------------------------------------------

    /**
     * Translates an absolute inventory click to a local slot and delegates to
     * {@link #handleItemClick(InventoryClickEvent, int)}.
     *
     * @param event inventory click event
     * @return {@code true} if a handler was invoked
     */
    public boolean handleClick(InventoryClickEvent event) {
        int slot = event.getSlot();
        int x = slot % gui().rowSize();
        int y = slot / gui().rowSize();
        int localSlot = (y - position.y) * size.width + (x - position.x);
        return handleItemClick(event, localSlot);
    }

    /**
     * Directly invokes the click handler for the given {@link GUIItem},
     * if one is registered.
     *
     * @param event inventory click event
     * @param item  item whose handler should be called
     * @return {@code true} if a handler was found and invoked
     */
    public boolean handleClick(InventoryClickEvent event, GUIItem item) {
        BiConsumer<InventoryClickEvent, GUIContainer> handler = getHandler(item.id());
        if (handler != null) {
            handler.accept(event, this);
            return true;
        }
        return false;
    }

    /**
     * Attempts to handle a click at the given local slot.
     *
     * <p>The clicked {@link ItemStack} is inspected for the embedded item UUID.
     * If found, the associated handler is called on this container.
     * If not found locally, each child container is checked recursively.</p>
     *
     * @param event     inventory click event
     * @param localSlot slot index relative to this container (row-first)
     * @return {@code true} if a handler was found and invoked anywhere in the tree
     */
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
            int relX = localSlot % size.width - child.position.x;
            int relY = localSlot / size.width - child.position.y;
            int childSlot = relY * child.size.width + relX;
            if (child.handleItemClick(event, childSlot)) return true;
        }

        return false;
    }

    // -------------------------------------------------------------------------
    // Slot / inventory helpers
    // -------------------------------------------------------------------------

    /**
     * Converts a local slot index to an absolute inventory slot and writes the
     * item to the backing {@link Inventory}.
     *
     * <p>If the computed absolute slot is out of the inventory's range and
     * {@link #isWrapped()} is {@code true}, the slot is wrapped modulo the
     * inventory size before writing.</p>
     *
     * @param slot local slot index
     * @param item item stack to write
     */
    private void setItemToSlot(int slot, ItemStack item) {
        Inventory inv = gui.getBuiltInventory();
        int x = slot % size.width;
        int y = slot / size.width;
        int invSlot = (position.y + y) * gui().rowSize() + (position.x + x);

        if (invSlot >= 0 && invSlot < inv.getSize()) {
            inv.setItem(invSlot, item);
        } else if (wrapped) {
            int wrappedSlot = invSlot % inv.getSize();
            if (wrappedSlot < 0) wrappedSlot += inv.getSize();
            inv.setItem(wrappedSlot, item);
        }
    }

    /**
     * Reads the item UUID stored in an {@link ItemStack}'s
     * {@link org.bukkit.persistence.PersistentDataContainer} under the
     * {@link GUI#ITEM_UUID_KEY} key.
     *
     * @param item item stack to inspect (may be {@code null})
     * @return stored UUID, or {@code null} if absent or the item has no meta
     */
    public UUID getItemIdFromItemStack(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(gui().getPlugin(), GUI.ITEM_UUID_KEY);
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        if (!pdc.has(key, PersistentDataType.STRING)) return null;
        return UUID.fromString(pdc.get(key, PersistentDataType.STRING));
    }

    /**
     * Moves an item from one local slot to another.
     * If the source slot is empty, this method does nothing.
     *
     * @param fromSlot source slot index
     * @param toSlot   destination slot index
     */
    public void moveItem(int fromSlot, int toSlot) {
        if (!items.containsKey(fromSlot)) return;
        ItemStack item = items.remove(fromSlot);
        items.put(toSlot, item);
        setItemToSlot(toSlot, item);
    }

    // -------------------------------------------------------------------------
    // Refresh / render
    // -------------------------------------------------------------------------

    /**
     * Re-writes all items in this container (and all children) to the backing
     * {@link Inventory} without rebuilding the whole GUI.
     *
     * <p>Use this after mutating item state to push changes to players who
     * already have the GUI open.</p>
     */
    public void refresh() {
        if (gui.getBuiltInventory() == null) return;
        for (Map.Entry<Integer, ItemStack> e : items.entrySet()) {
            setItemToSlot(e.getKey(), e.getValue());
        }
        for (GUIContainer child : children) {
            child.refresh();
        }
    }

    /**
     * Renders all items in this container (and all children) into the given
     * {@link Inventory} at absolute positions derived from {@code parentOffset}.
     *
     * <p>This is called recursively by the GUI's build pipeline and should not
     * normally be called manually.</p>
     *
     * @param inventory    target inventory to write items into
     * @param parentOffset absolute position of the parent container's origin,
     *                     used to compute this container's absolute position
     */
    public void render(Inventory inventory, Vector2 parentOffset) {
        Vector2 abs = new Vector2(parentOffset.x + position.x, parentOffset.y + position.y);

        for (Map.Entry<Integer, ItemStack> e : items.entrySet()) {
            int localSlot = e.getKey();
            int x = localSlot % size.width;
            int y = localSlot / size.width;
            int invSlot = (abs.y + y) * gui().rowSize() + (abs.x + x);

            if (invSlot >= 0 && invSlot < inventory.getSize()) {
                inventory.setItem(invSlot, e.getValue());
            } else if (isWrapped()) {
                int wrappedSlot = invSlot % inventory.getSize();
                if (wrappedSlot < 0) wrappedSlot += inventory.getSize();
                inventory.setItem(wrappedSlot, e.getValue());
            }
        }

        for (GUIContainer child : children) {
            child.render(inventory, abs);
        }
    }
}
package dev.notmarra.notlib.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import dev.notmarra.notlib.NotLib;
import dev.notmarra.notlib.chat.Colors;
import dev.notmarra.notlib.chat.Text;
import dev.notmarra.notlib.gui.animations.*;
import dev.notmarra.notlib.gui.utils.Size;
import dev.notmarra.notlib.gui.utils.Vector2;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import net.kyori.adventure.text.Component;

/**
 * Central class for building and displaying custom inventory GUIs.
 *
 * <p>A {@code GUI} owns a tree of {@link GUIContainer}s, a set of
 * {@link GUIAnimation}s, and optional lifecycle callbacks for open/close events.
 * It implements {@link InventoryHolder} so it can be passed directly to
 * Bukkit's inventory API.</p>
 *
 * <h2>Quick start</h2>
 * <pre>{@code
 * GUI gui = GUI.create("My Shop")
 *     .rows(3)
 *     .pattern(List.of(
 *         "#########",
 *         "#AAAAAAA#",
 *         "#########"
 *     ))
 *     .emptySlotChars(List.of('#'))
 *     .onPatternMatch(info -> switch (info.ch) {
 *         case '#' -> new GUIItem(info.gui, Material.GRAY_STAINED_GLASS_PANE).name(" ");
 *         case 'A' -> new GUIItem(info.gui, Material.DIAMOND).name("Buy Diamond")
 *                         .onClick((e, c) -> player.sendMessage("Bought!"));
 *         default  -> null;
 *     });
 *
 * gui.open(player);
 * }</pre>
 *
 * <h2>Pattern matching</h2>
 * <p>Call {@link #pattern(List)} with a list of strings where each character
 * represents one inventory slot. Register {@link #onPatternMatch(Function)} to
 * map each character to a {@link GUIItem}. Characters listed via
 * {@link #emptySlotChars(List)} are skipped and the callback is not invoked
 * for them.</p>
 *
 * <h2>Animations</h2>
 * <p>Animation instances are created through the {@code a*()} / {@code easeInOut()}
 * factory methods (e.g. {@link #aPulse(long, long)}). Each factory registers the
 * animation automatically. Call {@link #cancelAllAnimations()} when the GUI closes
 * to avoid orphaned tasks.</p>
 *
 * <h2>Inventory types</h2>
 * <p>By default a GUI is a double-chest (6 rows × 9 columns).
 * Use {@link #rows(int)} for other chest sizes, or {@link #type(InventoryType)}
 * for non-chest inventories such as hoppers, dispensers, etc.</p>
 */
public class GUI implements InventoryHolder {

    /**
     * The {@link org.bukkit.persistence.PersistentDataContainer} key used to
     * embed a {@link GUIItem}'s UUID into its {@link ItemStack} meta.
     * This key is read by {@link GUIListener} to route click events.
     */
    public static final String ITEM_UUID_KEY = "notlib-gui-item-uuid";

    /** The owning plugin instance, used for scheduler calls and namespaced keys. */
    public NotLib plugin;

    /**
     * The Bukkit inventory type, or {@code null} when the GUI is a double-chest
     * (the default). A {@code null} value triggers creation via
     * {@link org.bukkit.Server#createInventory(InventoryHolder, int, Component)}.
     */
    @Nullable private InventoryType guiType;

    /** Title shown in the inventory's title bar. */
    private Component guiTitle;

    /** Root container that spans the entire inventory grid. */
    private final GUIContainer rootContainer;

    /**
     * Cached {@link Inventory} instance. Created on the first call to
     * {@link #getBuiltInventory()} and reused thereafter.
     */
    private Inventory builtInventory;

    /**
     * Pattern rows used for slot mapping. Each string represents one row;
     * each character represents one slot.
     */
    private List<String> pattern = new ArrayList<>();

    /**
     * Characters in the pattern that should be treated as empty/filler slots.
     * The {@link #onPatternMatch} callback is not invoked for these characters.
     */
    private List<Character> patternEmptySlotChars = new ArrayList<>();

    /**
     * Callback invoked once per non-empty pattern slot during
     * {@link #patternMatchGui()}. Returns the {@link GUIItem} to place at that
     * slot, or {@code null} to leave it empty.
     */
    private Function<GUIPatternMatchInfo, GUIItem> onPatternMatch;

    /**
     * Optional callback fired by {@link GUIListener} when the inventory is opened.
     * Public so the listener can access it without a getter.
     */
    public Consumer<InventoryOpenEvent> onOpen;

    /**
     * Optional callback fired by {@link GUIListener} when the inventory is closed.
     * Public so the listener can access it without a getter.
     */
    public Consumer<InventoryCloseEvent> onClose;

    /**
     * All animations currently registered to this GUI.
     * Animations add themselves via {@link #registerAnimation(GUIAnimation)}.
     */
    public final List<GUIAnimation> animations = new ArrayList<>();

    /**
     * Maps each supported {@link InventoryType} to its logical grid {@link Size}
     * (width × height in slots). Populated in the constructor.
     */
    public final Map<InventoryType, Size> inventorySizes = new HashMap<>();

    // -------------------------------------------------------------------------
    // Constructor
    // -------------------------------------------------------------------------

    /**
     * Creates a new GUI with default settings: double-chest size (9 × 6),
     * title "GUI", and no pattern or animations.
     *
     * <p>Prefer the static factory methods {@link #create()},
     * {@link #create(String)}, etc. for a more readable call site.</p>
     */
    public GUI() {
        plugin = NotLib.getInstance();
        guiTitle = Text.of("GUI").build();
        rootContainer = new GUIContainer(this);
        rows(6);
        inventorySizes.put(InventoryType.CHEST,        Size.of(9, 3));
        inventorySizes.put(InventoryType.DISPENSER,    Size.of(3, 3));
        inventorySizes.put(InventoryType.DROPPER,      Size.of(3, 3));
        inventorySizes.put(InventoryType.HOPPER,       Size.of(5, 1));
        inventorySizes.put(InventoryType.BARREL,       Size.of(9, 3));
        inventorySizes.put(InventoryType.SHULKER_BOX,  Size.of(9, 3));
        inventorySizes.put(InventoryType.SMOKER,       Size.of(3, 1));
        inventorySizes.put(InventoryType.BLAST_FURNACE,Size.of(3, 1));
        inventorySizes.put(InventoryType.BREWING,      Size.of(5, 1));
        inventorySizes.put(InventoryType.ENCHANTING,   Size.of(2, 1));
        inventorySizes.put(InventoryType.ANVIL,        Size.of(3, 1));
        inventorySizes.put(InventoryType.GRINDSTONE,   Size.of(3, 1));
        inventorySizes.put(InventoryType.CARTOGRAPHY,  Size.of(3, 1));
        inventorySizes.put(InventoryType.STONECUTTER,  Size.of(2, 1));
        inventorySizes.put(InventoryType.LOOM,         Size.of(4, 1));
        inventorySizes.put(InventoryType.CRAFTING,     Size.of(2, 2));
        inventorySizes.put(InventoryType.FURNACE,      Size.of(3, 1));
        inventorySizes.put(InventoryType.WORKBENCH,    Size.of(3, 3));
        inventorySizes.put(InventoryType.SMITHING,     Size.of(4, 1));
    }

    // -------------------------------------------------------------------------
    // Type / size / title
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} when this GUI uses a chest or double-chest inventory.
     * A {@code null} {@link #guiType} is treated as a double-chest.
     *
     * @return {@code true} for chest-type GUIs
     */
    public boolean isChest() { return guiType == InventoryType.CHEST || guiType == null; }

    /**
     * Sets the inventory type to a non-chest type such as
     * {@link InventoryType#HOPPER}, {@link InventoryType#DISPENSER}, etc.
     * The size is automatically adjusted to match the type.
     * If {@code type} is not in {@link #inventorySizes}, this call is a no-op.
     *
     * @param type target inventory type
     * @return this GUI
     */
    public GUI type(InventoryType type) {
        if (!inventorySizes.containsKey(type)) return this;
        guiType = type;
        return size(inventorySizes.get(type));
    }

    /**
     * Returns the number of columns in a single row of this GUI's inventory.
     * Always {@code 9} for chest/double-chest; derived from {@link #inventorySizes}
     * for other types.
     *
     * @return row width in slots
     */
    public int rowSize() {
        if (guiType == null) return 9;
        return inventorySizes.get(guiType).width;
    }

    /**
     * Returns {@code true} when both a pattern and a match callback have been
     * configured, meaning pattern matching will run on the next build.
     *
     * @return {@code true} if pattern matching is active
     */
    public boolean isPatternMatched() { return !this.pattern.isEmpty() && this.onPatternMatch != null; }

    /**
     * Sets the inventory title from a legacy/MiniMessage string.
     *
     * @param title title text (supports MiniMessage and legacy {@code &} codes)
     * @return this GUI
     */
    public GUI title(String title) { return title(Text.of(title)); }

    /**
     * Sets the inventory title from a {@link Text} builder.
     *
     * @param title title text builder
     * @return this GUI
     */
    public GUI title(Text title) { return title(title.build()); }

    /**
     * Sets the inventory title from a raw Adventure {@link Component}.
     *
     * @param title title component
     * @return this GUI
     */
    public GUI title(Component title) { this.guiTitle = title; return this; }

    /**
     * Sets the position of the root container (shifts all items by the given offset).
     *
     * @param x column offset
     * @return this GUI
     */
    public GUI position(int x) { rootContainer.position(x); return this; }

    /**
     * Sets the position of the root container.
     *
     * @param x column offset
     * @param y row offset
     * @return this GUI
     */
    public GUI position(int x, int y) { rootContainer.position(x, y); return this; }

    /**
     * Sets the position of the root container from a {@link Vector2}.
     *
     * @param position position vector
     * @return this GUI
     */
    public GUI position(Vector2 position) { rootContainer.position(position); return this; }

    /**
     * Sets the GUI size to a square ({@code width × width}).
     *
     * @param width width and height in slots
     * @return this GUI
     */
    public GUI size(int width) { return size(Size.of(width)); }

    /**
     * Sets the GUI size explicitly.
     *
     * @param width  number of columns
     * @param height number of rows
     * @return this GUI
     */
    public GUI size(int width, int height) { return size(Size.of(width, height)); }

    /**
     * Sets the GUI size from a {@link Size} value.
     *
     * @param size size value
     * @return this GUI
     */
    public GUI size(Size size) { rootContainer.size(size); return this; }

    /**
     * Sets the number of rows for a chest-type GUI.
     *
     * <ul>
     *   <li>For a single chest ({@link InventoryType#CHEST}): 1–3 rows.</li>
     *   <li>For a double-chest ({@code null} type, the default): 1–6 rows.</li>
     * </ul>
     * Out-of-range values are silently ignored.
     * Has no effect when the GUI is not a chest type.
     *
     * @param rows number of rows
     * @return this GUI
     */
    public GUI rows(int rows) {
        if (!isChest()) return this;
        if (guiType == InventoryType.CHEST) {
            if (rows < 1 || rows > 3) return this;
        } else {
            if (rows < 1 || rows > 6) return this;
        }
        return size(9, rows);
    }

    // -------------------------------------------------------------------------
    // Pattern
    // -------------------------------------------------------------------------

    /**
     * Sets the slot pattern from a single multi-line string.
     * The string is split on newline characters so each line becomes one row.
     *
     * <p>Example — a 3-row chest with a border of glass panes:</p>
     * <pre>{@code
     * gui.pattern(
     *     "#########\n" +
     *     "#AAAAAAA#\n" +
     *     "#########"
     * );
     * }</pre>
     *
     * @param pattern newline-delimited pattern string
     * @return this GUI
     */
    public GUI pattern(String pattern) { return pattern(Arrays.asList(pattern.split("\n"))); }

    /**
     * Sets the slot pattern from a list of row strings.
     * Each string represents one row; each character represents one slot.
     * The pattern must not be wider or taller than the GUI size.
     *
     * @param pattern list of pattern rows
     * @return this GUI
     */
    public GUI pattern(List<String> pattern) {
        this.pattern = pattern;
        return this;
    }

    /**
     * Declares which pattern characters represent empty/filler slots.
     * The {@link #onPatternMatch} callback is <em>not</em> invoked for these
     * characters, so you can leave those slots blank without returning {@code null}
     * from the callback every time.
     *
     * @param characters list of filler characters (e.g. {@code List.of(' ', '#')})
     * @return this GUI
     */
    public GUI emptySlotChars(List<Character> characters) {
        this.patternEmptySlotChars = characters;
        return this;
    }

    /**
     * Returns the raw pattern row list.
     *
     * @return pattern rows (may be empty if no pattern was set)
     */
    public List<String> pattern() { return pattern; }

    /**
     * Returns the current pattern-match callback.
     *
     * @return callback, or {@code null} if none was registered
     */
    public Function<GUIPatternMatchInfo, GUIItem> onPatternMatch() { return onPatternMatch; }

    /**
     * Returns the list of characters that are treated as empty pattern slots.
     *
     * @return filler characters
     */
    public List<Character> emptySlotCharacters() { return this.patternEmptySlotChars; }

    // -------------------------------------------------------------------------
    // Size / state accessors
    // -------------------------------------------------------------------------

    /**
     * Returns the logical size of this GUI (width × height in slots).
     *
     * @return GUI size
     */
    public Size size() { return rootContainer.size(); }

    /**
     * Returns the total number of slots ({@code width × height}).
     *
     * @return total slot count
     */
    public int totalSize() { return rootContainer.totalSize(); }

    /**
     * Returns the plugin instance that owns this GUI.
     *
     * @return owning plugin
     */
    public NotLib getPlugin() { return plugin; }

    // -------------------------------------------------------------------------
    // Containers
    // -------------------------------------------------------------------------

    /**
     * Creates a new child container inside the root container and returns it.
     *
     * @return new child container
     */
    public GUIContainer createContainer() {
        GUIContainer container = new GUIContainer(this, rootContainer);
        rootContainer.addChild(container);
        return container;
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
     * Creates a child container at a specific position with given dimensions.
     *
     * @param x      column offset (0-based)
     * @param y      row offset (0-based)
     * @param width  number of columns
     * @param height number of rows
     * @return new child container
     */
    public GUIContainer createContainer(int x, int y, int width, int height) {
        return createContainer().position(x, y).size(width, height);
    }

    /**
     * Creates a child container at a specific position with the given size.
     *
     * @param position position vector (relative to the root container)
     * @param size     size value
     * @return new child container
     */
    public GUIContainer createContainer(Vector2 position, Size size) {
        return createContainer().position(position).size(size);
    }

    // -------------------------------------------------------------------------
    // Items
    // -------------------------------------------------------------------------

    /**
     * Places a {@link GUIItem} at the given absolute slot in the root container.
     *
     * @param item item to place
     * @param slot absolute slot index (0-based, row-first)
     * @return this GUI
     */
    public GUI addItem(GUIItem item, int slot) {
        rootContainer.addItem(item, slot);
        return this;
    }

    /**
     * Places a {@link GUIItem} at the given grid coordinates in the root container.
     *
     * @param item item to place
     * @param x    column (0-based)
     * @param y    row (0-based)
     * @return this GUI
     */
    public GUI addItem(GUIItem item, int x, int y) {
        rootContainer.addItem(item, x, y);
        return this;
    }

    /**
     * Adds a button (item with a click handler) to the root container at the given slot.
     *
     * @param item   item to use as the button
     * @param slot   absolute slot index
     * @param action click handler
     * @return this GUI
     */
    public GUI addButton(GUIItem item, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(item, slot, action);
        return this;
    }

    /**
     * Creates a button from a material and a {@link Text} name, and places it
     * at the given slot in the root container.
     *
     * @param material button material
     * @param name     display name builder
     * @param slot     absolute slot index
     * @param action   click handler
     * @return this GUI
     */
    public GUI addButton(Material material, Text name, int slot,
                         BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
        return this;
    }

    /**
     * Creates a button from a material and a string name, and places it
     * at the given slot in the root container.
     *
     * @param material button material
     * @param name     display name (MiniMessage / legacy {@code &} codes supported)
     * @param slot     absolute slot index
     * @param action   click handler
     * @return this GUI
     */
    public GUI addButton(Material material, String name, int slot,
                         BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
        return this;
    }

    /**
     * Creates a button from a material and a string name, and places it
     * at the given grid position in the root container.
     *
     * @param material button material
     * @param name     display name
     * @param x        column (0-based)
     * @param y        row (0-based)
     * @param action   click handler
     * @return this GUI
     */
    public GUI addButton(Material material, String name, int x, int y,
                         BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }

    /**
     * Creates a button from a material and a {@link Text} name, and places it
     * at the given grid position in the root container.
     *
     * @param material button material
     * @param name     display name builder
     * @param x        column (0-based)
     * @param y        row (0-based)
     * @param action   click handler
     * @return this GUI
     */
    public GUI addButton(Material material, Text name, int x, int y,
                         BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }

    /**
     * Creates a bare {@link GUIItem} builder pre-associated with this GUI and
     * the root container. Use the returned builder to configure the item, then
     * add it with {@link #addItem(GUIItem, int)}.
     *
     * @param material item material
     * @return new {@link GUIItem} builder
     */
    public GUIItem createItem(Material material) {
        return new GUIItem(this, rootContainer, material);
    }

    // -------------------------------------------------------------------------
    // Animations
    // -------------------------------------------------------------------------

    /**
     * Registers an animation with this GUI so it is tracked and can be
     * cancelled via {@link #cancelAllAnimations()}.
     * Called automatically by {@link GUIAnimation} constructors.
     *
     * @param animation animation to register
     * @return the same animation (for chaining)
     */
    public GUIAnimation registerAnimation(GUIAnimation animation) {
        animations.add(animation);
        return animation;
    }

    /**
     * Cancels and removes all animations currently registered to this GUI.
     * Call this when the GUI is closed to prevent orphaned BukkitTasks.
     */
    public void cancelAllAnimations() {
        for (GUIAnimation animation : animations) animation.cancel();
        animations.clear();
    }

    /**
     * Removes a single animation from the registry without cancelling it.
     * Called internally by {@link GUIAnimation#cancel()}.
     *
     * @param animation animation to deregister
     */
    public void removeAnimation(GUIAnimation animation) {
        animations.remove(animation);
    }

    /**
     * Returns all animations currently registered to this GUI.
     *
     * @return live animation list
     */
    public List<GUIAnimation> getAnimations() { return animations; }

    // -------------------------------------------------------------------------
    // Inventory lifecycle
    // -------------------------------------------------------------------------

    /**
     * Returns the backing {@link Inventory}, building it on the first call.
     * Pattern matching is applied during the initial build and not re-applied
     * on subsequent calls.
     *
     * @return built inventory (never {@code null})
     */
    public Inventory getBuiltInventory() {
        if (builtInventory == null) {
            builtInventory = build();
            patternMatchGui();
        }
        return builtInventory;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Delegates to {@link #getBuiltInventory()}.</p>
     */
    @Override
    public Inventory getInventory() { return getBuiltInventory(); }

    /**
     * Re-renders all containers into the backing inventory.
     * Use this after modifying item state to push updates to players who
     * currently have the GUI open without closing and reopening it.
     */
    public void refresh() {
        builtInventory.clear();
        rootContainer.refresh();
    }

    /**
     * Constructs and returns a brand-new {@link Inventory} from the current
     * state of the root container tree.
     * Called once by {@link #getBuiltInventory()} and should not be called
     * manually in normal usage.
     *
     * @return newly created inventory
     */
    public Inventory build() {
        Inventory inventory;
        if (guiType != null) {
            inventory = plugin.getServer().createInventory(this, guiType, guiTitle);
        } else {
            inventory = plugin.getServer().createInventory(this, rootContainer.totalSize(), guiTitle);
        }
        rootContainer.render(inventory, Vector2.zero());
        return inventory;
    }

    /**
     * Opens this GUI for the given player via the registered {@link GUIListener}.
     * If no listener is registered in the plugin, a warning is logged and the
     * GUI is not opened.
     *
     * @param player player to open the GUI for
     */
    public void open(Player player) {
        GUIListener listener = plugin.getGUIListener();
        if (listener == null) {
            getPlugin().getComponentLogger().warn(
                    Text.of("GUI listener not registered", Colors.ORANGE.get()).build());
            return;
        }
        listener.openGUI(player, this);
    }

    /**
     * Closes this GUI for the given player if it is currently open.
     * Does nothing if the player has a different inventory open.
     *
     * @param player player whose GUI should be closed
     */
    public void close(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory != null && inventory.getHolder() == this) {
            player.closeInventory();
        }
    }

    // -------------------------------------------------------------------------
    // Callbacks
    // -------------------------------------------------------------------------

    /**
     * Registers a callback that is invoked by {@link GUIListener} when the
     * inventory is opened by a player.
     *
     * @param onOpen open callback
     * @return this GUI
     */
    public GUI onOpen(Consumer<InventoryOpenEvent> onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    /**
     * Registers a callback that is invoked by {@link GUIListener} when the
     * inventory is closed by a player.
     *
     * @param onClose close callback
     * @return this GUI
     */
    public GUI onClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
        return this;
    }

    /**
     * Registers the callback used to resolve each non-empty pattern character
     * to a {@link GUIItem}.
     *
     * <p>The callback receives a {@link GUIPatternMatchInfo} describing the
     * current character, its position, and its occurrence index. Return the
     * item to place at that slot, or {@code null} to leave it empty.</p>
     *
     * @param onPatternMatch pattern match callback
     * @return this GUI
     */
    public GUI onPatternMatch(Function<GUIPatternMatchInfo, GUIItem> onPatternMatch) {
        this.onPatternMatch = onPatternMatch;
        return this;
    }

    // -------------------------------------------------------------------------
    // Click handling
    // -------------------------------------------------------------------------

    /**
     * Delegates a raw click event to the root container's click handler.
     * Returns {@code false} when the click did not originate in this GUI's inventory.
     *
     * @param event inventory click event
     * @return {@code true} if a handler was found and invoked
     */
    public boolean handleClick(InventoryClickEvent event) {
        if (event.getClickedInventory().getHolder() != this) return false;
        return rootContainer.handleClick(event);
    }

    /**
     * Invokes the click handler for a specific {@link GUIItem} directly.
     * Returns {@code false} when the click did not originate in this GUI's inventory.
     *
     * @param event inventory click event
     * @param item  item whose handler should be invoked
     * @return {@code true} if a handler was found and invoked
     */
    public boolean handleClick(InventoryClickEvent event, GUIItem item) {
        if (event.getClickedInventory().getHolder() != this) return false;
        return rootContainer.handleClick(event, item);
    }

    // -------------------------------------------------------------------------
    // Item lookup
    // -------------------------------------------------------------------------

    /**
     * Extracts the item UUID from an {@link ItemStack}'s persistent data and
     * delegates to the root container lookup.
     *
     * @param item item stack to inspect (may be {@code null})
     * @return stored UUID, or {@code null} if absent
     */
    public UUID getItemIdFromItemStack(ItemStack item) { return rootContainer.getItemIdFromItemStack(item); }

    /**
     * Finds and returns the {@link GUIItem} registered under the given UUID,
     * searching the entire container tree.
     *
     * @param itemId item UUID
     * @return matching item, or {@code null} if not found
     */
    public GUIItem getItem(UUID itemId) { return rootContainer.getItem(itemId); }

    /**
     * Returns the built {@link ItemStack} at the given absolute slot,
     * searching the entire container tree.
     *
     * @param slot absolute slot index
     * @return item stack, or {@code null} if the slot is empty
     */
    public ItemStack getItem(int slot) { return rootContainer.getItem(slot); }

    // -------------------------------------------------------------------------
    // Pattern matching (internal)
    // -------------------------------------------------------------------------

    /**
     * Iterates over every character in the configured pattern and calls
     * {@link #onPatternMatch} for each non-empty slot.
     * Called once during {@link #getBuiltInventory()} after the inventory is built.
     * Has no effect when {@link #isPatternMatched()} returns {@code false}.
     */
    private void patternMatchGui() {
        if (!isPatternMatched()) return;
        Size guiSize = size();

        if (!isChest()) {
            NotLib.getInstance().getLogger().severe(
                    "Pattern cannot be used with GUI type: " + guiType + "! (Only chest/double-chest)");
            return;
        }

        if (pattern().size() > guiSize.height) {
            NotLib.getInstance().getLogger().severe(
                    "Pattern height is too big for this GUI: " + pattern().size() + " > " + guiSize.height);
            return;
        }

        int maxWidth = Collections.max(pattern().stream().map(String::length).toList());
        if (maxWidth > guiSize.width) {
            NotLib.getInstance().getLogger().severe(
                    "Pattern width is too big for this GUI: " + maxWidth + " > " + guiSize.width);
            return;
        }

        Map<Character, Integer> totals = new HashMap<>();
        for (String line : pattern) {
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                totals.merge(ch, 1, Integer::sum);
            }
        }

        Map<Character, Integer> counts = new HashMap<>();
        for (int y = 0; y < pattern.size(); y++) {
            String patternLine = pattern.get(y);
            for (int x = 0; x < patternLine.length(); x++) {
                char ch = patternLine.charAt(x);
                counts.merge(ch, 1, Integer::sum);

                if (patternEmptySlotChars.contains(ch)) continue;

                int slot = x + (y * guiSize.width);
                GUIPatternMatchInfo info = new GUIPatternMatchInfo(
                        this, ch, x, y, counts.get(ch), totals.get(ch), slot, totals);

                GUIItem matched = onPatternMatch.apply(info);
                if (matched != null) addItem(matched, slot);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Animation factories
    // -------------------------------------------------------------------------

    /**
     * Creates and registers a back-and-forth animation that oscillates between
     * 0 and 1 and back again over the given duration.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIBackAndForthAnimation}
     */
    public GUIAnimation aBackAndForth(long durationTicks, long frames) {
        return registerAnimation(new GUIBackAndForthAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers a bounce animation that uses a sine curve to
     * simulate a bouncing effect.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIBounceAnimation}
     */
    public GUIAnimation aBounce(long durationTicks, long frames) {
        return registerAnimation(new GUIBounceAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers an ease-in-out animation using a cubic Bézier curve
     * that starts and ends slowly.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIEaseInOutAnimation}
     */
    public GUIAnimation easeInOut(long durationTicks, long frames) {
        return registerAnimation(new GUIEaseInOutAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers an elastic animation that overshoots and snaps back,
     * simulating a spring or elastic band.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIElasticAnimation}
     */
    public GUIAnimation aElastic(long durationTicks, long frames) {
        return registerAnimation(new GUIElasticAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers an infinite looping animation that continuously
     * cycles from 0 to 1 and wraps around.
     *
     * @param durationTicks period length in server ticks
     * @param frames        number of discrete animation steps per period
     * @return registered {@link GUIInfiniteAnimation}
     */
    public GUIAnimation aInfinite(long durationTicks, long frames) {
        return registerAnimation(new GUIInfiniteAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers a linear progress animation that moves from 0 to 1
     * at a constant rate.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIProgressAnimation}
     */
    public GUIAnimation aProgress(long durationTicks, long frames) {
        return registerAnimation(new GUIProgressAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers a pulse animation that oscillates smoothly between
     * a minimum and maximum value using a sine wave.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIPulseAnimation}
     */
    public GUIAnimation aPulse(long durationTicks, long frames) {
        return registerAnimation(new GUIPulseAnimation(this, durationTicks, frames));
    }

    /**
     * Creates and registers a step animation that jumps between discrete values
     * in equal intervals rather than transitioning smoothly.
     *
     * @param durationTicks total duration in server ticks
     * @param frames        number of discrete animation steps
     * @return registered {@link GUIStepAnimation}
     */
    public GUIAnimation aStep(long durationTicks, long frames) {
        return registerAnimation(new GUIStepAnimation(this, durationTicks, frames));
    }

    // -------------------------------------------------------------------------
    // Static factories
    // -------------------------------------------------------------------------

    /**
     * Creates a new GUI with default settings.
     *
     * @return new GUI
     */
    public static GUI create() { return new GUI(); }

    /**
     * Creates a new GUI with the given string title.
     *
     * @param title inventory title (MiniMessage / legacy {@code &} codes supported)
     * @return new GUI
     */
    public static GUI create(String title) { return create().title(title); }

    /**
     * Creates a new GUI with the given {@link Text} title.
     *
     * @param title title builder
     * @return new GUI
     */
    public static GUI create(Text title) { return create().title(title); }

    /**
     * Creates a new GUI with the given Adventure {@link Component} title.
     *
     * @param title title component
     * @return new GUI
     */
    public static GUI create(Component title) { return create().title(title); }

    // =========================================================================
    // Inner classes
    // =========================================================================

    /**
     * Immutable snapshot of context information passed to the
     * {@link GUI#onPatternMatch} callback for a single pattern slot.
     *
     * <p>Use the fields to decide which item to place at the slot.
     * For example, {@link #count} and {@link #total} let you know which
     * occurrence of a repeated character is currently being processed,
     * which is useful for building paginated grids.</p>
     */
    public class GUIPatternMatchInfo {

        /** The GUI being built. */
        public final GUI gui;

        /** The pattern character at this slot. */
        public final char ch;

        /** Column index of this slot (0-based). */
        public final int x;

        /** Row index of this slot (0-based). */
        public final int y;

        /**
         * 1-based occurrence index of {@link #ch} in the pattern
         * (i.e. "this is the Nth time we have seen this character").
         */
        public final int count;

        /** Total number of times {@link #ch} appears anywhere in the pattern. */
        public final int total;

        /** Absolute inventory slot index ({@code y * guiWidth + x}). */
        public final int slot;

        /**
         * Total occurrence counts for every character in the pattern,
         * keyed by character. Useful when the callback needs to know how
         * many slots of another type exist (e.g. for pagination logic).
         */
        public final Map<Character, Integer> totals;

        /**
         * Creates a new match info record.
         *
         * @param gui    owner GUI
         * @param ch     pattern character
         * @param x      column index
         * @param y      row index
         * @param count  1-based occurrence index of {@code ch}
         * @param total  total occurrences of {@code ch}
         * @param slot   absolute inventory slot
         * @param totals occurrence counts for all pattern characters
         */
        public GUIPatternMatchInfo(
                GUI gui, char ch, int x, int y,
                int count, int total, int slot,
                Map<Character, Integer> totals) {
            this.gui    = gui;
            this.ch     = ch;
            this.x      = x;
            this.y      = y;
            this.count  = count;
            this.total  = total;
            this.slot   = slot;
            this.totals = totals;
        }

        /**
         * Returns the position of this slot as a {@link Vector2}.
         *
         * @return {@code Vector2(x, y)}
         */
        public Vector2 pos() { return Vector2.of(this.x, this.y); }
    }

    /**
     * Wraps a {@link ConfigurationSection} that describes a single item entry
     * inside a pattern configuration block.
     *
     * <p>Expected YAML structure:</p>
     * <pre>
     * type:   DIAMOND        # Material name (default: DIAMOND)
     * take:   false          # Whether the player may pick the item up (default: false)
     * amount: 1              # Stack size (default: 1)
     * name:   "Diamond"      # Display name (default: "Diamond")
     * lore:                  # Optional lore list
     *   - "First line"
     *   - "Second line"
     * </pre>
     */
    public class GUIPatternItemConfigurationSection {

        /** The underlying configuration section. */
        public final ConfigurationSection section;

        /** Key for the material type field. */
        public static final String N_PATTERN = "type";
        /** Default material name when the key is missing. */
        public static final String DEF_PATTERN = "DIAMOND";

        /** Key for the pick-up permission field. */
        public static final String N_TAKE = "take";
        /** Default pick-up value. */
        public static final boolean DEF_TAKE = false;

        /** Key for the stack amount field. */
        public static final String N_AMOUNT = "amount";
        /** Default stack amount. */
        public static final int DEF_AMOUNT = 1;

        /** Key for the display name field. */
        public static final String N_NAME = "name";
        /** Default display name. */
        public static final String DEF_NAME = "Diamond";

        /** Key for the lore list field. */
        public static final String N_LORE = "lore";

        /**
         * Creates a wrapper around the given configuration section.
         *
         * @param section YAML configuration section
         */
        public GUIPatternItemConfigurationSection(ConfigurationSection section) {
            this.section = section;
        }

        /**
         * Returns the raw material type string from the configuration.
         *
         * @return material type string, defaults to {@value #DEF_PATTERN}
         */
        public String getType() { return this.section.getString(N_PATTERN, DEF_PATTERN); }

        /**
         * Resolves and returns the {@link Material} from {@link #getType()}.
         * Falls back to {@link Material#DIAMOND} for unknown names.
         *
         * @return resolved material
         */
        public Material getMaterial() {
            Material material = Material.getMaterial(getType());
            return material != null ? material : Material.DIAMOND;
        }

        /**
         * Returns whether the player is allowed to pick this item up.
         *
         * @return {@code true} if pick-up is enabled, defaults to {@value #DEF_TAKE}
         */
        public boolean getTake() { return this.section.getBoolean(N_TAKE, DEF_TAKE); }

        /**
         * Returns the configured stack size.
         *
         * @return amount, defaults to {@value #DEF_AMOUNT}
         */
        public int getAmount() { return this.section.getInt(N_AMOUNT, DEF_AMOUNT); }

        /**
         * Returns the configured display name.
         *
         * @return name string, defaults to {@value #DEF_NAME}
         */
        public String getName() { return this.section.getString(N_NAME, DEF_NAME); }

        /**
         * Returns the configured lore lines.
         *
         * @return list of lore strings, empty if not configured
         */
        public List<String> getLore() { return this.section.getStringList(N_LORE); }
    }

    /**
     * Wraps a top-level {@link ConfigurationSection} that contains a pattern
     * definition and associated item rules for pattern-based GUI construction
     * driven entirely by YAML config.
     *
     * <p>Expected YAML structure:</p>
     * <pre>
     * pattern:
     *   - "#########"
     *   - "#AAAAAAA#"
     *   - "#########"
     * items:
     *   "#":
     *     type: GRAY_STAINED_GLASS_PANE
     *     name: " "
     *   "A":
     *     - type: DIAMOND
     *       name: "Buy Diamond"
     * </pre>
     */
    public class GUIPatternConfigurationSection {

        /** The underlying configuration section. */
        public final ConfigurationSection section;

        /** Key for the pattern rows list. */
        public static final String N_PATTERN = "pattern";
        /** Key for the items map. */
        public static final String N_ITEMS = "items";

        /**
         * Creates a wrapper around the given configuration section.
         *
         * @param section YAML configuration section
         */
        public GUIPatternConfigurationSection(ConfigurationSection section) {
            this.section = section;
        }

        /**
         * Returns the list of pattern row strings read from the YAML.
         *
         * @return pattern rows, empty if not defined
         */
        public List<String> getPattern() { return section.getStringList(N_PATTERN); }

        /**
         * Parses and returns the item rules map.
         * Each key is a one-character string matching a pattern character;
         * the value is a list of {@link GUIPatternItemConfigurationSection}
         * wrappers (one entry per item variant or rotation).
         *
         * @return map of pattern character string → item rule list
         */
        public Map<String, List<GUIPatternItemConfigurationSection>> getItemRules() {
            Map<String, List<GUIPatternItemConfigurationSection>> rules = new HashMap<>();

            ConfigurationSection itemsSection = section.getConfigurationSection(N_ITEMS);
            if (itemsSection == null) return rules;

            for (String key : itemsSection.getKeys(false)) {
                List<GUIPatternItemConfigurationSection> itemRules =
                        rules.computeIfAbsent(key, k -> new ArrayList<>());

                if (itemsSection.isList(key)) {
                    for (Object o : itemsSection.getList(key)) {
                        if (o instanceof ConfigurationSection cs) {
                            itemRules.add(new GUIPatternItemConfigurationSection(cs));
                        }
                    }
                } else if (itemsSection.isConfigurationSection(key)) {
                    ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                    itemRules.add(new GUIPatternItemConfigurationSection(itemSection));
                }
            }

            return rules;
        }
    }
}
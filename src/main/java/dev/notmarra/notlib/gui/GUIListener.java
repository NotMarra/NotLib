package dev.notmarra.notlib.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import dev.notmarra.notlib.extensions.NotListener;
import dev.notmarra.notlib.extensions.NotPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;

/**
 * Bukkit event listener that manages the lifecycle of open {@link GUI} instances
 * and routes inventory events to the correct GUI.
 *
 * <h2>How it works</h2>
 * <p>When a GUI is opened via {@link #openGUI(Player, GUI)}, the player's UUID
 * is mapped to their {@link GUI}. All subsequent inventory events for that player
 * are then forwarded to the matching GUI until it is closed.</p>
 *
 * <h2>Click routing</h2>
 * <p>On each {@link InventoryClickEvent} the listener:</p>
 * <ol>
 *   <li>Resolves the {@link GUI} from the player UUID.</li>
 *   <li>Reads the item UUID embedded in the clicked {@link org.bukkit.inventory.ItemStack}'s
 *       {@link org.bukkit.persistence.PersistentDataContainer}.</li>
 *   <li>Finds the matching {@link GUIItem} and invokes its click handler.</li>
 *   <li>Cancels the event unless the item is marked as pick-up-able via
 *       {@link GUIItem#canPickUp()}.</li>
 * </ol>
 *
 * <h2>Registration</h2>
 * <p>Register this listener once during plugin startup via the usual Bukkit
 * listener registration mechanism.  Only one instance should be active per
 * plugin to avoid double-handling events.</p>
 */
public class GUIListener extends NotListener {

    /**
     * Identifier used to look up this listener via the plugin's listener registry.
     */
    public static final String ID = "notguilistener";

    /**
     * Maps each player's UUID to the {@link GUI} they currently have open.
     * Entries are added in {@link #openGUI} and removed in
     * {@link #onInventoryClose}.
     */
    private final Map<UUID, GUI> openGUIs = new HashMap<>();

    /**
     * Creates a new listener bound to the given plugin.
     *
     * @param plugin owning plugin instance
     */
    public GUIListener(NotPlugin plugin) { super(plugin); }

    /**
     * {@inheritDoc}
     *
     * @return {@value #ID}
     */
    @Override
    public String getId() { return ID; }

    /**
     * Opens a {@link GUI} for the given player and registers the mapping so
     * subsequent inventory events are routed to that GUI.
     *
     * <p>If the player already has a different GUI open, the old entry is
     * silently overwritten. The player's current inventory is replaced by
     * the GUI inventory via {@link Player#openInventory(Inventory)}.</p>
     *
     * @param player player to open the GUI for
     * @param gui    GUI to display
     */
    public void openGUI(Player player, GUI gui) {
        openGUIs.put(player.getUniqueId(), gui);
        player.openInventory(gui.getInventory());
    }

    /**
     * Handles all inventory click events.
     *
     * <p>The event is ignored when:</p>
     * <ul>
     *   <li>The clicker is not a {@link Player}.</li>
     *   <li>The player has no registered open GUI.</li>
     *   <li>The clicked inventory is {@code null}.</li>
     *   <li>The clicked item has no embedded item UUID
     *       (the slot is empty or the item was not created by this library).</li>
     * </ul>
     *
     * <p>When a valid item UUID is found but the corresponding {@link GUIItem}
     * no longer exists in the GUI (e.g. it was removed after opening), the
     * event is still cancelled to prevent the player from taking the item.</p>
     *
     * @param event the inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        GUI gui = openGUIs.get(player.getUniqueId());
        if (gui == null) return;

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) return;

        UUID itemUUID = gui.getItemIdFromItemStack(event.getCurrentItem());
        if (itemUUID == null) {
            event.setCancelled(true);
            return;
        }

        GUIItem item = gui.getItem(itemUUID);
        if (item == null) return;

        gui.handleClick(event, item);
        event.setCancelled(!item.canPickUp());
    }

    /**
     * Handles inventory open events to fire the GUI's optional
     * {@link GUI#onOpen} callback.
     *
     * <p>The callback is only fired when the inventory that was opened
     * matches the player's currently registered GUI inventory, preventing
     * false positives when the player opens an unrelated inventory.</p>
     *
     * @param event the inventory open event
     */
    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory openInventory = event.getInventory();
        GUI gui = openGUIs.get(player.getUniqueId());

        if (gui != null && openInventory.equals(gui.getBuiltInventory())) {
            if (gui.onOpen != null) gui.onOpen.accept(event);
        }
    }

    /**
     * Handles inventory close events to fire the GUI's optional
     * {@link GUI#onClose} callback and clean up the open-GUI registry.
     *
     * <p>The callback is only fired and the registry entry only removed when
     * the inventory that was closed matches the player's registered GUI
     * inventory.  This prevents the mapping from being cleared prematurely
     * if the player closes an unrelated inventory.</p>
     *
     * @param event the inventory close event
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        Inventory closedInventory = event.getInventory();
        GUI gui = openGUIs.get(player.getUniqueId());

        if (gui != null && closedInventory.equals(gui.getBuiltInventory())) {
            if (gui.onClose != null) gui.onClose.accept(event);
            openGUIs.remove(player.getUniqueId());
        }
    }
}
package dev.notmarra.notlib.gui;

/**
 * Compile-time slot index constants for every vanilla Minecraft inventory type.
 *
 * <p>Each inner class groups the constants for one inventory type.
 * Use these instead of raw integers to make GUI code self-documenting and
 * resilient to future slot-layout changes.</p>
 *
 * <p>All indices are 0-based and match the slot numbering used by the Bukkit
 * inventory API (i.e. what {@link org.bukkit.event.inventory.InventoryClickEvent#getSlot()}
 * returns when that inventory type is open).</p>
 *
 * <p>This class is a pure constants container and cannot be instantiated.</p>
 *
 * <h2>Usage example</h2>
 * <pre>{@code
 * gui.type(InventoryType.ANVIL);
 * gui.addButton(Material.PAPER, "Input", GUISlotIDs.AnvilSlots.LEFT, handler);
 * }</pre>
 */
public class GUISlotIDs {

    private GUISlotIDs() {}

    // =========================================================================
    // Brewing Stand
    // =========================================================================

    /**
     * Slot indices for the Brewing Stand inventory.
     *
     * <pre>
     * Layout (top-down view):
     *
     *   [FUEL=4]       [INGREDIENT=3]
     *         [LEFT=0] [MIDDLE=1] [RIGHT=2]
     * </pre>
     */
    public class BrewingSlots {

        /** Blaze powder fuel slot (top-left). Index {@code 4}. */
        public static final int FUEL = 4;

        /** Ingredient slot at the top (what gets brewed into the bottles). Index {@code 3}. */
        public static final int INGREDIENT = 3;

        /** Left bottle slot. Index {@code 0}. */
        public static final int LEFT = 0;

        /** Middle bottle slot. Index {@code 1}. */
        public static final int MIDDLE = 1;

        /** Right bottle slot. Index {@code 2}. */
        public static final int RIGHT = 2;
    }

    // =========================================================================
    // Anvil
    // =========================================================================

    /**
     * Slot indices for the Anvil inventory.
     *
     * <pre>
     * Layout:
     *
     *   [LEFT=0] + [RIGHT=1]  →  [OUTPUT=2]
     * </pre>
     */
    public class AnvilSlots {

        /** First input slot (item to be renamed or repaired). Index {@code 0}. */
        public static final int LEFT = 0;

        /** Second input slot (sacrifice or rename material). Index {@code 1}. */
        public static final int RIGHT = 1;

        /** Output slot (result of the operation). Index {@code 2}. */
        public static final int OUTPUT = 2;
    }

    // =========================================================================
    // Beacon
    // =========================================================================

    /**
     * Slot indices for the Beacon inventory.
     */
    public class BeaconSlots {

        /** The single payment slot where the mineral item is placed. Index {@code 0}. */
        public static final int INPUT = 0;
    }

    // =========================================================================
    // Cartography Table
    // =========================================================================

    /**
     * Slot indices for the Cartography Table inventory.
     *
     * <pre>
     * Layout:
     *
     *   [UP=0]
     *    |+|    →  [OUTPUT=2]
     *   [DOWN=1]
     * </pre>
     */
    public class CartographyTableSlots {

        /** Map input slot. Index {@code 0}. */
        public static final int UP = 0;

        /** Paper / glass pane input slot. Index {@code 1}. */
        public static final int DOWN = 1;

        /** Output slot. Index {@code 2}. */
        public static final int OUTPUT = 2;
    }

    // =========================================================================
    // Blast Furnace
    // =========================================================================

    /**
     * Slot indices for the Blast Furnace inventory.
     *
     * <pre>
     * Layout:
     *
     *   [UP=0]
     *    |||    →  [OUTPUT=2]
     *   [DOWN=1]
     * </pre>
     */
    public class BlastFurnaceSlots {

        /** Item-to-smelt slot. Index {@code 0}. */
        public static final int UP = 0;

        /** Fuel slot. Index {@code 1}. */
        public static final int DOWN = 1;

        /** Output slot. Index {@code 2}. */
        public static final int OUTPUT = 2;
    }

    // =========================================================================
    // Enchanting Table
    // =========================================================================

    /**
     * Slot indices for the Enchanting Table inventory.
     *
     * <pre>
     * Layout:
     *
     *   [LEFT=0] [RIGHT=1]
     * </pre>
     */
    public class EnchantingTableSlots {

        /** Item-to-enchant slot. Index {@code 0}. */
        public static final int LEFT = 0;

        /** Lapis lazuli slot. Index {@code 1}. */
        public static final int RIGHT = 1;
    }

    // =========================================================================
    // Furnace
    // =========================================================================

    /**
     * Slot indices for the regular Furnace inventory.
     *
     * <pre>
     * Layout:
     *
     *   [UP=0]
     *    |||    →  [OUTPUT=2]
     *   [DOWN=1]
     * </pre>
     */
    public class FurnaceSlots {

        /** Item-to-smelt slot. Index {@code 0}. */
        public static final int UP = 0;

        /** Fuel slot. Index {@code 1}. */
        public static final int DOWN = 1;

        /** Output slot. Index {@code 2}. */
        public static final int OUTPUT = 2;
    }

    // =========================================================================
    // Grindstone
    // =========================================================================

    /**
     * Slot indices for the Grindstone inventory.
     *
     * <pre>
     * Layout:
     *
     *   [UP=0]
     *    ---    →  [OUTPUT=2]
     *   [DOWN=1]
     * </pre>
     */
    public class GrindstoneSlots {

        /** First item input slot. Index {@code 0}. */
        public static final int UP = 0;

        /** Second item input slot. Index {@code 1}. */
        public static final int DOWN = 1;

        /** Output slot. Index {@code 2}. */
        public static final int OUTPUT = 2;
    }

    // =========================================================================
    // Hopper
    // =========================================================================

    /**
     * Slot indices for the Hopper inventory (5 slots in a single row).
     *
     * <pre>
     * Layout:
     *
     *   [FIRST=0] [SECOND=1] [THIRD=2] [FOURTH=3] [FIFTH=4]
     * </pre>
     */
    public class HopperSlots {

        /** Total number of slots. */
        public static final int SIZE = 5;

        /** First (leftmost) slot. Index {@code 0}. */
        public static final int FIRST = 0;

        /** Second slot. Index {@code 1}. */
        public static final int SECOND = 1;

        /** Third (center) slot. Index {@code 2}. */
        public static final int THIRD = 2;

        /** Fourth slot. Index {@code 3}. */
        public static final int FOURTH = 3;

        /** Fifth (rightmost / last) slot. Index {@code 4}. */
        public static final int FIFTH = 4;

        /** Alias for {@link #FIFTH}. Index {@code 4}. */
        public static final int LAST = 4;
    }

    // =========================================================================
    // Loom
    // =========================================================================

    /**
     * Slot indices for the Loom inventory.
     *
     * <pre>
     * Layout:
     *
     *   [LEFT=0]  [RIGHT=1]
     *   [MIDDLE=2]            →  [OUTPUT=4]
     * </pre>
     */
    public class LoomSlots {

        /** Banner input slot. Index {@code 0}. */
        public static final int LEFT = 0;

        /** Dye input slot. Index {@code 1}. */
        public static final int RIGHT = 1;

        /** Banner pattern item slot. Index {@code 2}. */
        public static final int MIDDLE = 2;

        /** Output slot. Index {@code 4}. */
        public static final int OUTPUT = 4;
    }

    // =========================================================================
    // Smithing Table
    // =========================================================================

    /**
     * Slot indices for the Smithing Table inventory.
     *
     * <pre>
     * Layout:
     *
     *   [LEFT=0] [MIDDLE=1] [RIGHT=2]  →  [OUTPUT=3]
     * </pre>
     */
    public class SmithingTableSlots {

        /** Template slot (netherite upgrade template, etc.). Index {@code 0}. */
        public static final int LEFT = 0;

        /** Base item slot. Index {@code 1}. */
        public static final int MIDDLE = 1;

        /** Additional material slot. Index {@code 2}. */
        public static final int RIGHT = 2;

        /** Output slot. Index {@code 3}. */
        public static final int OUTPUT = 3;
    }

    // =========================================================================
    // Smoker
    // =========================================================================

    /**
     * Slot indices for the Smoker inventory.
     *
     * <pre>
     * Layout:
     *
     *   [UP=0]
     *    |||    →  [OUTPUT=2]
     *   [DOWN=1]
     * </pre>
     */
    public class SmokerSlots {

        /** Food item slot. Index {@code 0}. */
        public static final int UP = 0;

        /** Fuel slot. Index {@code 1}. */
        public static final int DOWN = 1;

        /** Output slot. Index {@code 2}. */
        public static final int OUTPUT = 2;
    }

    // =========================================================================
    // Stonecutter
    // =========================================================================

    /**
     * Slot indices for the Stonecutter inventory.
     *
     * <pre>
     * Layout:
     *
     *   [INPUT=0]  →  [OUTPUT=1]
     * </pre>
     */
    public class StonecutterSlots {

        /** Stone material input slot. Index {@code 0}. */
        public static final int INPUT = 0;

        /** Output slot (click a recipe button first). Index {@code 1}. */
        public static final int OUTPUT = 1;
    }

    // =========================================================================
    // Player Inventory
    // =========================================================================

    /**
     * Slot indices for the Player inventory as seen when a chest or similar
     * container is open (i.e. the player's own inventory is visible below).
     *
     * <p>These indices match the values returned by
     * {@link org.bukkit.event.inventory.InventoryClickEvent#getSlot()} when
     * the player's own inventory portion is clicked.</p>
     */
    public class PlayerSlots {

        /** Number of slots in the hotbar. */
        public static final int HOTBAR_SIZE = 9;

        /** Number of slots in the main inventory grid (excluding hotbar). */
        public static final int INVENTORY_SIZE = 27;

        /** Helmet armour slot. Index {@code 39}. */
        public static final int HELMET = 39;

        /** Chestplate armour slot. Index {@code 38}. */
        public static final int CHESTPLATE = 38;

        /** Leggings armour slot. Index {@code 37}. */
        public static final int LEGGINGS = 37;

        /** Boots armour slot. Index {@code 36}. */
        public static final int BOOTS = 36;

        /** Off-hand slot. Index {@code 40}. */
        public static final int OFFHAND = 40;

        /** First (leftmost) hotbar slot. Index {@code 0}. */
        public static final int HOTBAR_FIRST = 0;

        /** Last (rightmost) hotbar slot. Index {@code 8}. */
        public static final int HOTBAR_LAST = 8;

        /** Top-left slot of the main inventory grid. Index {@code 9}. */
        public static final int INV_TOP_LEFT = 9;

        /** Top-right slot of the main inventory grid. Index {@code 17}. */
        public static final int INV_TOP_RIGHT = 17;

        /** Middle-row left slot of the main inventory grid. Index {@code 18}. */
        public static final int INV_MIDDLE_LEFT = 18;

        /** Middle-row right slot of the main inventory grid. Index {@code 26}. */
        public static final int INV_MIDDLE_RIGHT = 26;

        /** Bottom-row left slot of the main inventory grid. Index {@code 27}. */
        public static final int INV_BOTTOM_LEFT = 27;

        /** Bottom-row right slot of the main inventory grid. Index {@code 35}. */
        public static final int INV_BOTTOM_RIGHT = 35;
    }

    // =========================================================================
    // Small Chest (3 rows)
    // =========================================================================

    /**
     * Corner / edge slot indices for a small Chest inventory (3 rows × 9 columns).
     */
    public class ChestSmallSlots {

        /** Total slot count. */
        public static final int SIZE = 27;

        /** Top-left corner. Index {@code 0}. */
        public static final int TOP_LEFT = 0;

        /** Top-right corner. Index {@code 8}. */
        public static final int TOP_RIGHT = 8;

        /** Middle-row left edge. Index {@code 9}. */
        public static final int MIDDLE_LEFT = 9;

        /** Middle-row right edge. Index {@code 17}. */
        public static final int MIDDLE_RIGHT = 17;

        /** Bottom-left corner. Index {@code 18}. */
        public static final int BOTTOM_LEFT = 18;

        /** Bottom-right corner. Index {@code 26}. */
        public static final int BOTTOM_RIGHT = 26;
    }

    // =========================================================================
    // Shulker Box
    // =========================================================================

    /**
     * Corner slot indices for a Shulker Box inventory (3 rows × 9 columns,
     * same layout as a small chest).
     */
    public class ShulkerBoxSlots {

        /** Total slot count. */
        public static final int SIZE = 27;

        /** Top-left corner. Index {@code 0}. */
        public static final int TOP_LEFT = 0;

        /** Top-right corner. Index {@code 8}. */
        public static final int TOP_RIGHT = 8;

        /** Bottom-left corner. Index {@code 18}. */
        public static final int BOTTOM_LEFT = 18;

        /** Bottom-right corner. Index {@code 26}. */
        public static final int BOTTOM_RIGHT = 26;
    }

    // =========================================================================
    // Large Chest (6 rows / double-chest)
    // =========================================================================

    /**
     * Corner slot indices for a large Chest (double-chest) inventory
     * (6 rows × 9 columns).
     */
    public class ChestLargeSlots {

        /** Total slot count. */
        public static final int SIZE = 54;

        /** Top-left corner. Index {@code 0}. */
        public static final int TOP_LEFT = 0;

        /** Top-right corner. Index {@code 8}. */
        public static final int TOP_RIGHT = 8;

        /** Bottom-left corner. Index {@code 45}. */
        public static final int BOTTOM_LEFT = 45;

        /** Bottom-right corner. Index {@code 53}. */
        public static final int BOTTOM_RIGHT = 53;
    }

    // =========================================================================
    // Dispenser
    // =========================================================================

    /**
     * Slot indices for the Dispenser inventory (3 rows × 3 columns).
     *
     * <pre>
     * Layout:
     *
     *   [TOP_LEFT=0]    [TOP_MIDDLE=1]    [TOP_RIGHT=2]
     *   [MIDDLE_LEFT=3] [MIDDLE_MIDDLE=4] [MIDDLE_RIGHT=5]
     *   [BOTTOM_LEFT=6] [BOTTOM_MIDDLE=7] [BOTTOM_RIGHT=8]
     * </pre>
     */
    public class DispenserSlots {

        /** Total slot count. */
        public static final int SIZE = 9;

        /** Top-left slot. Index {@code 0}. */
        public static final int TOP_LEFT = 0;

        /** Top-center slot. Index {@code 1}. */
        public static final int TOP_MIDDLE = 1;

        /** Top-right slot. Index {@code 2}. */
        public static final int TOP_RIGHT = 2;

        /** Middle-left slot. Index {@code 3}. */
        public static final int MIDDLE_LEFT = 3;

        /** Center slot. Index {@code 4}. */
        public static final int MIDDLE_MIDDLE = 4;

        /** Middle-right slot. Index {@code 5}. */
        public static final int MIDDLE_RIGHT = 5;

        /** Bottom-left slot. Index {@code 6}. */
        public static final int BOTTOM_LEFT = 6;

        /** Bottom-center slot. Index {@code 7}. */
        public static final int BOTTOM_MIDDLE = 7;

        /** Bottom-right slot. Index {@code 8}. */
        public static final int BOTTOM_RIGHT = 8;
    }

    // =========================================================================
    // Dropper
    // =========================================================================

    /**
     * Slot indices for the Dropper inventory (3 rows × 3 columns).
     * Identical layout to {@link DispenserSlots}.
     *
     * <pre>
     * Layout:
     *
     *   [TOP_LEFT=0]    [TOP_MIDDLE=1]    [TOP_RIGHT=2]
     *   [MIDDLE_LEFT=3] [MIDDLE_MIDDLE=4] [MIDDLE_RIGHT=5]
     *   [BOTTOM_LEFT=6] [BOTTOM_MIDDLE=7] [BOTTOM_RIGHT=8]
     * </pre>
     */
    public class DropperSlots {

        /** Total slot count. */
        public static final int SIZE = 9;

        /** Top-left slot. Index {@code 0}. */
        public static final int TOP_LEFT = 0;

        /** Top-center slot. Index {@code 1}. */
        public static final int TOP_MIDDLE = 1;

        /** Top-right slot. Index {@code 2}. */
        public static final int TOP_RIGHT = 2;

        /** Middle-left slot. Index {@code 3}. */
        public static final int MIDDLE_LEFT = 3;

        /** Center slot. Index {@code 4}. */
        public static final int MIDDLE_MIDDLE = 4;

        /** Middle-right slot. Index {@code 5}. */
        public static final int MIDDLE_RIGHT = 5;

        /** Bottom-left slot. Index {@code 6}. */
        public static final int BOTTOM_LEFT = 6;

        /** Bottom-center slot. Index {@code 7}. */
        public static final int BOTTOM_MIDDLE = 7;

        /** Bottom-right slot. Index {@code 8}. */
        public static final int BOTTOM_RIGHT = 8;
    }

    // =========================================================================
    // Crafting Table
    // =========================================================================

    /**
     * Slot indices for the Crafting Table (Workbench) inventory.
     *
     * <p>Note: slot {@code 0} is the output slot; the 3×3 crafting grid
     * occupies slots 1–9.</p>
     *
     * <pre>
     * Layout:
     *
     *   [OUTPUT=0]
     *   [TOP_LEFT=1]    [TOP_MIDDLE=2]    [TOP_RIGHT=3]
     *   [MIDDLE_LEFT=4] [MIDDLE_MIDDLE=5] [MIDDLE_RIGHT=6]
     *   [BOTTOM_LEFT=7] [BOTTOM_MIDDLE=8] [BOTTOM_RIGHT=9]
     * </pre>
     */
    public class CraftingTableSlots {

        /** Total slot count (output + 3×3 grid). */
        public static final int SIZE = 9;

        /** Top-left crafting grid slot. Index {@code 1}. */
        public static final int TOP_LEFT = 1;

        /** Top-center crafting grid slot. Index {@code 2}. */
        public static final int TOP_MIDDLE = 2;

        /** Top-right crafting grid slot. Index {@code 3}. */
        public static final int TOP_RIGHT = 3;

        /** Middle-left crafting grid slot. Index {@code 4}. */
        public static final int MIDDLE_LEFT = 4;

        /** Center crafting grid slot. Index {@code 5}. */
        public static final int MIDDLE_MIDDLE = 5;

        /** Middle-right crafting grid slot. Index {@code 6}. */
        public static final int MIDDLE_RIGHT = 6;

        /** Bottom-left crafting grid slot. Index {@code 7}. */
        public static final int BOTTOM_LEFT = 7;

        /** Bottom-center crafting grid slot. Index {@code 8}. */
        public static final int BOTTOM_MIDDLE = 8;

        /** Bottom-right crafting grid slot. Index {@code 9}. */
        public static final int BOTTOM_RIGHT = 9;

        /** Output slot. Index {@code 0}. */
        public static final int OUTPUT = 0;
    }
}
package com.notmarra.notlib.utils.gui;

public class NotGUISlotIDs {
    /*
    [4]     [3]
        [0] [1] [2]
    */
    public class BrewingSlots {
        public static final int FUEL = 4;
        public static final int INGREDIENT = 3;
        public static final int LEFT = 0;
        public static final int MIDDLE = 1;
        public static final int RIGHT = 2;
    }

    /*
    [0] + [1] -> [2]
    */
    public class AnvilSlots {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int OUTPUT = 2;
    }

    public class BeaconSlots {
        public static final int INPUT = 0;
    }

    /*
    [0]
    |+| -> [2]
    [1]
    */
    public class CartographyTableSlots {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int OUTPUT = 2;
    }

    /*
    [0]
    ||| -> [2]
    [1]
    */
    public class BlastFurnaceSlots {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int OUTPUT = 2;
    }

    /*
    [0][1]
    */
    public class EnchantingTableSlots {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
    }

    /*
    [0]
    ||| -> [2]
    [1]
    */
    public class FurnaceSlots {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int OUTPUT = 2;
    }

    /*
    [0]
    --- -> [2]
    [1]
    */
    public class GrindstoneSlots {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int OUTPUT = 2;
    }

    /*
    [0][1][2][3][4]
    */
    public class HopperSlots {
        public static final int SIZE = 5;
        public static final int FIRST = 0;
        public static final int SECOND = 1;
        public static final int THIRD = 2;
        public static final int FOURTH = 3;
        public static final int FIFTH = 4;
        public static final int LAST = 4;
    }

    /*
    [0][1]
    [ 2  ] -> [3]
    */
    public class LoomSlots {
        public static final int LEFT = 0;
        public static final int RIGHT = 1;
        public static final int MIDDLE = 2;
        public static final int OUTPUT = 4;
    }

    /*
    [0][1][2] -> [3]
    */
    public class SmithingTableSlots {
        public static final int LEFT = 0;
        public static final int MIDDLE = 1;
        public static final int RIGHT = 2;
        public static final int OUTPUT = 3;
    }

    /*
    [0]
    ||| -> [2]
    [1]
    */
    public class SmokerSlots {
        public static final int UP = 0;
        public static final int DOWN = 1;
        public static final int OUTPUT = 2;
    }

    /*
    [0] -> [1]
    */
    public class StonecutterSlots {
        public static final int INPUT = 0;
        public static final int OUTPUT = 1;
    }

    public class PlayerSlots {
        public static final int HOTBAR_SIZE = 9;
        public static final int INVENTORY_SIZE = 27;

        public static final int HELMET = 39;
        public static final int CHESTPLATE = 38;
        public static final int LEGGINGS = 37;
        public static final int BOOTS = 36;
        public static final int OFFHAND = 40;
        public static final int HOTBAR_FIRST = 0;
        public static final int HOTBAR_LAST = 8;
        public static final int INV_TOP_LEFT = 9;
        public static final int INV_TOP_RIGHT = 17;
        public static final int INV_MIDDLE_LEFT = 18;
        public static final int INV_MIDDLE_RIGHT = 26;
        public static final int INV_BOTTOM_LEFT = 27;
        public static final int INV_BOTTOM_RIGHT = 35;
    }

    public class ChestSmallSlots {
        public static final int SIZE = 27;
        public static final int TOP_LEFT = 0;
        public static final int TOP_RIGHT = 8;
        public static final int MIDDLE_LEFT = 9;
        public static final int MIDDLE_RIGHT = 17;
        public static final int BOTTOM_LEFT = 18;
        public static final int BOTTOM_RIGHT = 26;
    }

    public class ShulkerBoxSlots {
        public static final int SIZE = 27;
        public static final int TOP_LEFT = 0;
        public static final int TOP_RIGHT = 8;
        public static final int BOTTOM_LEFT = 18;
        public static final int BOTTOM_RIGHT = 26;
    }

    public class ChestLargeSlots {
        public static final int SIZE = 54;
        public static final int TOP_LEFT = 0;
        public static final int TOP_RIGHT = 8;
        public static final int BOTTOM_LEFT = 45;
        public static final int BOTTOM_RIGHT = 53;
    }

    public class DispenserSlots {
        public static final int SIZE = 9;
        public static final int TOP_LEFT = 0;
        public static final int TOP_MIDDLE = 1;
        public static final int TOP_RIGHT = 2;
        public static final int MIDDLE_LEFT = 3;
        public static final int MIDDLE_MIDDLE = 4;
        public static final int MIDDLE_RIGHT = 5;
        public static final int BOTTOM_LEFT = 6;
        public static final int BOTTOM_MIDDLE = 7;
        public static final int BOTTOM_RIGHT = 8;
    }

    public class DropperSlots {
        public static final int SIZE = 9;
        public static final int TOP_LEFT = 0;
        public static final int TOP_MIDDLE = 1;
        public static final int TOP_RIGHT = 2;
        public static final int MIDDLE_LEFT = 3;
        public static final int MIDDLE_MIDDLE = 4;
        public static final int MIDDLE_RIGHT = 5;
        public static final int BOTTOM_LEFT = 6;
        public static final int BOTTOM_MIDDLE = 7;
        public static final int BOTTOM_RIGHT = 8;
    }

    public class CraftingTableSlots {
        public static final int SIZE = 9;
        public static final int TOP_LEFT = 1;
        public static final int TOP_MIDDLE = 2;
        public static final int TOP_RIGHT = 3;
        public static final int MIDDLE_LEFT = 4;
        public static final int MIDDLE_MIDDLE = 5;
        public static final int MIDDLE_RIGHT = 6;
        public static final int BOTTOM_LEFT = 7;
        public static final int BOTTOM_MIDDLE = 8;
        public static final int BOTTOM_RIGHT = 9;
        public static final int OUTPUT = 0;
    }
}
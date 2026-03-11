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

public class GUI implements InventoryHolder {
    public static final String ITEM_UUID_KEY = "notlib-gui-item-uuid";

    public NotLib plugin;
    @Nullable private InventoryType guiType; // null is double-chest
    private Component guiTitle;
    private final GUIContainer rootContainer;
    private Inventory builtInventory;
    private List<String> pattern = new ArrayList<>();
    // private 
    private List<Character> patternEmptySlotChars = new ArrayList<>();
    private Function<GUIPatternMatchInfo, GUIItem> onPatternMatch;
    public Consumer<InventoryOpenEvent> onOpen;
    public Consumer<InventoryCloseEvent> onClose;

    public final List<GUIAnimation> animations = new ArrayList<>();
    public final Map<InventoryType, Size> inventorySizes = new HashMap<>();

    public GUI() {
        plugin = NotLib.getInstance();
        guiTitle = Text.of("GUI").build();
        rootContainer = new GUIContainer(this);
        rows(6);
        inventorySizes.put(InventoryType.CHEST, Size.of(9, 3));
        inventorySizes.put(InventoryType.DISPENSER, Size.of(3, 3));
        inventorySizes.put(InventoryType.DROPPER, Size.of(3, 3));
        inventorySizes.put(InventoryType.HOPPER, Size.of(5, 1));
        inventorySizes.put(InventoryType.BARREL, Size.of(9, 3));
        inventorySizes.put(InventoryType.SHULKER_BOX, Size.of(9, 3));
        inventorySizes.put(InventoryType.SMOKER, Size.of(3, 1));
        inventorySizes.put(InventoryType.BLAST_FURNACE, Size.of(3, 1));
        inventorySizes.put(InventoryType.BREWING, Size.of(5, 1));
        inventorySizes.put(InventoryType.ENCHANTING, Size.of(2, 1));
        inventorySizes.put(InventoryType.ANVIL, Size.of(3, 1));
        inventorySizes.put(InventoryType.GRINDSTONE, Size.of(3, 1));
        inventorySizes.put(InventoryType.CARTOGRAPHY, Size.of(3, 1));
        inventorySizes.put(InventoryType.STONECUTTER, Size.of(2, 1));
        inventorySizes.put(InventoryType.LOOM, Size.of(4, 1));
        inventorySizes.put(InventoryType.CRAFTING, Size.of(2, 2));
        inventorySizes.put(InventoryType.FURNACE, Size.of(3, 1));
        inventorySizes.put(InventoryType.WORKBENCH, Size.of(3, 3));
        inventorySizes.put(InventoryType.SMITHING, Size.of(4, 1));
    }

    public boolean isChest() { return guiType == InventoryType.CHEST || guiType == null; }
    public GUI type(InventoryType type) {
        if (!inventorySizes.containsKey(type)) return this;
        guiType = type;
        return size(inventorySizes.get(type));
    }
    public int rowSize() {
        if (guiType == null) return 9;
        return inventorySizes.get(guiType).width;
    }
    public boolean isPatternMatched() { return !this.pattern.isEmpty() && this.onPatternMatch != null; }
    public GUI title(String title) { return title(Text.of(title)); }
    public GUI title(Text title) { return title(title.build()); }
    public GUI title(Component title) { this.guiTitle = title; return this; }
    public GUI position(int x) { rootContainer.position(x); return this; }
    public GUI position(int x, int y) { rootContainer.position(x, y); return this; }
    public GUI position(Vector2 position) { rootContainer.position(position); return this; }
    public GUI size(int width) { return size(Size.of(width)); }
    public GUI size(int width, int height) { return size(Size.of(width, height)); }
    public GUI size(Size size) { rootContainer.size(size); return this; }
    public GUI rows(int rows) {
        if (!isChest()) return this;
        if (guiType == InventoryType.CHEST) {
            if (rows < 1 || rows > 3) return this;
        } else { // double-chest
            if (rows < 1 || rows > 6) return this;
        }
        return size(9, rows);
    }
    public GUI pattern(String pattern) { return pattern(Arrays.asList(pattern.split("\n"))); }
    public GUI pattern(List<String> pattern) {
        this.pattern = pattern;
        return this;
    }
    public GUI emptySlotChars(List<Character> characters) {
        this.patternEmptySlotChars = characters;
        return this;
    }

    public GUIContainer createContainer() {
        GUIContainer container = new GUIContainer(this, rootContainer);
        rootContainer.addChild(container);
        return container;
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

    public GUI addItem(GUIItem item, int slot) {
        rootContainer.addItem(item, slot);
        return this;
    }

    public GUI addItem(GUIItem item, int x, int y) {
        rootContainer.addItem(item, x, y);
        return this;
    }

    public GUI addButton(GUIItem item, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(item, slot, action);
        return this;
    }
    
    public GUI addButton(Material material, Text name, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
        return this;
    }

    public GUI addButton(Material material, String name, int slot, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
        return this;
    }
    
    public GUI addButton(Material material, String name, int x, int y, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }

    public GUI addButton(Material material, Text name, int x, int y, BiConsumer<InventoryClickEvent, GUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }

    public GUIItem createItem(Material material) {
        return new GUIItem(this, rootContainer, material);
    }

    public GUIAnimation registerAnimation(GUIAnimation animation) {
        animations.add(animation);
        return animation;
    }

    public void cancelAllAnimations() {
        for (GUIAnimation animation : animations) animation.cancel();
        animations.clear();
    }

    public void removeAnimation(GUIAnimation animation) {
        animations.remove(animation);
    }

    public List<GUIAnimation> getAnimations() {
        return animations;
    }

    public Inventory getBuiltInventory() {
        if (builtInventory == null) {
            builtInventory = build();
            patternMatchGui();
        }
        return builtInventory;
    }

    @Override
    public Inventory getInventory() {
        return getBuiltInventory();
    }

    public List<String> pattern() { return pattern; }
    public Function<GUIPatternMatchInfo, GUIItem> onPatternMatch() { return onPatternMatch; }
    public List<Character> emptySlotCharacters() { return this.patternEmptySlotChars; }
    public Size size() { return rootContainer.size(); }
    public int totalSize() { return rootContainer.totalSize(); }

    public NotLib getPlugin() {
        return plugin;
    }

    public void refresh() {
        builtInventory.clear();
        rootContainer.refresh();
    }

    public boolean handleClick(InventoryClickEvent event) {
        if (event.getClickedInventory().getHolder() != this) return false;
        return rootContainer.handleClick(event);
    }

    public boolean handleClick(InventoryClickEvent event, GUIItem item) {
        if (event.getClickedInventory().getHolder() != this) return false;
        return rootContainer.handleClick(event, item);
    }

    private void patternMatchGui() {
        if (!isPatternMatched()) return;
        Size guiSize = size();

        if (!isChest()) {
            NotLib.getInstance().getLogger().severe("Pattern cannot be used with GUI type: " + guiType + "! (Only chest/double-chest)");
            return;
        }

        if (pattern().size() > guiSize.height) {
            NotLib.getInstance().getLogger().severe("Pattern height is too big for this GUI: " + (pattern().size()) + " > " + guiSize.height);
            return;
        }

        int maxWidth = Collections.max(pattern().stream().map(c -> c.length()).toList());
        if (maxWidth > guiSize.width) {
            NotLib.getInstance().getLogger().severe("Pattern width is too big for this GUI: " + (maxWidth) + " > " + guiSize.width);
            return;   
        }

        Map<Character, Integer> totals = new HashMap<>();
        for (String line : pattern) {
            for (int i = 0; i < line.length(); i++) {
                char ch = line.charAt(i);
                if (totals.containsKey(ch)) {
                    totals.put(ch, totals.get(ch) + 1);
                } else {
                    totals.put(ch, 1);
                }
            }
        }

        Map<Character, Integer> counts = new HashMap<>();
        for (int y = 0; y < pattern.size(); y++) {
            String patternLine = pattern.get(y);
            for (int x = 0; x < patternLine.length(); x++) {
                char ch = patternLine.charAt(x);

                if (counts.containsKey(ch)) {
                    counts.put(ch, counts.get(ch) + 1);
                } else {
                    counts.put(ch, 1);
                }

                if (patternEmptySlotChars.contains(ch)) {
                    continue;
                }

                int slot = x + (y * guiSize.width);

                GUIPatternMatchInfo info = new GUIPatternMatchInfo(
                    this,
                    ch, x, y,
                    counts.get(ch), totals.get(ch), slot, totals 
                );

                GUIItem matched = onPatternMatch.apply(info);
                if (matched != null) addItem(matched, slot);
            }
        }
    }

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

    public void open(Player player) {
        GUIListener listener = plugin.getGUIListener();
        if (listener == null) {
            getPlugin().getComponentLogger().warn(Text.of("GUI listener not registered", Colors.ORANGE.get()).build());
            return;
        }
        listener.openGUI(player, this);
    }

    public void close(Player player) {
        Inventory inventory = player.getOpenInventory().getTopInventory();
        if (inventory != null && inventory.getHolder() == this) {
            player.closeInventory();
        }
    }

    public GUI onOpen(Consumer<InventoryOpenEvent> onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    public GUI onClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
        return this;
    }

    public GUI onPatternMatch(Function<GUIPatternMatchInfo, GUIItem> onPatternMatch) {
        this.onPatternMatch = onPatternMatch;
        return this;
    }

    public UUID getItemIdFromItemStack(ItemStack item) { return rootContainer.getItemIdFromItemStack(item); }
    public GUIItem getItem(UUID itemId) { return rootContainer.getItem(itemId); }
    public ItemStack getItem(int slot) { return rootContainer.getItem(slot); }

    // Animations

    public GUIAnimation aBackAndForth(long durationTicks, long frames) {
        return registerAnimation(new GUIBackAndForthAnimation(this, durationTicks, frames));
    }
    public GUIAnimation aBounce(long durationTicks, long frames) {
        return registerAnimation(new GUIBounceAnimation(this, durationTicks, frames));
    }
    public GUIAnimation easeInOut(long durationTicks, long frames) {
        return registerAnimation(new GUIEaseInOutAnimation(this, durationTicks, frames));
    }
    public GUIAnimation aElastic(long durationTicks, long frames) {
        return registerAnimation(new GUIElasticAnimation(this, durationTicks, frames));
    }
    public GUIAnimation aInfinite(long durationTicks, long frames) {
        return registerAnimation(new GUIInfiniteAnimation(this, durationTicks, frames));
    }
    public GUIAnimation aProgress(long durationTicks, long frames) {
        return registerAnimation(new GUIProgressAnimation(this, durationTicks, frames));
    }
    public GUIAnimation aPulse(long durationTicks, long frames) {
        return registerAnimation(new GUIPulseAnimation(this, durationTicks, frames));
    }
    public GUIAnimation aStep(long durationTicks, long frames) {
        return registerAnimation(new GUIStepAnimation(this, durationTicks, frames));
    }

    public static GUI create() { return new GUI(); }
    public static GUI create(String title) { return create().title(title); }
    public static GUI create(Text title) { return create().title(title); }
    public static GUI create(Component title) { return create().title(title); }

    public class GUIPatternMatchInfo {
        public final GUI gui;
        public final char ch;
        public final int x;
        public final int y;
        public final int count; // kolikátý
        public final int total; // celkem stejných
        public final int slot;
        public final Map<Character, Integer> totals;

        public GUIPatternMatchInfo(
            GUI gui,
            char ch, int x, int y,
            int count, int total, int slot,
            Map<Character, Integer> totals
        ) {
            this.gui = gui;
            this.ch = ch;
            this.x = x;
            this.y = y;
            this.count = count;
            this.total = total;
            this.slot = slot;
            this.totals = totals;
        }

        public Vector2 pos() {
            return Vector2.of(this.x, this.y);
        }
    }

    public class GUIPatternItemConfigurationSection {
        public final ConfigurationSection section;

        public static final String N_PATTERN = "type";
        public static final String DEF_PATTERN = "DIAMOND";
        public static final String N_TAKE = "take";
        public static final boolean DEF_TAKE = false;
        public static final String N_AMOUNT = "amount";
        public static final int DEF_AMOUNT = 1;
        public static final String N_NAME = "name";
        public static final String DEF_NAME = "Diamond";
        public static final String N_LORE = "lore";

        public GUIPatternItemConfigurationSection(ConfigurationSection section) {
            this.section = section;
        }

        public String getType() { return this.section.getString(N_PATTERN, DEF_PATTERN); }
        public Material getMaterial() {
            Material material = Material.getMaterial(getType());
            return material != null ? material : Material.DIAMOND;
        }

        public boolean getTake() { return this.section.getBoolean(N_TAKE, DEF_TAKE); }
        public int getAmount() { return this.section.getInt(N_AMOUNT, DEF_AMOUNT); }
        public String getName() { return this.section.getString(N_NAME, DEF_NAME); }
        public List<String> getLore() { return this.section.getStringList(N_LORE); }
    }

    public class GUIPatternSection {
        Map<String, List<GUIPatternItemConfigurationSection>> rules = new HashMap<>();
        
    }

    public class GUIPatternConfigurationSection {
        public final ConfigurationSection section;

        public static final String N_PATTERN = "pattern";
        public static final String N_ITEMS = "items";

        public GUIPatternConfigurationSection(ConfigurationSection section) {
            this.section = section;
        }

        public List<String> getPattern() { return section.getStringList(N_PATTERN); }

        public Map<String, List<GUIPatternItemConfigurationSection>> getItemRules() {
            Map<String, List<GUIPatternItemConfigurationSection>> rules = new HashMap<>();

            ConfigurationSection itemsSection = section.getConfigurationSection(N_ITEMS);

            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    List<GUIPatternItemConfigurationSection> itemRules = new ArrayList<>();
                    if (rules.containsKey(key)) itemRules = rules.get(key);

                    if (itemsSection.isList(key)) {
                        for (Object o : itemsSection.getList(key)) {
                            if (o instanceof ConfigurationSection) {
                                itemRules.add(new GUIPatternItemConfigurationSection((ConfigurationSection)o));
                            }
                        }
                    } else if (itemsSection.isConfigurationSection(key)) {
                        ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                        itemRules.add(new GUIPatternItemConfigurationSection(itemSection));
                    }
                }
            }

            return rules;
        }
    }
}

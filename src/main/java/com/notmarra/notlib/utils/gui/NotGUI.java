package com.notmarra.notlib.utils.gui;

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

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotDebugger;
import com.notmarra.notlib.utils.NotSize;
import com.notmarra.notlib.utils.NotVector2;
import com.notmarra.notlib.utils.gui.animations.NotGUIAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIBackAndForthAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIBounceAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIEaseInOutAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIElasticAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIInfiniteAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIProgressAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIPulseAnimation;
import com.notmarra.notlib.utils.gui.animations.NotGUIStepAnimation;

import net.kyori.adventure.text.Component;

public class NotGUI implements InventoryHolder {
    public static final String ITEM_UUID_KEY = "notlib-gui-item-uuid";

    public NotLib plugin;
    @Nullable private InventoryType guiType; // null is double-chest
    private Component guiTitle;
    private NotGUIContainer rootContainer;
    private Inventory builtInventory;
    private List<String> pattern = new ArrayList<>();
    // private 
    private List<Character> patternEmptySlotChars = new ArrayList<>();
    private Function<NotGUIPatternMatchInfo, NotGUIItem> onPatternMatch;
    public Consumer<InventoryOpenEvent> onOpen;
    public Consumer<InventoryCloseEvent> onClose;

    public final List<NotGUIAnimation> animations = new ArrayList<>();
    public final Map<InventoryType, NotSize> inventorySizes = new HashMap<>();

    public NotGUI() {
        plugin = NotLib.getInstance();
        guiTitle = ChatF.of("NotGUI").build();
        rootContainer = new NotGUIContainer(this);
        rows(6);
        inventorySizes.put(InventoryType.CHEST, NotSize.of(9, 3));
        inventorySizes.put(InventoryType.DISPENSER, NotSize.of(3, 3));
        inventorySizes.put(InventoryType.DROPPER, NotSize.of(3, 3));
        inventorySizes.put(InventoryType.HOPPER, NotSize.of(5, 1));
        inventorySizes.put(InventoryType.BARREL, NotSize.of(9, 3));
        inventorySizes.put(InventoryType.SHULKER_BOX, NotSize.of(9, 3));
        inventorySizes.put(InventoryType.SMOKER, NotSize.of(3, 1));
        inventorySizes.put(InventoryType.BLAST_FURNACE, NotSize.of(3, 1));
        inventorySizes.put(InventoryType.BREWING, NotSize.of(5, 1));
        inventorySizes.put(InventoryType.ENCHANTING, NotSize.of(2, 1));
        inventorySizes.put(InventoryType.ANVIL, NotSize.of(3, 1));
        inventorySizes.put(InventoryType.GRINDSTONE, NotSize.of(3, 1));
        inventorySizes.put(InventoryType.CARTOGRAPHY, NotSize.of(3, 1));
        inventorySizes.put(InventoryType.STONECUTTER, NotSize.of(2, 1));
        inventorySizes.put(InventoryType.LOOM, NotSize.of(4, 1));
        inventorySizes.put(InventoryType.CRAFTING, NotSize.of(2, 2));
        inventorySizes.put(InventoryType.FURNACE, NotSize.of(3, 1));
        inventorySizes.put(InventoryType.WORKBENCH, NotSize.of(3, 3));
        inventorySizes.put(InventoryType.SMITHING, NotSize.of(4, 1));
    }

    public boolean isChest() { return guiType == InventoryType.CHEST || guiType == null; }
    public NotGUI type(InventoryType type) {
        if (!inventorySizes.containsKey(type)) return this;
        guiType = type;
        return size(inventorySizes.get(type));
    }
    public int rowSize() {
        if (guiType == null) return 9;
        return inventorySizes.get(guiType).width;
    }
    public boolean isPatternMatched() { return !this.pattern.isEmpty() && this.onPatternMatch != null; }
    public NotGUI title(String title) { return title(ChatF.of(title)); }
    public NotGUI title(ChatF title) { return title(title.build()); }
    public NotGUI title(Component title) { this.guiTitle = title; return this; }
    public NotGUI position(int x) { rootContainer.position(x); return this; }
    public NotGUI position(int x, int y) { rootContainer.position(x, y); return this; }
    public NotGUI position(NotVector2 position) { rootContainer.position(position); return this; }
    public NotGUI size(int width) { return size(NotSize.of(width)); }
    public NotGUI size(int width, int height) { return size(NotSize.of(width, height)); }
    public NotGUI size(NotSize size) { rootContainer.size(size); return this; }
    public NotGUI rows(int rows) {
        if (!isChest()) return this;
        if (guiType == InventoryType.CHEST) {
            if (rows < 1 || rows > 3) return this;
        } else { // double-chest
            if (rows < 1 || rows > 6) return this;
        }
        return size(9, rows);
    }
    public NotGUI pattern(String pattern) { return pattern(Arrays.asList(pattern.split("\n"))); }
    public NotGUI pattern(List<String> pattern) {
        this.pattern = pattern;
        return this;
    }
    public NotGUI emptySlotChars(List<Character> characters) {
        this.patternEmptySlotChars = characters;
        return this;
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

    public NotGUI addButton(NotGUIItem item, int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(item, slot, action);
        return this;
    }
    
    public NotGUI addButton(Material material, ChatF name, int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
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

    public NotGUI addButton(Material material, ChatF name, int x, int y, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }

    public NotGUIItem createItem(Material material) {
        return new NotGUIItem(this, rootContainer, material);
    }

    public NotGUIAnimation registerAnimation(NotGUIAnimation animation) {
        animations.add(animation);
        return animation;
    }

    public void cancelAllAnimations() {
        for (NotGUIAnimation animation : animations) animation.cancel();
        animations.clear();
    }

    public void removeAnimation(NotGUIAnimation animation) {
        animations.remove(animation);
    }

    public List<NotGUIAnimation> getAnimations() {
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
    public Function<NotGUIPatternMatchInfo, NotGUIItem> onPatternMatch() { return onPatternMatch; }
    public List<Character> emptySlotCharacters() { return this.patternEmptySlotChars; }
    public NotSize size() { return rootContainer.size(); }
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

    public boolean handleClick(InventoryClickEvent event, NotGUIItem item) {
        if (event.getClickedInventory().getHolder() != this) return false;
        return rootContainer.handleClick(event, item);
    }

    private void patternMatchGui() {
        if (!isPatternMatched()) return;
        NotSize guiSize = size();

        if (!isChest()) {
            NotLib.dbg().log(NotDebugger.C_ERROR, "Pattern cannot be used with GUI type: " + guiType + "! (Only chest/double-chest)");
            return;
        }

        if (pattern().size() > guiSize.height) {
            NotLib.dbg().log(NotDebugger.C_ERROR, "Pattern height is too big for this GUI: " + (pattern().size()) + " > " + guiSize.height);
            return;
        }

        int maxWidth = Collections.max(pattern().stream().map(c -> c.length()).toList());
        if (maxWidth > guiSize.width) {
            NotLib.dbg().log(NotDebugger.C_ERROR, "Pattern width is too big for this GUI: " + (maxWidth) + " > " + guiSize.width);
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

                NotGUIPatternMatchInfo info = new NotGUIPatternMatchInfo(
                    this,
                    ch, x, y,
                    counts.get(ch), totals.get(ch), slot, totals 
                );

                NotGUIItem matched = onPatternMatch.apply(info);
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

        rootContainer.render(inventory, NotVector2.zero());

        return inventory;
    }

    public void open(Player player) {
        NotGUIListener listener = plugin.getNotGUIListener();
        if (listener == null) {
            getPlugin().getComponentLogger().warn(ChatF.of("GUI listener not registered", ChatF.C_ORANGE).build());
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

    public NotGUI onOpen(Consumer<InventoryOpenEvent> onOpen) {
        this.onOpen = onOpen;
        return this;
    }

    public NotGUI onClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
        return this;
    }

    public NotGUI onPatternMatch(Function<NotGUIPatternMatchInfo, NotGUIItem> onPatternMatch) {
        this.onPatternMatch = onPatternMatch;
        return this;
    }

    public UUID getItemIdFromItemStack(ItemStack item) { return rootContainer.getItemIdFromItemStack(item); }
    public NotGUIItem getNotItem(UUID itemId) { return rootContainer.getNotItem(itemId); }
    public ItemStack getItem(int slot) { return rootContainer.getItem(slot); }

    // Animations

    public NotGUIAnimation aBackAndForth(long durationTicks, long frames) {
        return registerAnimation(new NotGUIBackAndForthAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation aBounce(long durationTicks, long frames) {
        return registerAnimation(new NotGUIBounceAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation easeInOut(long durationTicks, long frames) {
        return registerAnimation(new NotGUIEaseInOutAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation aElastic(long durationTicks, long frames) {
        return registerAnimation(new NotGUIElasticAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation aInfinite(long durationTicks, long frames) {
        return registerAnimation(new NotGUIInfiniteAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation aProgress(long durationTicks, long frames) {
        return registerAnimation(new NotGUIProgressAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation aPulse(long durationTicks, long frames) {
        return registerAnimation(new NotGUIPulseAnimation(this, durationTicks, frames));
    }
    public NotGUIAnimation aStep(long durationTicks, long frames) {
        return registerAnimation(new NotGUIStepAnimation(this, durationTicks, frames));
    }

    public static NotGUI create() { return new NotGUI(); }
    public static NotGUI create(String title) { return create().title(title); }
    public static NotGUI create(ChatF title) { return create().title(title); }
    public static NotGUI create(Component title) { return create().title(title); }

    public class NotGUIPatternMatchInfo {
        public final NotGUI gui;
        public final char ch;
        public final int x;
        public final int y;
        public final int count; // kolikátý
        public final int total; // celkem stejných
        public final int slot;
        public final Map<Character, Integer> totals;

        public NotGUIPatternMatchInfo(
            NotGUI gui,
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

        public NotVector2 pos() {
            return NotVector2.of(this.x, this.y);
        }
    }

    public class NotGUIPatternItemConfigurationSection {
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

        public NotGUIPatternItemConfigurationSection(ConfigurationSection section) {
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

    public class NotGUIPatternSection {
        Map<String, List<NotGUIPatternItemConfigurationSection>> rules = new HashMap<>();
        
    }

    public class NotGUIPatternConfigurationSection {
        public final ConfigurationSection section;

        public static final String N_PATTERN = "pattern";
        public static final String N_ITEMS = "items";

        public NotGUIPatternConfigurationSection(ConfigurationSection section) {
            this.section = section;
        }

        public List<String> getPattern() { return section.getStringList(N_PATTERN); }

        public Map<String, List<NotGUIPatternItemConfigurationSection>> getItemRules() {
            Map<String, List<NotGUIPatternItemConfigurationSection>> rules = new HashMap<>();

            ConfigurationSection itemsSection = section.getConfigurationSection(N_ITEMS);

            if (itemsSection != null) {
                for (String key : itemsSection.getKeys(false)) {
                    List<NotGUIPatternItemConfigurationSection> itemRules = new ArrayList<>();
                    if (rules.containsKey(key)) itemRules = rules.get(key);

                    if (itemsSection.isList(key)) {
                        for (Object o : itemsSection.getList(key)) {
                            if (o instanceof ConfigurationSection) {
                                itemRules.add(new NotGUIPatternItemConfigurationSection((ConfigurationSection)o));
                            }
                        }
                    } else if (itemsSection.isConfigurationSection(key)) {
                        ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                        itemRules.add(new NotGUIPatternItemConfigurationSection(itemSection));
                    }
                }
            }

            return rules;
        }
    }
}

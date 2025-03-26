package com.notmarra.notlib.utils.gui;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.utils.ChatF;
import com.notmarra.notlib.utils.NotSize;
import com.notmarra.notlib.utils.NotVector2;

import net.kyori.adventure.text.Component;

public class NotGUI implements InventoryHolder {
    public static final String ITEM_UUID_KEY = "notlib-gui-item-uuid";

    private NotLib plugin;
    @Nullable private InventoryType guiType; // null is double-chest
    private Component guiTitle;
    private NotGUIContainer rootContainer;
    private Inventory builtInventory;

    public final Map<InventoryType, NotSize> inventorySizes = new HashMap<>();

    public NotGUI() {
        plugin = NotLib.getInstance();
        guiTitle = ChatF.of("NotGUI").build();
        rootContainer = new NotGUIContainer(this);
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
    
    public NotGUI addButton(Material material, String name, int slot, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(material, name, slot, action);
        return this;
    }
    
    public NotGUI addButton(Material material, String name, int x, int y, BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        rootContainer.addButton(material, name, x, y, action);
        return this;
    }
    
    public NotGUIItem createItem(Material material) {
        return new NotGUIItem(this, rootContainer, material);
    }

    public NotGUIAnimation createAnimation(long durationTicks, long frames) {
        return new NotGUIAnimation(this, durationTicks, frames);
    }

    public Inventory getBuiltInventory() {
        if (builtInventory == null) {
            builtInventory = build();
        }
        return builtInventory;
    }

    @Override
    public Inventory getInventory() {
        return getBuiltInventory();
    }

    public NotLib getPlugin() {
        return plugin;
    }

    public void refresh() {
        builtInventory.clear();
        rootContainer.refresh();
    }

    public void animate(long durationTicks, int frames, Consumer<Float> updateFunction) {
        createAnimation(durationTicks, frames).start(updateFunction);
    }

    public boolean handleClick(InventoryClickEvent event) {
        return rootContainer.handleClick(event);
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
        NotGUIListener listener = plugin.getGUIListener();
        if (listener == null) {
            getPlugin().getLogger().warning("GUI listener not registered");
            return;
        }
        listener.openGUI(player, this);
    }

    public static NotGUI create() { return new NotGUI(); }
    public static NotGUI create(String title) { return create().title(title); }
    public static NotGUI create(ChatF title) { return create().title(title); }
    public static NotGUI create(Component title) { return create().title(title); }
}

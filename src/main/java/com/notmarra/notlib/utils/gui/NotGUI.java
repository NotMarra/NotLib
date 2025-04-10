package com.notmarra.notlib.utils.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import com.notmarra.notlib.NotLib;
import com.notmarra.notlib.utils.ChatF;
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

    private NotLib plugin;
    @Nullable private InventoryType guiType; // null is double-chest
    private Component guiTitle;
    private NotGUIContainer rootContainer;
    private Inventory builtInventory;
    public Consumer<InventoryCloseEvent> onClose;

    public final List<NotGUIAnimation> animations = new ArrayList<>();
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
        }
        return builtInventory;
    }

    @Override
    public Inventory getInventory() {
        return getBuiltInventory();
    }

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

    public NotGUI onClose(Consumer<InventoryCloseEvent> onClose) {
        this.onClose = onClose;
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
}

package com.notmarra.notlib.utils.gui;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Item;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.notmarra.notlib.utils.ChatF;

import net.kyori.adventure.text.Component;

public class NotGUIItem { 
    private UUID uid;
    private NotGUI parentGUI;
    private NotGUIContainer parentContainer;

    private int itemAmount = 1;
    private Material itemType;
    private Component itemName;
    private List<Component> itemLore = new ArrayList<>();
    private String skullTexture;
    private BiConsumer<InventoryClickEvent, NotGUIContainer> action = null;
    private BiFunction<NotGUIItem, SkullMeta, SkullMeta> onSkullMeta = null;
    private boolean canPickUp = false;

    public NotGUIItem(NotGUI gui, Material itemType) {
        this(gui, null, itemType);
    }

    public NotGUIItem(NotGUI gui, NotGUIContainer parentContainer, Material itemType) {
        this.uid = UUID.randomUUID();
        this.parentGUI = gui;
        this.parentContainer = parentContainer;
        this.itemType = itemType;
    }

    public NotGUIItem withSkullTexture(String textureValue) {
        this.skullTexture = textureValue;
        return this;
    }

    public UUID id() { return uid; }
    public int amount() { return itemAmount; }
    public Material type() { return itemType; }
    public Component name() { return itemName; }
    public List<Component> lore() { return itemLore; }
    public boolean canPickUp() { return canPickUp; }
    public BiConsumer<InventoryClickEvent, NotGUIContainer> action() { return action; }

    public NotGUIItem amount(int itemAmount) {
        this.itemAmount = itemAmount;
        return this;
    }

    public NotGUIItem type(Material itemType) {
        this.itemType = itemType;
        return this;
    }

    public NotGUIItem name(String itemName) {
        return name(ChatF.of(itemName));
    }

    public NotGUIItem name(ChatF itemName) {
        return name(itemName.build());
    }

    public NotGUIItem name(Component itemName) {
        this.itemName = itemName;
        return this;
    }

    public NotGUIItem lore(String itemLore) { return lore(List.of(itemLore)); }
    public NotGUIItem lore(Component itemLore) { return lore(List.of(itemLore)); }
    public NotGUIItem lore(ChatF itemLore) { return lore(List.of(itemLore)); }

    public NotGUIItem lore(List<Object> itemLore) {
        for (Object line : itemLore) {
            if (line instanceof String) {
                this.itemLore.add(ChatF.of((String) line).build());
            } else if (line instanceof ChatF) {
                this.itemLore.add(((ChatF) line).build());
            } else if (line instanceof Component) {
                this.itemLore.add((Component) line);
            }
        }
        return this;
    }

    public NotGUIItem canPickUp(boolean canPickUp) {
        this.canPickUp = canPickUp;
        return this;
    }

    public NotGUIItem action(BiConsumer<InventoryClickEvent, NotGUIContainer> action) {
        this.canPickUp = false; // buttons are not pickable
        this.action = action;
        return this;
    }
    public NotGUIItem onClick(BiConsumer<InventoryClickEvent, NotGUIContainer> action) { return action(action); }

    public NotGUIItem onSkullMeta(BiFunction<NotGUIItem, SkullMeta, SkullMeta> onSkullMeta) { this.onSkullMeta = onSkullMeta; return this; }

    public ItemStack build() {
        ItemStack stack = new ItemStack(itemType);
        stack.setAmount(itemAmount);

        ItemMeta meta = stack.hasItemMeta() ? stack.getItemMeta() : Bukkit.getItemFactory().getItemMeta(itemType);
        // NOTE: Material.AIR has not ItemMeta no matter what!
        if (itemType != Material.AIR && meta != null) {
            if (itemName != null) meta.displayName(itemName);
            if (itemLore != null) meta.lore(itemLore);

            if (onSkullMeta != null) {
                meta = onSkullMeta.apply(this, (SkullMeta) meta);
            } else {
                if (itemType == Material.PLAYER_HEAD && skullTexture != null) {
                    try {
                        SkullMeta skullMeta = (SkullMeta) meta;
                        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID());
                        
                        PlayerTextures textures = profile.getTextures();
                        URL url = new URI("http://textures.minecraft.net/texture/" + skullTexture).toURL();
                        textures.setSkin(url);
                        profile.setTextures(textures);
                        
                        skullMeta.setPlayerProfile(profile);
                        meta = skullMeta;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            if (gui() != null) {
                NamespacedKey key = new NamespacedKey(gui().getPlugin(), NotGUI.ITEM_UUID_KEY);
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(key, PersistentDataType.STRING, uid.toString());
            }

            stack.setItemMeta(meta);
        }

        return stack;
    }

    public NotGUI gui() { return parentGUI; }
    public NotGUIContainer parent() { return parentContainer; }

    public NotGUIItem addToGUI(int slot) { gui().addItem(this, slot); return this; }
    public NotGUIItem addToGUI(int x, int y) { gui().addItem(this, x, y); return this; }

    public static NotGUIItem create(NotGUI gui, Material itemType) { return new NotGUIItem(gui, itemType); }
    public static NotGUIItem create(Material itemType) { return new NotGUIItem(null, itemType); }

    public static NotGUIItem fromItemStack(ItemStack item) {
        NotGUIItem newItem = create(item.getType())
            .name(item.effectiveName());
        
        List<Component> lore = item.lore();
        if (lore != null) newItem.lore(lore.stream().map(c -> (Object)c).toList());

        return newItem;
    }

    public static NotGUIItem fromItem(Item item) {
        return fromItemStack(item.getItemStack());
    }
}

package com.notmarra.notlib.utils.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.notmarra.notlib.utils.ChatF;

import net.kyori.adventure.text.Component;

public class NotGUIItem { 
    private UUID uid;
    private NotGUI parentGUI;
    private NotGUIContainer parentContainer;

    private int itemAmount = 1;
    private Material itemType;
    private Component itemName;
    private List<Component> itemLore;

    public NotGUIItem(NotGUI gui, Material itemType) {
        this(gui, null, itemType);
    }

    public NotGUIItem(NotGUI gui, NotGUIContainer parentContainer, Material itemType) {
        this.uid = UUID.randomUUID();
        this.parentGUI = gui;
        this.parentContainer = parentContainer;
        this.itemType = itemType;
    }

    public UUID id() { return uid; }
    public int amount() { return itemAmount; }
    public Material type() { return itemType; }
    public Component name() { return itemName; }
    public List<Component> lore() { return itemLore; }

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

    public NotGUIItem lore(List<Object> itemLore) {
        List<Component> lore = new ArrayList<>();

        for (Object line : itemLore) {
            if (line instanceof String) {
                lore.add(ChatF.of((String) line).build());
            } else if (line instanceof ChatF) {
                lore.add(((ChatF) line).build());
            } else if (line instanceof Component) {
                lore.add((Component) line);
            }
        }

        this.itemLore = lore;
        return this;
    }

    public ItemStack build() {
        ItemStack stack = new ItemStack(itemType);
        stack.setAmount(itemAmount);

        ItemMeta meta = stack.getItemMeta();
        meta.displayName(itemName);
        meta.lore(itemLore);

        NamespacedKey key = new NamespacedKey(gui().getPlugin(), NotGUI.ITEM_UUID_KEY);
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, uid.toString());

        stack.setItemMeta(meta);

        return stack;
    }

    public NotGUI gui() {
        return parentGUI;
    }

    public NotGUIContainer parent() {
        return parentContainer;
    }
}

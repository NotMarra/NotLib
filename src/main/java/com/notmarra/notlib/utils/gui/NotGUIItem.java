package com.notmarra.notlib.utils.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.notmarra.notlib.utils.ChatF;

import net.kyori.adventure.text.Component;

public class NotGUIItem {
    public NotGUI parentGUI;
    public NotGUIContainer parentContainer;

    private int itemAmount = 1;
    private Material itemType;
    private Component itemName;
    private List<Component> itemLore;

    public NotGUIItem(NotGUI gui, Material itemType) {
        this(gui, null, itemType);
    }

    public NotGUIItem(NotGUI gui, NotGUIContainer parentContainer, Material itemType) {
        this.parentGUI = gui;
        this.parentContainer = parentContainer;
        this.itemType = itemType;
    }

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

        stack.setItemMeta(meta);

        return stack;
    }

    public NotGUI gui() {
        return parentGUI;
    }

    public NotGUIContainer container() {
        return parentContainer;
    }
}

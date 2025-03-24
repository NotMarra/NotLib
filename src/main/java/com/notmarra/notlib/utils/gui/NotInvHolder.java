package com.notmarra.notlib.utils.gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import com.notmarra.notlib.utils.ChatF;

public class NotInvHolder implements InventoryHolder {
    private final JavaPlugin plugin;
    private final Inventory inventory;

    public NotInvHolder(JavaPlugin plugin) {
        this.plugin = plugin;
        this.inventory = prepareInventory();
    }

    private Inventory prepareInventory() {
        // SLOT IDS: https://mcutils.com/inventory-slots 

    //     int rows = (int) Math.ceil(colorMaterials.size() / 9.0);
    //     ChatF title = ChatF.of("Select Chat Color", ChatF.C_BLACK);
    //     Inventory inventory = plugin.getServer().createInventory(this, rows * 9, title.build());
        
    //     ItemStack clearItem = new ItemStack(Material.BARRIER);
    //     ItemMeta clearMeta = clearItem.getItemMeta();
    //     clearMeta.displayName(ChatF.of("Clear Color", ChatF.C_RED).build());
        
    //     List<Component> clearLore = new ArrayList<>();
    //     clearLore.add(ChatF.of("Click to remove your chat color", ChatF.C_GRAY).build());
    //     clearMeta.lore(clearLore);
        
    //     clearItem.setItemMeta(clearMeta);
    //     inventory.setItem(rows * 9 - 1, clearItem);
        
    //     int i = 0;
    //     for (Map.Entry<ChatColor, Material> entry : colorMaterials.entrySet()) {
    //         ChatColor chatColor = entry.getKey();
    //         Material material = entry.getValue();
            
    //         ItemStack item = new ItemStack(material);
    //         ItemMeta meta = item.getItemMeta();
            
    //         meta.displayName(ChatF.of(chatColor.name().replace('_', ' '), chatColor.getColor()).build());
            
    //         List<Component> lore = new ArrayList<>();
    //         lore.add(ChatF.of("Click to select this color", ChatF.C_GRAY).build());
    //         meta.lore(lore);
            
    //         item.setItemMeta(meta);
    //         inventory.setItem(i, item);
    //         i++;
    //     }

    //     return inventory;
        Inventory inventory = plugin.getServer().createInventory(this, InventoryType.BREWING, ChatF.of("NotGUI").build());
        inventory.setItem(0, new ItemStack(Material.ACACIA_BOAT));
        inventory.setItem(1, new ItemStack(Material.ACACIA_FENCE));
        inventory.setItem(2, new ItemStack(Material.ACACIA_BUTTON));
        inventory.setItem(3, new ItemStack(Material.ACACIA_DOOR));
        inventory.setItem(4, new ItemStack(Material.ACACIA_LEAVES));
        return inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}

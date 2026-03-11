package dev.notmarra.notlib.gui;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import dev.notmarra.notlib.chat.Text;
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

import net.kyori.adventure.text.Component;

public class GUIItem {
    private UUID uid;
    private GUI parentGUI;
    private GUIContainer parentContainer;

    private int itemAmount = 1;
    private Material itemType;
    private Component itemName;
    private List<Component> itemLore = new ArrayList<>();
    private String skullTexture;
    private BiConsumer<InventoryClickEvent, GUIContainer> action = null;
    private BiFunction<GUIItem, SkullMeta, SkullMeta> onSkullMeta = null;
    private boolean canPickUp = false;

    public GUIItem(GUI gui, Material itemType) {
        this(gui, null, itemType);
    }

    public GUIItem(GUI gui, GUIContainer parentContainer, Material itemType) {
        this.uid = UUID.randomUUID();
        this.parentGUI = gui;
        this.parentContainer = parentContainer;
        this.itemType = itemType;
    }

    public GUIItem withSkullTexture(String textureValue) {
        this.skullTexture = textureValue;
        return this;
    }

    public UUID id() { return uid; }
    public int amount() { return itemAmount; }
    public Material type() { return itemType; }
    public Component name() { return itemName; }
    public List<Component> lore() { return itemLore; }
    public boolean canPickUp() { return canPickUp; }
    public BiConsumer<InventoryClickEvent, GUIContainer> action() { return action; }

    public GUIItem amount(int itemAmount) {
        this.itemAmount = itemAmount;
        return this;
    }

    public GUIItem type(Material itemType) {
        this.itemType = itemType;
        return this;
    }

    public GUIItem name(String itemName) {
        return name(Text.of(itemName));
    }

    public GUIItem name(Text itemName) {
        return name(itemName.build());
    }

    public GUIItem name(Component itemName) {
        this.itemName = itemName;
        return this;
    }

    public GUIItem lore(String itemLore) { return lore(List.of(itemLore)); }
    public GUIItem lore(Component itemLore) { return lore(List.of(itemLore)); }
    public GUIItem lore(Text itemLore) { return lore(List.of(itemLore)); }

    public GUIItem lore(List<Object> itemLore) {
        for (Object line : itemLore) {
            if (line instanceof String) {
                this.itemLore.add(Text.of((String) line).build());
            } else if (line instanceof Text) {
                this.itemLore.add(((Text) line).build());
            } else if (line instanceof Component) {
                this.itemLore.add((Component) line);
            }
        }
        return this;
    }

    public GUIItem canPickUp(boolean canPickUp) {
        this.canPickUp = canPickUp;
        return this;
    }

    public GUIItem action(BiConsumer<InventoryClickEvent, GUIContainer> action) {
        this.canPickUp = false; // buttons are not pickable
        this.action = action;
        return this;
    }
    public GUIItem onClick(BiConsumer<InventoryClickEvent, GUIContainer> action) { return action(action); }

    public GUIItem onSkullMeta(BiFunction<GUIItem, SkullMeta, SkullMeta> onSkullMeta) { this.onSkullMeta = onSkullMeta; return this; }

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
                NamespacedKey key = new NamespacedKey(gui().getPlugin(), GUI.ITEM_UUID_KEY);
                PersistentDataContainer container = meta.getPersistentDataContainer();
                container.set(key, PersistentDataType.STRING, uid.toString());
            }

            stack.setItemMeta(meta);
        }

        return stack;
    }

    public GUI gui() { return parentGUI; }
    public GUIContainer parent() { return parentContainer; }

    public GUIItem addToGUI(int slot) { gui().addItem(this, slot); return this; }
    public GUIItem addToGUI(int x, int y) { gui().addItem(this, x, y); return this; }

    public static GUIItem create(GUI gui, Material itemType) { return new GUIItem(gui, itemType); }
    public static GUIItem create(Material itemType) { return new GUIItem(null, itemType); }

    public static GUIItem fromItemStack(ItemStack item) {
        GUIItem newItem = create(item.getType())
            .name(item.effectiveName());
        
        List<Component> lore = item.lore();
        if (lore != null) newItem.lore(lore.stream().map(c -> (Object)c).toList());

        return newItem;
    }

    public static GUIItem fromItem(Item item) {
        return fromItemStack(item.getItemStack());
    }
}

package dev.notmarra.notlib.gui;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import dev.notmarra.notlib.chat.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
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
import net.kyori.adventure.text.format.TextDecoration;

/**
 * Fluent builder for a single inventory item inside a {@link GUI}.
 *
 * <h2>Basic usage</h2>
 * <pre>{@code
 * GUIItem item = new GUIItem(gui, Material.DIAMOND)
 *     .name("&bShiny Diamond")
 *     .lore("A very valuable gem")
 *     .amount(3)
 *     .onClick((event, container) -> player.sendMessage("clicked!"));
 *
 * container.addItem(item, 4);
 * }</pre>
 *
 * <h2>Italic</h2>
 * <p>Minecraft applies italic to every custom item name and lore line by default.
 * This builder strips italic automatically on {@link #build()}.
 * Call {@link #keepItalic()} before building if you want the vanilla italic back.</p>
 *
 * <h2>Player heads and custom textures</h2>
 * <p>Five approaches are supported, from simplest to most flexible:</p>
 * <ol>
 *   <li>{@link #skullOwner(OfflinePlayer)} – player head via an {@link OfflinePlayer} reference.</li>
 *   <li>{@link #skullOwner(String)} – player head by username (may block the thread – avoid on main).</li>
 *   <li>{@link #skullTexture(String)} – custom texture via the hash part of a
 *       {@code textures.minecraft.net} URL.</li>
 *   <li>{@link #skullTextureBase64(String)} – custom texture via the raw base64 Value string
 *       (the "Value" field on sites such as minecraft-heads.com).</li>
 *   <li>{@link #onSkullMeta(BiFunction)} – full control over {@link SkullMeta};
 *       all other skull helpers are ignored when this is set.</li>
 * </ol>
 * <p>All skull helpers automatically switch the material to {@link Material#PLAYER_HEAD}.</p>
 */
public class GUIItem {

    private UUID uid;
    private GUI parentGUI;
    private GUIContainer parentContainer;

    private int itemAmount = 1;
    private Material itemType;
    private Component itemName;
    private List<Component> itemLore = new ArrayList<>();

    /** textures.minecraft.net hash (without the base URL). */
    private String skullTextureHash;
    /** Raw base64 Value string as returned by minecraft-heads.com or similar. */
    private String skullTextureBase64;
    /** Owner of a player-skull item. */
    private OfflinePlayer skullOwner;

    private BiConsumer<InventoryClickEvent, GUIContainer> action = null;
    private BiFunction<GUIItem, SkullMeta, SkullMeta> onSkullMeta = null;
    private boolean canPickUp = false;
    private boolean keepItalic = false;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Creates a new item builder associated with a GUI but no specific container.
     *
     * @param gui      the parent GUI; used for persistent-data key namespacing
     * @param itemType the Bukkit material
     */
    public GUIItem(GUI gui, Material itemType) {
        this(gui, null, itemType);
    }

    /**
     * Creates a new item builder associated with both a GUI and a specific container.
     *
     * @param gui             the parent GUI
     * @param parentContainer the container this item belongs to, or {@code null}
     * @param itemType        the Bukkit material
     */
    public GUIItem(GUI gui, GUIContainer parentContainer, Material itemType) {
        this.uid = UUID.randomUUID();
        this.parentGUI = gui;
        this.parentContainer = parentContainer;
        this.itemType = itemType;
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    /**
     * Returns the unique identifier of this item, used for click-event routing.
     *
     * @return item UUID
     */
    public UUID id() { return uid; }

    /**
     * Returns the current stack size.
     *
     * @return amount (1 by default)
     */
    public int amount() { return itemAmount; }

    /**
     * Returns the current material type.
     *
     * @return material
     */
    public Material type() { return itemType; }

    /**
     * Returns the display name component, or {@code null} if none was set.
     *
     * @return display name, may be {@code null}
     */
    public Component name() { return itemName; }

    /**
     * Returns the current lore lines.
     *
     * @return mutable lore component list
     */
    public List<Component> lore() { return itemLore; }

    /**
     * Returns whether the player is allowed to pick this item up from the GUI.
     *
     * @return {@code true} if pick-up is enabled
     */
    public boolean canPickUp() { return canPickUp; }

    /**
     * Returns the registered click handler, or {@code null} if none was set.
     *
     * @return click action, may be {@code null}
     */
    public BiConsumer<InventoryClickEvent, GUIContainer> action() { return action; }

    /**
     * Returns the parent GUI.
     *
     * @return parent GUI
     */
    public GUI gui() { return parentGUI; }

    /**
     * Returns the parent container, or {@code null} if this item was created without one.
     *
     * @return parent container, may be {@code null}
     */
    public GUIContainer parent() { return parentContainer; }

    // -------------------------------------------------------------------------
    // Basic item properties
    // -------------------------------------------------------------------------

    /**
     * Sets the stack size.
     *
     * @param itemAmount number of items (1–64)
     * @return this builder
     */
    public GUIItem amount(int itemAmount) {
        this.itemAmount = itemAmount;
        return this;
    }

    /**
     * Changes the material type.
     * <p>Note: calling any skull helper after this will override the type
     * to {@link Material#PLAYER_HEAD} again.</p>
     *
     * @param itemType new material
     * @return this builder
     */
    public GUIItem type(Material itemType) {
        this.itemType = itemType;
        return this;
    }

    /**
     * Sets the display name from a legacy-formatted or MiniMessage string.
     * Both {@code &} codes and {@code <tag>} syntax are supported via {@link Text}.
     *
     * @param itemName display name string
     * @return this builder
     */
    public GUIItem name(String itemName) {
        return name(Text.of(itemName));
    }

    /**
     * Sets the display name from a {@link Text} builder.
     *
     * @param itemName display name builder
     * @return this builder
     */
    public GUIItem name(Text itemName) {
        return name(itemName.build());
    }

    /**
     * Sets the display name from a raw Adventure {@link Component}.
     *
     * @param itemName display name component
     * @return this builder
     */
    public GUIItem name(Component itemName) {
        this.itemName = itemName;
        return this;
    }

    /**
     * Opts out of automatic italic removal.
     *
     * <p>By default {@link #build()} calls
     * {@code .decoration(TextDecoration.ITALIC, false)} on the display name and
     * every lore line, preventing Minecraft's built-in italic from showing.
     * Call this method if you intentionally want the vanilla italic appearance.</p>
     *
     * @return this builder
     */
    public GUIItem keepItalic() {
        this.keepItalic = true;
        return this;
    }

    // -------------------------------------------------------------------------
    // Lore
    // -------------------------------------------------------------------------

    /**
     * Appends a single lore line from a legacy/MiniMessage string.
     *
     * @param line lore text
     * @return this builder
     */
    public GUIItem lore(String line) { return loreLine(Text.of(line).build()); }

    /**
     * Appends a single lore line from an Adventure {@link Component}.
     *
     * @param line lore component
     * @return this builder
     */
    public GUIItem lore(Component line) { return loreLine(line); }

    /**
     * Appends a single lore line from a {@link Text} builder.
     *
     * @param line lore text builder
     * @return this builder
     */
    public GUIItem lore(Text line) { return loreLine(line.build()); }

    /**
     * Appends multiple lore lines from a mixed-type list.
     * Each element may be a {@link String}, {@link Text}, or {@link Component};
     * unknown types are silently ignored.
     *
     * @param lines list of lore entries
     * @return this builder
     */
    public GUIItem lore(List<Object> lines) {
        for (Object line : lines) {
            if (line instanceof String s)         loreLine(Text.of(s).build());
            else if (line instanceof Text t)      loreLine(t.build());
            else if (line instanceof Component c) loreLine(c);
        }
        return this;
    }

    /**
     * Appends multiple lore lines from a list of plain strings.
     * Each string is parsed by {@link Text}, so MiniMessage and legacy {@code &} codes work.
     *
     * @param lines lore strings
     * @return this builder
     */
    public GUIItem loreStrings(List<String> lines) {
        lines.forEach(l -> loreLine(Text.of(l).build()));
        return this;
    }

    /**
     * Appends multiple lore lines from a list of {@link Text} builders.
     *
     * @param lines lore text builders
     * @return this builder
     */
    public GUIItem loreTexts(List<Text> lines) {
        lines.forEach(l -> loreLine(l.build()));
        return this;
    }

    /**
     * Appends multiple lore lines from a list of Adventure {@link Component} objects.
     *
     * @param lines lore components
     * @return this builder
     */
    public GUIItem loreComponents(List<Component> lines) {
        lines.forEach(this::loreLine);
        return this;
    }

    /**
     * Internal helper — appends a single already-built component to the lore list.
     */
    private GUIItem loreLine(Component line) {
        this.itemLore.add(line);
        return this;
    }

    // -------------------------------------------------------------------------
    // Skull / head texture
    // -------------------------------------------------------------------------

    /**
     * Sets the skull owner to an {@link OfflinePlayer}.
     * The item will display that player's current skin.
     * Material is automatically set to {@link Material#PLAYER_HEAD}.
     *
     * @param player skull owner
     * @return this builder
     */
    public GUIItem skullOwner(OfflinePlayer player) {
        this.skullOwner = player;
        this.itemType = Material.PLAYER_HEAD;
        return this;
    }

    /**
     * Sets the skull owner by username.
     * Material is automatically set to {@link Material#PLAYER_HEAD}.
     *
     * <p><strong>Warning:</strong> {@link Bukkit#getOfflinePlayer(String)} may perform
     * a blocking network lookup when the player has never joined the server.
     * Prefer {@link #skullOwner(OfflinePlayer)} with a pre-fetched reference whenever
     * possible, especially on the main thread.</p>
     *
     * @param playerName exact player username
     * @return this builder
     */
    @SuppressWarnings("deprecation")
    public GUIItem skullOwner(String playerName) {
        this.skullOwner = Bukkit.getOfflinePlayer(playerName);
        this.itemType = Material.PLAYER_HEAD;
        return this;
    }

    /**
     * Sets a custom head texture via a textures.minecraft.net hash.
     *
     * <p>The hash is the trailing path segment of the full texture URL. For example,
     * given the URL {@code http://textures.minecraft.net/texture/abc123def456},
     * pass {@code "abc123def456"}.</p>
     *
     * <p>Internally the hash is wrapped in the standard base64 skin JSON before
     * being applied via Paper's {@link PlayerProfile} API, so the texture renders
     * correctly on all supported server versions.</p>
     *
     * <p>Material is automatically set to {@link Material#PLAYER_HEAD}.</p>
     *
     * @param textureHash hexadecimal texture hash
     * @return this builder
     */
    public GUIItem skullTexture(String textureHash) {
        this.skullTextureHash = textureHash;
        this.itemType = Material.PLAYER_HEAD;
        return this;
    }

    /**
     * Sets a custom head texture via a raw base64 skin Value string.
     *
     * <p>This is the full "Value" string found on sites like
     * <a href="https://minecraft-heads.com">minecraft-heads.com</a> under
     * "Other" → "For Developers". It base64-decodes to a JSON object
     * of the form {@code {"textures":{"SKIN":{"url":"http://..."}}}}.</p>
     *
     * <p>Material is automatically set to {@link Material#PLAYER_HEAD}.</p>
     *
     * @param base64Value base64-encoded skin Value string
     * @return this builder
     */
    public GUIItem skullTextureBase64(String base64Value) {
        this.skullTextureBase64 = base64Value;
        this.itemType = Material.PLAYER_HEAD;
        return this;
    }

    /**
     * Provides a low-level callback for full control over the {@link SkullMeta}.
     *
     * <p>When this callback is set it takes precedence over every other skull/texture
     * helper ({@link #skullOwner(OfflinePlayer)}, {@link #skullTexture(String)},
     * {@link #skullTextureBase64(String)}). The callback receives {@code this}
     * builder and the current {@link SkullMeta}, and must return the
     * (possibly mutated) meta.</p>
     *
     * @param onSkullMeta callback {@code (GUIItem, SkullMeta) → SkullMeta}
     * @return this builder
     */
    public GUIItem onSkullMeta(BiFunction<GUIItem, SkullMeta, SkullMeta> onSkullMeta) {
        this.onSkullMeta = onSkullMeta;
        return this;
    }

    // -------------------------------------------------------------------------
    // Interaction
    // -------------------------------------------------------------------------

    /**
     * Controls whether the player can take this item out of the GUI inventory.
     *
     * <p>Calling {@link #action(BiConsumer)} or {@link #onClick(BiConsumer)} always
     * resets this flag to {@code false}, because interactive buttons should not
     * be removable by default.</p>
     *
     * @param canPickUp {@code true} to allow the player to pick the item up
     * @return this builder
     */
    public GUIItem canPickUp(boolean canPickUp) {
        this.canPickUp = canPickUp;
        return this;
    }

    /**
     * Registers a click handler and marks the item as non-pickable.
     *
     * <p>The handler receives the raw {@link InventoryClickEvent} and the
     * {@link GUIContainer} the item belongs to, so you can update the GUI
     * state directly from within the callback.</p>
     *
     * @param action click handler {@code (InventoryClickEvent, GUIContainer) → void}
     * @return this builder
     */
    public GUIItem action(BiConsumer<InventoryClickEvent, GUIContainer> action) {
        this.canPickUp = false;
        this.action = action;
        return this;
    }

    /**
     * Alias for {@link #action(BiConsumer)} — use whichever reads more naturally
     * in context.
     *
     * @param action click handler
     * @return this builder
     */
    public GUIItem onClick(BiConsumer<InventoryClickEvent, GUIContainer> action) {
        return action(action);
    }

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    /**
     * Builds and returns the final {@link ItemStack}.
     *
     * <p>The following transformations are applied in order:</p>
     * <ol>
     *   <li>Stack size is set.</li>
     *   <li>Display name is applied; italic is stripped via
     *       {@link TextDecoration#ITALIC} unless {@link #keepItalic()} was called.</li>
     *   <li>Lore is applied; same italic stripping applies to every line.</li>
     *   <li>Skull/texture data is applied when the meta is a {@link SkullMeta}
     *       (priority: {@link #onSkullMeta} → {@link #skullOwner} →
     *       {@link #skullTextureBase64} → {@link #skullTextureHash}).</li>
     *   <li>The item's UUID is stored in the
     *       {@link org.bukkit.persistence.PersistentDataContainer} under the
     *       {@link GUI#ITEM_UUID_KEY} key so the {@link GUIListener} can route
     *       click events back to the correct handler.</li>
     * </ol>
     *
     * <p>{@link Material#AIR} is handled gracefully — the bare stack is returned
     * immediately without any meta operations.</p>
     *
     * @return built {@link ItemStack}
     */
    public ItemStack build() {
        ItemStack stack = new ItemStack(itemType);
        stack.setAmount(itemAmount);

        if (itemType == Material.AIR) return stack;

        ItemMeta meta = stack.hasItemMeta()
                ? stack.getItemMeta()
                : Bukkit.getItemFactory().getItemMeta(itemType);

        if (meta == null) return stack;

        // display name
        if (itemName != null) {
            Component name = keepItalic
                    ? itemName
                    : itemName.decoration(TextDecoration.ITALIC, false);
            meta.displayName(name);
        }

        // lore
        if (!itemLore.isEmpty()) {
            List<Component> lore = keepItalic
                    ? itemLore
                    : itemLore.stream()
                    .map(l -> l.decoration(TextDecoration.ITALIC, false))
                    .toList();
            meta.lore(lore);
        }

        // skull / texture
        if (meta instanceof SkullMeta skullMeta) {
            if (onSkullMeta != null) {
                meta = onSkullMeta.apply(this, skullMeta);
            } else if (skullOwner != null) {
                skullMeta.setOwningPlayer(skullOwner);
                meta = skullMeta;
            } else if (skullTextureBase64 != null) {
                meta = applyBase64Texture(skullMeta, skullTextureBase64);
            } else if (skullTextureHash != null) {
                String url  = "http://textures.minecraft.net/texture/" + skullTextureHash;
                String json = "{\"textures\":{\"SKIN\":{\"url\":\"" + url + "\"}}}";
                meta = applyBase64Texture(skullMeta,
                        Base64.getEncoder().encodeToString(json.getBytes()));
            }
        }

        // persistent UUID for click routing
        if (parentGUI != null) {
            NamespacedKey key = new NamespacedKey(parentGUI.getPlugin(), GUI.ITEM_UUID_KEY);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(key, PersistentDataType.STRING, uid.toString());
        }

        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * Extracts the texture URL from a base64 skin Value string and applies it
     * to the given {@link SkullMeta} via Paper's {@link PlayerProfile} API.
     *
     * <p>The base64 string decodes to a JSON object in the form
     * {@code {"textures":{"SKIN":{"url":"http://textures.minecraft.net/texture/..."}}}}.<br>
     * The URL is parsed from the JSON without a full JSON library by simple
     * string search. A freshly generated random {@link UUID} is used as the
     * profile ID so the texture is always applied regardless of caching.</p>
     *
     * <p>If the string is malformed or any exception occurs, the original
     * unmodified {@link SkullMeta} is returned and the exception is printed
     * to stderr.</p>
     *
     * @param skullMeta   skull meta to mutate
     * @param base64Value raw base64 skin Value string
     * @return the (possibly mutated) skull meta
     */
    private SkullMeta applyBase64Texture(SkullMeta skullMeta, String base64Value) {
        try {
            String decoded   = new String(Base64.getDecoder().decode(base64Value));
            String urlMarker = "\"url\":\"";
            int start = decoded.indexOf(urlMarker);
            if (start == -1) return skullMeta;
            start += urlMarker.length();
            int end = decoded.indexOf("\"", start);
            if (end == -1) return skullMeta;

            String textureUrl = decoded.substring(start, end);

            PlayerProfile  profile  = Bukkit.createProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URI(textureUrl).toURL());
            profile.setTextures(textures);
            skullMeta.setPlayerProfile(profile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return skullMeta;
    }

    // -------------------------------------------------------------------------
    // GUI / container shortcuts
    // -------------------------------------------------------------------------

    /**
     * Adds this item to the parent GUI at the given absolute slot index.
     * Requires {@link #gui()} to be non-{@code null}.
     *
     * @param slot absolute inventory slot (0-based)
     * @return this builder
     */
    public GUIItem addToGUI(int slot) { gui().addItem(this, slot); return this; }

    /**
     * Adds this item to the parent GUI at the given grid coordinates.
     * Requires {@link #gui()} to be non-{@code null}.
     *
     * @param x column (0-based)
     * @param y row (0-based)
     * @return this builder
     */
    public GUIItem addToGUI(int x, int y) { gui().addItem(this, x, y); return this; }

    // -------------------------------------------------------------------------
    // Static factories
    // -------------------------------------------------------------------------

    /**
     * Creates a new item builder attached to a GUI.
     *
     * @param gui      parent GUI
     * @param itemType material
     * @return new {@link GUIItem} builder
     */
    public static GUIItem create(GUI gui, Material itemType) {
        return new GUIItem(gui, itemType);
    }

    /**
     * Creates a standalone item builder not attached to any GUI.
     * The persistent UUID will <em>not</em> be embedded in the item's NBT,
     * so click routing will not work.
     *
     * @param itemType material
     * @return new {@link GUIItem} builder
     */
    public static GUIItem create(Material itemType) {
        return new GUIItem(null, itemType);
    }

    /**
     * Creates a player-head item owned by the given {@link OfflinePlayer}.
     *
     * @param gui    parent GUI
     * @param player skull owner
     * @return new builder with material {@link Material#PLAYER_HEAD}
     */
    public static GUIItem playerHead(GUI gui, OfflinePlayer player) {
        return new GUIItem(gui, Material.PLAYER_HEAD).skullOwner(player);
    }

    /**
     * Creates a player-head item owned by the given username.
     * See {@link #skullOwner(String)} for the blocking-lookup warning.
     *
     * @param gui        parent GUI
     * @param playerName exact player username
     * @return new builder with material {@link Material#PLAYER_HEAD}
     */
    public static GUIItem playerHead(GUI gui, String playerName) {
        return new GUIItem(gui, Material.PLAYER_HEAD).skullOwner(playerName);
    }

    /**
     * Creates a custom-texture head from a textures.minecraft.net hash.
     *
     * @param gui         parent GUI
     * @param textureHash hex hash (trailing segment of the texture URL)
     * @return new builder with material {@link Material#PLAYER_HEAD}
     */
    public static GUIItem customHead(GUI gui, String textureHash) {
        return new GUIItem(gui, Material.PLAYER_HEAD).skullTexture(textureHash);
    }

    /**
     * Creates a custom-texture head from a raw base64 skin Value string.
     *
     * @param gui         parent GUI
     * @param base64Value raw base64 Value string (e.g. from minecraft-heads.com)
     * @return new builder with material {@link Material#PLAYER_HEAD}
     */
    public static GUIItem customHeadBase64(GUI gui, String base64Value) {
        return new GUIItem(gui, Material.PLAYER_HEAD).skullTextureBase64(base64Value);
    }

    /**
     * Wraps an existing {@link ItemStack} in a new {@link GUIItem} builder.
     * The effective display name and lore are copied; no GUI or container is attached.
     *
     * @param item source item stack
     * @return new {@link GUIItem} builder
     */
    public static GUIItem fromItemStack(ItemStack item) {
        GUIItem newItem = create(item.getType()).name(item.effectiveName());
        List<Component> lore = item.lore();
        if (lore != null) newItem.loreComponents(lore);
        return newItem;
    }

    /**
     * Wraps the {@link ItemStack} of a dropped {@link Item} entity in a new builder.
     *
     * @param item dropped item entity
     * @return new {@link GUIItem} builder
     */
    public static GUIItem fromItem(Item item) {
        return fromItemStack(item.getItemStack());
    }
}
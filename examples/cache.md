# Creating Custom Cache in NotLib

This guide explains how to create and register custom caches in your plugin using the NotLib cache system.

## Overview

The NotLib cache system provides a flexible way to cache any type of data. You can create custom caches by:

1. Extending `BaseNotCache<T>` with your data type
2. Implementing the `hash()` method
3. Registering your cache with `NotCache`

## Step 1: Create Your Cache Class

Create a new class that extends `BaseNotCache<T>` where `T` is the type you want to cache.

```java
package com.yourplugin.cache;

import org.bukkit.Location;
import com.notmarra.notlib.cache.BaseNotCache;
import com.notmarra.notlib.extensions.NotPlugin;

public class LocationCache extends BaseNotCache<Location> {

    public LocationCache(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public String hash(Location source) {
        // Create a unique hash for the location
        return String.format("%s_%d_%d_%d",
            source.getWorld().getName(),
            source.getBlockX(),
            source.getBlockY(),
            source.getBlockZ()
        );
    }
}
```

### Important: The hash() Method

The `hash()` method must return a **unique identifier** for each cached object. This is used as the key in the internal storage.

**Good hash examples:**

- UUID: `uuid.toString()`
- Coordinates: `"world_x_y_z"`
- Compound keys: `"playerUUID_itemType"`
- Names: `name.toLowerCase()`

**Bad hash examples:**

- Non-unique values: `"cache"` (same for all objects)
- Changing values: `System.currentTimeMillis()` (changes every time)
- Complex objects without toString: `object.hashCode()` (may collide)

## Step 2: Register Your Cache

In your plugin's `onEnable()` method, create an instance of your cache and register it with NotCache.

```java
package com.yourplugin;

import com.notmarra.notlib.cache.NotCache;
import com.notmarra.notlib.extensions.NotPlugin;
import com.yourplugin.cache.LocationCache;

public class YourPlugin extends NotPlugin {

    private LocationCache locationCache;

    @Override
    public void onEnable() {
        // Initialize NotCache first
        NotCache.initialize(this);

        // Create your cache
        locationCache = new LocationCache(this);

        // Register it with a unique key
        NotCache.getInstance().registerCache("locations", locationCache);

        getLogger().info("LocationCache registered!");
    }

    @Override
    public void onDisable() {
        // Clean up: unregister your cache
        NotCache.getInstance().unregisterCache("locations");
    }

    // Getter for easy access
    public LocationCache getLocationCache() {
        return locationCache;
    }
}
```

## Step 3: Use Your Cache

There are three ways to access your custom cache:

### Method A: Direct Reference (Recommended)

Store a reference to your cache in your plugin class and access it directly.

```java
public class WarpCommand implements CommandExecutor {
    private final YourPlugin plugin;

    public WarpCommand(YourPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Location warp = new Location(world, 100, 64, 100);

        // Store location
        plugin.getLocationCache().store(warp);

        // Retrieve location
        String hash = plugin.getLocationCache().hash(warp);
        Location cached = plugin.getLocationCache().get(hash);

        return true;
    }
}
```

### Method B: Via NotCache Registry

Retrieve your cache from the NotCache registry when needed.

```java
public class WarpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Get cache from registry
        LocationCache cache = NotCache.getInstance()
            .getCache("locations", LocationCache.class);

        if (cache != null) {
            Location warp = new Location(world, 100, 64, 100);
            cache.store(warp);
        }

        return true;
    }
}
```

### Method C: Generic Access

Access without specifying the cache type.

```java
BaseNotCache<Location> cache = NotCache.getInstance().getCache("locations");

if (cache != null) {
    Location warp = new Location(world, 100, 64, 100);
    cache.store(warp);
}
```

## Advanced Examples

### Example 1: Item Cache

```java
package com.yourplugin.cache;

import org.bukkit.inventory.ItemStack;
import com.notmarra.notlib.cache.BaseNotCache;
import com.notmarra.notlib.extensions.NotPlugin;

public class ItemCache extends BaseNotCache<ItemStack> {

    public ItemCache(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public String hash(ItemStack source) {
        // Use material and custom model data as hash
        int customModelData = source.hasItemMeta() && source.getItemMeta().hasCustomModelData()
            ? source.getItemMeta().getCustomModelData()
            : 0;
        return source.getType().name() + "_" + customModelData;
    }

    // Custom method: get or create with default
    public ItemStack getOrCreate(String hash, ItemStack defaultItem) {
        ItemStack cached = get(hash);
        if (cached != null) {
            return cached.clone(); // Return clone to prevent modifications
        }
        store(defaultItem);
        return defaultItem.clone();
    }
}
```

### Example 2: Player Data Cache with Events

```java
package com.yourplugin.cache;

import java.util.UUID;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.notmarra.notlib.cache.BaseNotCache;
import com.notmarra.notlib.extensions.NotPlugin;

public class PlayerDataCache extends BaseNotCache<PlayerData> implements Listener {

    public PlayerDataCache(NotPlugin plugin) {
        super(plugin);
        // Register events for automatic cleanup
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public String hash(PlayerData source) {
        return source.getUuid().toString();
    }

    // Custom method: get or create new data
    public PlayerData getOrCreate(UUID uuid) {
        String hash = uuid.toString();
        PlayerData data = get(hash);

        if (data == null) {
            data = new PlayerData(uuid);
            store(data);
        }

        return data;
    }

    // Automatic cleanup when player quits
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        String hash = event.getPlayer().getUniqueId().toString();
        PlayerData data = get(hash);

        if (data != null) {
            // Optional: save to database before removing
            saveToDatabase(data);
            remove(hash);
        }
    }

    private void saveToDatabase(PlayerData data) {
        // Your database save logic here
    }
}

// Data class
class PlayerData {
    private final UUID uuid;
    private int coins;
    private int level;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
        this.coins = 0;
        this.level = 1;
    }

    public UUID getUuid() { return uuid; }
    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
```

**Usage:**

```java
// Get or create player data
PlayerData data = plugin.getPlayerDataCache().getOrCreate(player.getUniqueId());
data.setCoins(data.getCoins() + 100);

// Data is automatically saved and removed when player quits
```

### Example 3: Cooldown Cache

```java
package com.yourplugin.cache;

import java.util.UUID;
import com.notmarra.notlib.cache.BaseNotCache;
import com.notmarra.notlib.extensions.NotPlugin;

public class CooldownCache extends BaseNotCache<Cooldown> {

    public CooldownCache(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public String hash(Cooldown source) {
        return source.getPlayerUuid() + "_" + source.getAction();
    }

    public boolean isOnCooldown(UUID playerUuid, String action) {
        String hash = playerUuid.toString() + "_" + action;
        Cooldown cooldown = get(hash);

        if (cooldown == null) {
            return false;
        }

        if (cooldown.isExpired()) {
            remove(hash);
            return false;
        }

        return true;
    }

    public void setCooldown(UUID playerUuid, String action, long durationMillis) {
        Cooldown cooldown = new Cooldown(playerUuid, action,
            System.currentTimeMillis() + durationMillis);
        store(cooldown);
    }

    public long getRemainingTime(UUID playerUuid, String action) {
        String hash = playerUuid.toString() + "_" + action;
        Cooldown cooldown = get(hash);

        if (cooldown == null || cooldown.isExpired()) {
            return 0;
        }

        return cooldown.getExpiresAt() - System.currentTimeMillis();
    }
}

class Cooldown {
    private final UUID playerUuid;
    private final String action;
    private final long expiresAt;

    public Cooldown(UUID playerUuid, String action, long expiresAt) {
        this.playerUuid = playerUuid;
        this.action = action;
        this.expiresAt = expiresAt;
    }

    public UUID getPlayerUuid() { return playerUuid; }
    public String getAction() { return action; }
    public long getExpiresAt() { return expiresAt; }

    public boolean isExpired() {
        return System.currentTimeMillis() >= expiresAt;
    }
}
```

**Usage:**

```java
CooldownCache cooldowns = plugin.getCooldownCache();

// Check cooldown
if (cooldowns.isOnCooldown(player.getUniqueId(), "teleport")) {
    long remaining = cooldowns.getRemainingTime(player.getUniqueId(), "teleport");
    player.sendMessage("Cooldown: " + (remaining / 1000) + " seconds");
    return;
}

// Set cooldown (5 seconds)
cooldowns.setCooldown(player.getUniqueId(), "teleport", 5000);
player.teleport(destination);
```

## Registering Multiple Caches

You can register as many custom caches as you need:

```java
@Override
public void onEnable() {
    NotCache.initialize(this);

    // Create all your caches
    locationCache = new LocationCache(this);
    playerDataCache = new PlayerDataCache(this);
    itemCache = new ItemCache(this);
    cooldownCache = new CooldownCache(this);

    // Register them with unique keys
    NotCache.getInstance().registerCache("locations", locationCache);
    NotCache.getInstance().registerCache("playerdata", playerDataCache);
    NotCache.getInstance().registerCache("items", itemCache);
    NotCache.getInstance().registerCache("cooldowns", cooldownCache);

    getLogger().info("All caches registered!");
}

@Override
public void onDisable() {
    // Unregister all caches
    NotCache.getInstance().unregisterCache("locations");
    NotCache.getInstance().unregisterCache("playerdata");
    NotCache.getInstance().registerCache("items");
    NotCache.getInstance().unregisterCache("cooldowns");
}
```

## Available Methods from BaseNotCache

All custom caches automatically inherit these methods:

```java
// Store an object
T store(T source)

// Retrieve by hash
T get(String hash)

// Remove by hash
T remove(String hash)

// Remove by object
T remove(T source)

// Check if exists
boolean contains(String hash)

// Get cache size
int size()

// Clear all cached data
void clear()
```

## Best Practices

### 1. Choose Unique Hash Keys

Your `hash()` method must return unique identifiers to prevent collisions.

```java
// Good - unique per player and action
@Override
public String hash(PlayerAction source) {
    return source.getPlayerUuid() + "_" + source.getActionType();
}

// Bad - not unique
@Override
public String hash(PlayerAction source) {
    return source.getActionType(); // Multiple players can have same action!
}
```

### 2. Implement Event Listeners When Needed

If your cached data needs cleanup (like player data), implement `Listener` and register appropriate events.

```java
public class MyCache extends BaseNotCache<MyData> implements Listener {
    public MyCache(NotPlugin plugin) {
        super(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Clean up player data
    }
}
```

### 3. Return Clones for Mutable Objects

If your cached objects are mutable (like ItemStack), return clones to prevent external modifications.

```java
public ItemStack getItem(String hash) {
    ItemStack cached = get(hash);
    return cached != null ? cached.clone() : null;
}
```

### 4. Add Custom Helper Methods

Extend your cache with convenience methods for common operations.

```java
public class MyCache extends BaseNotCache<MyData> {
    // ... hash() implementation ...

    // Helper: get or create
    public MyData getOrCreate(String key, Supplier<MyData> creator) {
        MyData data = get(key);
        if (data == null) {
            data = creator.get();
            store(data);
        }
        return data;
    }

    // Helper: batch store
    public void storeAll(Collection<MyData> items) {
        items.forEach(this::store);
    }
}
```

### 5. Document Your Cache

Add javadoc comments explaining what gets cached and how to use it.

```java
/**
 * Cache for storing player warp locations.
 * Locations are cached by their coordinates and world name.
 * Cache is automatically cleared when the plugin disables.
 */
public class LocationCache extends BaseNotCache<Location> {
    // ...
}
```

### 6. Clean Up on Disable

Always unregister your caches in `onDisable()` to free memory.

```java
@Override
public void onDisable() {
    NotCache.getInstance().unregisterCache("mykey");
}
```

## Thread Safety

The cache system uses `ConcurrentHashMap` internally, making it thread-safe for basic operations. However, if you're performing complex operations or modifying cached objects, consider additional synchronization.

```java
public synchronized void updatePlayerData(UUID uuid, Consumer<PlayerData> updater) {
    PlayerData data = getOrCreate(uuid);
    updater.accept(data);
    store(data); // Re-store to ensure consistency
}
```

## Summary

Creating a custom cache requires:

1. Extend `BaseNotCache<T>` with your data type
2. Implement `hash()` to return unique identifiers
3. Register with `NotCache.getInstance().registerCache(key, cache)`
4. Access via direct reference or registry
5. Unregister in `onDisable()`

The cache system is flexible and allows you to cache any type of data your plugin needs!

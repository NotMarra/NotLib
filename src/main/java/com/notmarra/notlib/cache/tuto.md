# how to use:

### custom cache type:
```java
public enum CustomCacheTypes implements ICacheType {
    ITEMS("custom_items");
    
    private final String cacheKey;
    
    CustomCacheTypes(String cacheKey) {
        this.cacheKey = cacheKey;
    }
    
    @Override
    public String getCacheKey() {
        return cacheKey;
    }
}
```

### implementation:
```java
public class CustomItemCache extends BaseNotCache<ItemStack> {

    public CustomItemCache(NotPlugin plugin) {
        super(plugin);
    }

    @Override
    public String hash(ItemStack source) {
        // Custom hash logic for ItemStack
        return source.getType().name() + "_" + source.getAmount();
    }
    
    // Custom methods specific to ItemStack
    public ItemStack getByType(String materialName) {
        return storage.values().stream()
            .filter(item -> item.getType().name().equals(materialName))
            .findFirst()
            .orElse(null);
    }
}
```

### how to use:
```java
public class ExampleUsage {
    
    public void setupCache(NotPlugin plugin) {
        NotCache notCache = NotCache.initialize(plugin);
        
        // Register custom cache
        notCache.registerCacheType(CustomCacheTypes.ITEMS, CustomItemCache::new);
    }
    
    public void useCache() {
        // Using the default player cache
        NotPlayerCache playerCache = NotCache.player();
        // or
        NotPlayerCache playerCache2 = NotCache.cache(DefaultCacheTypes.PLAYER);
            
        // Using the custom cache
        CustomItemCache itemCache = NotCache.cache(CustomCacheTypes.ITEMS);
            
        // ItemStack item = new ItemStack(Material.DIAMOND);
        // itemCache.store(item);
    }
}
```

or

```java
// Register new cache
notCache.registerCacheType(CustomCacheTypes.ITEMS, CustomItemCache::new);

// Usage
CustomItemCache itemCache = NotCache.cache(CustomCacheTypes.ITEMS);
NotPlayerCache playerCache = NotCache.player();
```
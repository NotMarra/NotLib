package com.notmarra.notlib.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotCache {
    public final NotPlugin plugin;
    public static NotCache instance;

    private final NotPlayerCache playerCache;
    private final Map<String, BaseNotCache<?>> customCaches = new ConcurrentHashMap<>();

    public NotCache(NotPlugin plugin) {
        this.plugin = plugin;
        
        registerCacheType(DefaultCacheTypes.PLAYER, NotPlayerCache::new);
    }


    @SuppressWarnings("unchecked")
    public <T extends BaseNotCache<?>> void registerCacheType(ICacheType cacheType, Function<NotPlugin, T> factory) {
        cacheFactories.put(cacheType.getCacheKey(), (Function<NotPlugin, BaseNotCache<?>>) factory);
    }


    @SuppressWarnings("unchecked")
    public <T extends BaseNotCache<?>> T getCache(ICacheType cacheType) {
        String key = cacheType.getCacheKey();
        
        BaseNotCache<?> cache = caches.get(key);
        if (cache != null) {
            return (T) cache;
        }
        
        Function<NotPlugin, BaseNotCache<?>> factory = cacheFactories.get(key);
        if (factory == null) {
            throw new IllegalArgumentException("Cache type '" + key + "' is not registered");
        }
        
        cache = factory.apply(plugin);
        caches.put(key, cache);
        return (T) cache;
    }

    public NotPlayerCache playerCache() {
        return getCache(DefaultCacheTypes.PLAYER);
    }

    public void clearAll() {
        caches.values().forEach(BaseNotCache::clear);
    }

    public NotPlayerCache playerCache() {
        return playerCache;
    }

    /**
     * Register a custom cache
     * 
     * @param key   Unique identifier for the cache
     * @param cache The cache instance to register
     */
    public <T> void registerCache(String key, BaseNotCache<T> cache) {
        customCaches.put(key, cache);
    }

    /**
     * Get a registered custom cache
     * 
     * @param key  The cache identifier
     * @param type The cache class type
     * @return The cache instance or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseNotCache<?>> T getCache(String key, Class<T> type) {
        BaseNotCache<?> cache = customCaches.get(key);
        if (cache != null && type.isInstance(cache)) {
            return (T) cache;
        }
        return null;
    }

    /**
     * Get a registered custom cache
     * 
     * @param key The cache identifier
     * @return The cache instance or null if not found
     */
    @SuppressWarnings("unchecked")
    public <T> BaseNotCache<T> getCache(String key) {
        return (BaseNotCache<T>) customCaches.get(key);
    }

    /**
     * Unregister a custom cache
     * 
     * @param key The cache identifier
     */
    public void unregisterCache(String key) {
        BaseNotCache<?> cache = customCaches.remove(key);
        if (cache != null) {
            cache.clear();
        }
    }

    public static NotCache initialize(NotPlugin plugin) {
        if (NotCache.instance != null) {
            NotCache.instance.clearAll();
        }
        NotCache.instance = new NotCache(plugin);
        return NotCache.instance;
    }

    public static NotPlayerCache player() {
        return NotCache.instance.playerCache();
    }

    public static NotCache getInstance() {
        return instance;
    }
}
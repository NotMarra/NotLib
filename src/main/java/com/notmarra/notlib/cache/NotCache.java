package com.notmarra.notlib.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotCache {
    public final NotPlugin plugin;
    public static NotCache instance;
    
    private final Map<String, BaseNotCache<?>> caches = new HashMap<>();
    private final Map<String, Function<NotPlugin, BaseNotCache<?>>> cacheFactories = new HashMap<>();

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


    public void clearCache(ICacheType cacheType) {
        BaseNotCache<?> cache = caches.get(cacheType.getCacheKey());
        if (cache != null) {
            cache.clear();
        }
    }


    public void removeCache(ICacheType cacheType) {
        BaseNotCache<?> cache = caches.remove(cacheType.getCacheKey());
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

    public static <T extends BaseNotCache<?>> T cache(ICacheType cacheType) {
        return NotCache.instance.getCache(cacheType);
    }

    public static void shutdown() {
        if (NotCache.instance != null) {
            NotCache.instance.clearAll();
            NotCache.instance = null;
        }
    }
}
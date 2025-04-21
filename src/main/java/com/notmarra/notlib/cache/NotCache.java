package com.notmarra.notlib.cache;

import com.notmarra.notlib.extensions.NotPlugin;

public class NotCache {
    public final NotPlugin plugin;
    public static NotCache instance;
    
    private final NotPlayerCache playerCache;

    public NotCache(NotPlugin plugin) {
        this.plugin = plugin;
        this.playerCache = new NotPlayerCache(plugin);
    }

    public NotPlayerCache playerCache() { return playerCache; }

    public static NotCache initialize(NotPlugin plugin) {
        NotCache.instance = new NotCache(plugin);
        return NotCache.instance;
    }

    public static NotPlayerCache player() {
        return NotCache.instance.playerCache();
    }
}

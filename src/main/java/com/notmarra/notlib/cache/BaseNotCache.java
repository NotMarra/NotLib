package com.notmarra.notlib.cache;

import java.util.HashMap;
import java.util.Map;

import com.notmarra.notlib.extensions.NotPlugin;

public abstract class BaseNotCache<T> {
    public final NotPlugin plugin;

    private Map<String, T> storage = new HashMap<>(); 

    public BaseNotCache(NotPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract String hash(T source);

    public T get(String hash) { return storage.get(hash); }

    public T store(T source) {
        storage.put(hash(source), source);
        return source;
    }
}

package com.notmarra.notlib.cache;

import java.util.HashMap;
import java.util.Map;

import com.notmarra.notlib.extensions.NotPlugin;

public abstract class BaseNotCache<T> {
    public final NotPlugin plugin;

    protected Map<String, T> storage = new HashMap<>(); 

    public BaseNotCache(NotPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract String hash(T source);

    public T get(String hash) { 
        return storage.get(hash); 
    }

    public T store(T source) {
        storage.put(hash(source), source);
        return source;
    }

    public void remove(T source) {
        storage.remove(hash(source));
    }

    public void remove(String hash) {
        storage.remove(hash);
    }

    public void clear() {
        storage.clear();
    }

    public int size() {
        return storage.size();
    }

    public boolean contains(T source) {
        return storage.containsKey(hash(source));
    }

    public boolean contains(String hash) {
        return storage.containsKey(hash);
    }
}
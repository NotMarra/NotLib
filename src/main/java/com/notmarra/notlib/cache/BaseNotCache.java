package com.notmarra.notlib.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.notmarra.notlib.extensions.NotPlugin;

public abstract class BaseNotCache<T> {
    protected final NotPlugin plugin;
    private final Map<String, T> storage = new ConcurrentHashMap<>();

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

    public T remove(String hash) {
        return storage.remove(hash);
    }

    public T remove(T source) {
        return remove(hash(source));
    }

    public void clear() {
        storage.clear();
    }

    public int size() {
        return storage.size();
    }

    public boolean contains(String hash) {
        return storage.containsKey(hash);
    }

    protected Map<String, T> getStorage() {
        return storage;
    }
}
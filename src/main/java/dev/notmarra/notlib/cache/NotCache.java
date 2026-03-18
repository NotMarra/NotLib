package dev.notmarra.notlib.cache;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Thread-safe in-memory cache with configurable TTL, eviction policy,
 * dirty-write tracking, and background expiry cleanup.
 *
 * <pre>{@code
 * NotCache<UUID, PlayerProfile> cache = NotCache.<UUID, PlayerProfile>builder()
 *         .maxSize(1000)
 *         .ttlMillis(5 * 60 * 1000)        // 5 minutes
 *         .evictionPolicy(CacheEvictionPolicy.LRU)
 *         .build();
 *
 * cache.put(player.getUniqueId(), profile);
 * Optional<PlayerProfile> hit = cache.get(player.getUniqueId());
 * }</pre>
 *
 * @param <K> key type
 * @param <V> value type
 */
public class NotCache<K, V> {

    private static final Logger LOGGER = Logger.getLogger(NotCache.class.getName());

    // ── INTERNAL STORAGE ─────────────────────────────────────────────────────

    // LinkedHashMap preserves insertion order (FIFO) and access order (LRU)
    private final LinkedHashMap<K, CacheEntry<V>> store;
    private final Object lock = new Object();

    // ── CONFIGURATION ────────────────────────────────────────────────────────

    private final int maxSize;
    private final long defaultTtlMillis;
    private final CacheEvictionPolicy evictionPolicy;

    // ── STATS ────────────────────────────────────────────────────────────────

    private final AtomicLong hits       = new AtomicLong();
    private final AtomicLong misses     = new AtomicLong();
    private final AtomicLong evictions  = new AtomicLong();
    private final AtomicLong expirations = new AtomicLong();

    // ── BACKGROUND CLEANUP ───────────────────────────────────────────────────

    private final ScheduledExecutorService scheduler;

    // ── CONSTRUCTOR ──────────────────────────────────────────────────────────

    private NotCache(Builder<K, V> b) {
        this.maxSize = b.maxSize;
        this.defaultTtlMillis = b.ttlMillis;
        this.evictionPolicy = b.evictionPolicy;

        boolean accessOrder = evictionPolicy == CacheEvictionPolicy.LRU;
        this.store = new LinkedHashMap<>(16, 0.75f, accessOrder) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, CacheEntry<V>> eldest) {
                // Actual eviction is handled manually so we can track stats
                return false;
            }
        };

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "NotCache-Cleanup");
            t.setDaemon(true);
            return t;
        });

        if (b.cleanupIntervalMillis > 0) {
            scheduler.scheduleAtFixedRate(
                    this::evictExpired,
                    b.cleanupIntervalMillis,
                    b.cleanupIntervalMillis,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    // ── PUBLIC API ───────────────────────────────────────────────────────────

    /** Store a value using the default TTL. */
    public void put(K key, V value) {
        put(key, value, defaultTtlMillis);
    }

    /** Store a value with a custom TTL (milliseconds). 0 = immortal. */
    public void put(K key, V value, long ttlMillis) {
        synchronized (lock) {
            store.put(key, new CacheEntry<>(value, ttlMillis));
            enforceCapacity();
        }
    }

    /**
     * Retrieve a value. Returns empty if missing or expired.
     */
    public Optional<V> get(K key) {
        synchronized (lock) {
            CacheEntry<V> entry = store.get(key);
            if (entry == null) {
                misses.incrementAndGet();
                return Optional.empty();
            }
            if (entry.isExpired()) {
                store.remove(key);
                expirations.incrementAndGet();
                misses.incrementAndGet();
                return Optional.empty();
            }
            hits.incrementAndGet();
            return Optional.ofNullable(entry.getValue());
        }
    }

    /**
     * Return cached value if present; otherwise compute, cache, and return it.
     */
    public V getOrLoad(K key, Function<K, V> loader) {
        return getOrLoad(key, loader, defaultTtlMillis);
    }

    public V getOrLoad(K key, Function<K, V> loader, long ttlMillis) {
        Optional<V> cached = get(key);
        if (cached.isPresent()) return cached.get();
        V value = loader.apply(key);
        if (value != null) put(key, value, ttlMillis);
        return value;
    }

    /**
     * Mark an existing entry as dirty (needs write-back to DB).
     */
    public void markDirty(K key) {
        synchronized (lock) {
            CacheEntry<V> entry = store.get(key);
            if (entry != null) entry.markDirty();
        }
    }

    /**
     * Update value in cache and mark it dirty.
     */
    public void update(K key, V value) {
        synchronized (lock) {
            CacheEntry<V> entry = store.get(key);
            if (entry != null) {
                entry.setValue(value); // setValue marks dirty internally
            } else {
                CacheEntry<V> newEntry = new CacheEntry<>(value, defaultTtlMillis);
                newEntry.markDirty();
                store.put(key, newEntry);
                enforceCapacity();
            }
        }
    }

    /** Remove an entry from cache. */
    public void invalidate(K key) {
        synchronized (lock) {
            store.remove(key);
        }
    }

    /** Remove all entries. */
    public void invalidateAll() {
        synchronized (lock) {
            store.clear();
        }
    }

    /** Check whether the cache contains a non-expired entry for this key. */
    public boolean contains(K key) {
        return get(key).isPresent();
    }

    // ── DIRTY TRACKING ───────────────────────────────────────────────────────

    /**
     * Returns all entries that have been modified but not yet persisted.
     */
    public Map<K, V> getDirtyEntries() {
        synchronized (lock) {
            return store.entrySet().stream()
                    .filter(e -> e.getValue().isDirty() && !e.getValue().isExpired())
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
        }
    }

    /**
     * Mark all currently dirty entries as clean (call after successful flush).
     */
    public void markAllClean() {
        synchronized (lock) {
            store.values().forEach(CacheEntry::markClean);
        }
    }

    // ── EXPIRY / EVICTION ────────────────────────────────────────────────────

    /** Remove all expired entries. Called automatically by background thread. */
    public int evictExpired() {
        synchronized (lock) {
            List<K> toRemove = store.entrySet().stream()
                    .filter(e -> e.getValue().isExpired())
                    .map(Map.Entry::getKey)
                    .toList();
            toRemove.forEach(store::remove);
            expirations.addAndGet(toRemove.size());
            return toRemove.size();
        }
    }

    private void enforceCapacity() {
        if (evictionPolicy == CacheEvictionPolicy.TTL_ONLY) return;
        while (store.size() > maxSize) {
            K eldest = store.keySet().iterator().next(); // LRU or FIFO depending on accessOrder
            store.remove(eldest);
            evictions.incrementAndGet();
        }
    }

    // ── STATS ────────────────────────────────────────────────────────────────

    public CacheStats getStats() {
        synchronized (lock) {
            return new CacheStats(
                    hits.get(), misses.get(),
                    evictions.get(), expirations.get(),
                    store.size(), maxSize
            );
        }
    }

    public void resetStats() {
        hits.set(0); misses.set(0);
        evictions.set(0); expirations.set(0);
    }

    public int size() {
        synchronized (lock) { return store.size(); }
    }

    // ── LIFECYCLE ────────────────────────────────────────────────────────────

    public void shutdown() {
        scheduler.shutdownNow();
        LOGGER.info("NotCache shut down. Final stats: " + getStats());
    }

    // ── BUILDER ──────────────────────────────────────────────────────────────

    public static <K, V> Builder<K, V> builder() {
        return new Builder<>();
    }

    public static class Builder<K, V> {
        private int maxSize = 500;
        private long ttlMillis = 5 * 60 * 1000L; // 5 minutes default
        private CacheEvictionPolicy evictionPolicy = CacheEvictionPolicy.LRU;
        private long cleanupIntervalMillis = 60_000L; // cleanup every minute

        public Builder<K, V> maxSize(int maxSize) {
            this.maxSize = maxSize;
            return this;
        }

        public Builder<K, V> ttlMillis(long ttlMillis) {
            this.ttlMillis = ttlMillis;
            return this;
        }

        public Builder<K, V> ttlSeconds(long seconds) {
            return ttlMillis(seconds * 1000L);
        }

        public Builder<K, V> ttlMinutes(long minutes) {
            return ttlMillis(minutes * 60 * 1000L);
        }

        public Builder<K, V> evictionPolicy(CacheEvictionPolicy policy) {
            this.evictionPolicy = policy;
            return this;
        }

        public Builder<K, V> cleanupIntervalMillis(long ms) {
            this.cleanupIntervalMillis = ms;
            return this;
        }

        public NotCache<K, V> build() {
            return new NotCache<>(this);
        }
    }
}

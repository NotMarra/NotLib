package dev.notmarra.notlib.cache;

import dev.notmarra.notlib.database.Database;
import dev.notmarra.notlib.database.EntityTable;
import dev.notmarra.notlib.database.repository.EntityRepository;
import dev.notmarra.notlib.database.repository.QueryBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * A drop-in replacement for {@link EntityRepository} that layers a
 * {@link NotCache} in front of every read and respects a configurable
 * {@link WriteStrategy} for every write.
 *
 * <h3>Quick start</h3>
 * <pre>{@code
 * CachedRepository<UUID, PlayerProfile> repo = CachedRepository
 *         .<UUID, PlayerProfile>builder(database, PlayerProfile.class)
 *         .writeStrategy(WriteStrategy.WRITE_BEHIND)
 *         .flushIntervalSeconds(30)
 *         .cache(NotCache.<UUID, PlayerProfile>builder()
 *                 .maxSize(1000)
 *                 .ttlMinutes(10)
 *                 .evictionPolicy(CacheEvictionPolicy.LRU)
 *                 .build())
 *         .build();
 *
 * // reads hit cache first
 * Optional<PlayerProfile> profile = repo.findById(player.getUniqueId());
 *
 * // writes respect the selected strategy
 * repo.upsert(updatedProfile);
 *
 * // on shutdown – flush all pending writes
 * repo.flush();
 * repo.close();
 * }</pre>
 *
 * @param <K> primary-key type
 * @param <V> entity type
 */
public class CachedRepository<K, V> {

    private static final Logger LOGGER = Logger.getLogger(CachedRepository.class.getName());

    // ── CORE COMPONENTS ──────────────────────────────────────────────────────

    private final EntityRepository<V> repo;
    private final NotCache<K, V> cache;
    private final WriteStrategy writeStrategy;
    private final Field pkField;

    // ── WRITE-BEHIND FLUSH ───────────────────────────────────────────────────

    private final ScheduledExecutorService flushScheduler;
    private final Object flushLock = new Object();

    // ── CONSTRUCTOR ──────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private CachedRepository(Builder<K, V> b) {
        this.repo = new EntityRepository<>(b.database, b.entityClass);
        this.cache = b.cache;
        this.writeStrategy = b.writeStrategy;

        // Resolve PK field via EntityTable reflection (re-use existing infra)
        EntityTable<V> table = new EntityTable<>(b.entityClass, b.database.getDialect());
        this.pkField = table.getPrimaryKey()
                .map(fc -> fc.field())
                .orElseThrow(() -> new IllegalArgumentException(
                        b.entityClass.getName() + " must have a @Column(primaryKey=true) field"));
        this.pkField.setAccessible(true);

        // Background flush (only relevant for WRITE_BEHIND)
        this.flushScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "CachedRepo-Flush");
            t.setDaemon(true);
            return t;
        });

        if (writeStrategy == WriteStrategy.WRITE_BEHIND && b.flushIntervalMillis > 0) {
            flushScheduler.scheduleAtFixedRate(
                    this::flush,
                    b.flushIntervalMillis,
                    b.flushIntervalMillis,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    // ── CREATE TABLE ─────────────────────────────────────────────────────────

    public void createTable() {
        repo.createTable();
    }

    // ── FIND (cache-first) ───────────────────────────────────────────────────

    public Optional<V> findById(K id) {
        return cache.get(id).map(Optional::of).orElseGet(() -> {
            Optional<V> fromDb = repo.findById(id);
            fromDb.ifPresent(v -> cache.put(id, v));
            return fromDb;
        });
    }

    public CompletableFuture<Optional<V>> findByIdAsync(K id) {
        return CompletableFuture.supplyAsync(() -> findById(id));
    }

    public List<V> findAll() {
        List<V> all = repo.findAll();
        all.forEach(v -> cache.put(extractKey(v), v));
        return all;
    }

    public CompletableFuture<List<V>> findAllAsync() {
        return CompletableFuture.supplyAsync(this::findAll);
    }

    public boolean exists(K id) {
        if (cache.contains(id)) return true;
        return repo.exists(id);
    }

    public CompletableFuture<Boolean> existsAsync(K id) {
        return CompletableFuture.supplyAsync(() -> exists(id));
    }

    // ── INSERT ───────────────────────────────────────────────────────────────

    public void insert(V entity) {
        K key = extractKey(entity);
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                repo.insert(entity);
                cache.put(key, entity);
            }
            case WRITE_BEHIND -> {
                cache.update(key, entity); // marks dirty
            }
            case READ_THROUGH -> {
                repo.insert(entity);
                cache.invalidate(key); // force re-fetch next read
            }
        }
    }

    public CompletableFuture<Void> insertAsync(V entity) {
        return CompletableFuture.runAsync(() -> insert(entity));
    }

    public void insertAll(List<V> entities) {
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                repo.insertAll(entities);
                entities.forEach(e -> cache.put(extractKey(e), e));
            }
            case WRITE_BEHIND -> {
                entities.forEach(e -> cache.update(extractKey(e), e));
            }
            case READ_THROUGH -> {
                repo.insertAll(entities);
                entities.forEach(e -> cache.invalidate(extractKey(e)));
            }
        }
    }

    public CompletableFuture<Void> insertAllAsync(List<V> entities) {
        return CompletableFuture.runAsync(() -> insertAll(entities));
    }

    // ── UPDATE ───────────────────────────────────────────────────────────────

    public void update(V entity) {
        K key = extractKey(entity);
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                repo.update(entity);
                cache.put(key, entity);
            }
            case WRITE_BEHIND -> {
                cache.update(key, entity);
            }
            case READ_THROUGH -> {
                repo.update(entity);
                cache.invalidate(key);
            }
        }
    }

    public CompletableFuture<Void> updateAsync(V entity) {
        return CompletableFuture.runAsync(() -> update(entity));
    }

    // ── UPSERT ───────────────────────────────────────────────────────────────

    public void upsert(V entity) {
        K key = extractKey(entity);
        switch (writeStrategy) {
            case WRITE_THROUGH -> {
                repo.upsert(entity);
                cache.put(key, entity);
            }
            case WRITE_BEHIND -> {
                cache.update(key, entity);
            }
            case READ_THROUGH -> {
                repo.upsert(entity);
                cache.invalidate(key);
            }
        }
    }

    public CompletableFuture<Void> upsertAsync(V entity) {
        return CompletableFuture.runAsync(() -> upsert(entity));
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    public void delete(K id) {
        cache.invalidate(id);
        repo.delete(id);
    }

    public CompletableFuture<Void> deleteAsync(K id) {
        return CompletableFuture.runAsync(() -> delete(id));
    }

    // ── QUERY BUILDER (bypasses cache – use for complex queries) ──────────────

    /**
     * Returns the underlying {@link QueryBuilder} for complex WHERE/ORDER queries.
     * Results from this builder are NOT automatically cached. Use
     * {@link #cacheAll(List)} to populate the cache after a bulk query.
     */
    public QueryBuilder<V> query() {
        return repo.query();
    }

    /** Populate cache from an externally fetched list (e.g. after a query()). */
    public void cacheAll(List<V> entities) {
        entities.forEach(e -> cache.put(extractKey(e), e));
    }

    // ── CACHE DIRECT ACCESS ──────────────────────────────────────────────────

    public void invalidate(K id) {
        cache.invalidate(id);
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public CacheStats getCacheStats() {
        return cache.getStats();
    }

    public NotCache<K, V> getCache() {
        return cache;
    }

    // ── FLUSH (write-behind sync) ────────────────────────────────────────────

    /**
     * Flushes all dirty cache entries to the database.
     * Safe to call manually at any time (e.g. on plugin disable).
     */
    public void flush() {
        synchronized (flushLock) {
            Map<K, V> dirty = cache.getDirtyEntries();
            if (dirty.isEmpty()) return;

            LOGGER.info("[CachedRepository] Flushing " + dirty.size() + " dirty entries to DB...");
            try {
                repo.insertAll(new ArrayList<>(dirty.values()));
                // insertAll with upsert semantics – override if your DB supports it
                cache.markAllClean();
                LOGGER.info("[CachedRepository] Flush complete.");
            } catch (Exception e) {
                LOGGER.severe("[CachedRepository] Flush failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Flush using upsert semantics (recommended for WRITE_BEHIND).
     */
    public void flushWithUpsert() {
        synchronized (flushLock) {
            Map<K, V> dirty = cache.getDirtyEntries();
            if (dirty.isEmpty()) return;

            LOGGER.info("[CachedRepository] Upserting " + dirty.size() + " dirty entries...");
            for (V entity : dirty.values()) {
                repo.upsert(entity);
            }
            cache.markAllClean();
            LOGGER.info("[CachedRepository] Upsert flush complete.");
        }
    }

    public CompletableFuture<Void> flushAsync() {
        return CompletableFuture.runAsync(this::flushWithUpsert);
    }

    // ── CALLBACKS ────────────────────────────────────────────────────────────

    /**
     * Run an action for every cached entity (useful for bulk operations on
     * loaded data without hitting the DB).
     */
    public void forEachCached(Consumer<V> action) {
        cache.getDirtyEntries().values().forEach(action); // dirty entries are in-memory modified
    }

    // ── LIFECYCLE ────────────────────────────────────────────────────────────

    /**
     * Flush pending writes, stop the background scheduler, and shut down the cache.
     */
    public void close() {
        if (writeStrategy == WriteStrategy.WRITE_BEHIND) {
            flushWithUpsert();
        }
        flushScheduler.shutdownNow();
        cache.shutdown();
    }

    // ── INTERNAL HELPERS ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private K extractKey(V entity) {
        try {
            return (K) pkField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read primary key from " + entity.getClass().getName(), e);
        }
    }

    // ── BUILDER ──────────────────────────────────────────────────────────────

    public static <K, V> Builder<K, V> builder(Database database, Class<V> entityClass) {
        return new Builder<>(database, entityClass);
    }

    public static class Builder<K, V> {
        private final Database database;
        private final Class<V> entityClass;
        private WriteStrategy writeStrategy = WriteStrategy.WRITE_THROUGH;
        private long flushIntervalMillis = 30_000L; // 30 s default for WRITE_BEHIND
        private NotCache<K, V> cache;

        private Builder(Database database, Class<V> entityClass) {
            this.database = database;
            this.entityClass = entityClass;
            // Default cache – can be overridden with .cache(...)
            this.cache = NotCache.<K, V>builder()
                    .maxSize(500)
                    .ttlMinutes(10)
                    .evictionPolicy(CacheEvictionPolicy.LRU)
                    .build();
        }

        public Builder<K, V> writeStrategy(WriteStrategy strategy) {
            this.writeStrategy = strategy;
            return this;
        }

        public Builder<K, V> flushIntervalSeconds(long seconds) {
            this.flushIntervalMillis = seconds * 1000L;
            return this;
        }

        public Builder<K, V> flushIntervalMillis(long ms) {
            this.flushIntervalMillis = ms;
            return this;
        }

        public Builder<K, V> cache(NotCache<K, V> cache) {
            this.cache = cache;
            return this;
        }

        public CachedRepository<K, V> build() {
            return new CachedRepository<>(this);
        }
    }
}


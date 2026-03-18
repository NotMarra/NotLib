package dev.notmarra.notlib.database;

import dev.notmarra.notlib.cache.CacheEvictionPolicy;
import dev.notmarra.notlib.cache.CachedRepository;
import dev.notmarra.notlib.cache.NotCache;
import dev.notmarra.notlib.cache.WriteStrategy;
import dev.notmarra.notlib.database.repository.EntityRepository;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Single entry point that wires a {@link Database} together with
 * {@link CachedRepository} instances.
 *
 * <h3>Minimal setup (SQLite + sane defaults)</h3>
 * <pre>{@code
 * DatabaseManager db = DatabaseManager.sqlite(dataFolder, "data").build();
 * db.registerCached(PlayerProfile.class);
 *
 * CachedRepository<UUID, PlayerProfile> repo = db.cached(PlayerProfile.class);
 * repo.findById(uuid);
 * }</pre>
 *
 * <h3>Custom setup (MariaDB + fine-tuned cache)</h3>
 * <pre>{@code
 * DatabaseManager db = DatabaseManager.mariadb("host", "3306", "mydb", "user", "pass")
 *         .defaultWriteStrategy(WriteStrategy.WRITE_BEHIND)
 *         .defaultFlushIntervalSeconds(20)
 *         .defaultCacheMaxSize(2000)
 *         .defaultTtlMinutes(15)
 *         .build();
 *
 * // Override per entity if needed
 * db.registerCached(PlayerProfile.class, WriteStrategy.WRITE_THROUGH,
 *         NotCache.<UUID, PlayerProfile>builder().maxSize(100).ttlMinutes(5).build());
 *
 * // Or use plain EntityRepository without cache
 * db.registerPlain(SomeLogEntry.class);
 * EntityRepository<SomeLogEntry> logs = db.plain(SomeLogEntry.class);
 * }</pre>
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    // ── CORE ─────────────────────────────────────────────────────────────────

    private final Database database;

    // ── REPO REGISTRIES ──────────────────────────────────────────────────────

    private final Map<Class<?>, CachedRepository<?, ?>> cachedRepos = new ConcurrentHashMap<>();
    private final Map<Class<?>, EntityRepository<?>>    plainRepos  = new ConcurrentHashMap<>();

    // ── DEFAULT CACHE CONFIG ─────────────────────────────────────────────────

    private final WriteStrategy defaultWriteStrategy;
    private final long defaultFlushIntervalMillis;
    private final int defaultCacheMaxSize;
    private final long defaultTtlMillis;
    private final CacheEvictionPolicy defaultEvictionPolicy;

    // ── CONSTRUCTOR ──────────────────────────────────────────────────────────

    private DatabaseManager(Builder b) {
        this.database                 = b.database;
        this.defaultWriteStrategy     = b.defaultWriteStrategy;
        this.defaultFlushIntervalMillis = b.defaultFlushIntervalMillis;
        this.defaultCacheMaxSize      = b.defaultCacheMaxSize;
        this.defaultTtlMillis         = b.defaultTtlMillis;
        this.defaultEvictionPolicy    = b.defaultEvictionPolicy;
    }

    // ── REGISTER ─────────────────────────────────────────────────────────────

    /**
     * Register an entity class with a {@link CachedRepository} using the
     * manager's default cache configuration.
     */
    public <K, V> CachedRepository<K, V> registerCached(Class<V> entityClass) {
        return registerCached(entityClass, defaultWriteStrategy, buildDefaultCache());
    }

    /**
     * Register with a custom {@link WriteStrategy} but default cache settings.
     */
    public <K, V> CachedRepository<K, V> registerCached(Class<V> entityClass, WriteStrategy strategy) {
        return registerCached(entityClass, strategy, buildDefaultCache());
    }

    /**
     * Register with full control over both strategy and the cache instance.
     */
    @SuppressWarnings("unchecked")
    public <K, V> CachedRepository<K, V> registerCached(
            Class<V> entityClass,
            WriteStrategy strategy,
            NotCache<K, V> cache
    ) {
        CachedRepository<K, V> repo = CachedRepository.<K, V>builder(database, entityClass)
                .writeStrategy(strategy)
                .flushIntervalMillis(defaultFlushIntervalMillis)
                .cache(cache)
                .build();
        repo.createTable();
        cachedRepos.put(entityClass, repo);
        LOGGER.info("[DatabaseManager] Registered cached repo: " + entityClass.getSimpleName()
                + " (" + strategy + ")");
        return repo;
    }

    /**
     * Register an entity class with a plain {@link EntityRepository} (no cache).
     * Use this for log tables, audit trails, or any write-heavy entity where
     * caching adds no value.
     */
    public <V> EntityRepository<V> registerPlain(Class<V> entityClass) {
        EntityRepository<V> repo = new EntityRepository<>(database, entityClass);
        repo.createTable();
        plainRepos.put(entityClass, repo);
        LOGGER.info("[DatabaseManager] Registered plain repo: " + entityClass.getSimpleName());
        return repo;
    }

    // ── RETRIEVE ─────────────────────────────────────────────────────────────

    /**
     * Get the {@link CachedRepository} for a registered entity class.
     *
     * @throws IllegalStateException if not registered via {@link #registerCached}
     */
    @SuppressWarnings("unchecked")
    public <K, V> CachedRepository<K, V> cached(Class<V> entityClass) {
        CachedRepository<?, ?> repo = cachedRepos.get(entityClass);
        if (repo == null) throw new IllegalStateException(
                entityClass.getSimpleName() + " is not registered. Call registerCached() first.");
        return (CachedRepository<K, V>) repo;
    }

    /**
     * Get the plain {@link EntityRepository} for a registered entity class.
     *
     * @throws IllegalStateException if not registered via {@link #registerPlain}
     */
    @SuppressWarnings("unchecked")
    public <V> EntityRepository<V> plain(Class<V> entityClass) {
        EntityRepository<?> repo = plainRepos.get(entityClass);
        if (repo == null) throw new IllegalStateException(
                entityClass.getSimpleName() + " is not registered. Call registerPlain() first.");
        return (EntityRepository<V>) repo;
    }

    // ── DIRECT DB ACCESS ─────────────────────────────────────────────────────

    /** Direct access to the underlying {@link Database} for custom queries. */
    public Database getDatabase() { return database; }

    // ── LIFECYCLE ────────────────────────────────────────────────────────────

    /**
     * Flush all WRITE_BEHIND repos and close everything cleanly.
     * Call this in your plugin's {@code onDisable()}.
     */
    public void close() {
        LOGGER.info("[DatabaseManager] Shutting down...");
        cachedRepos.values().forEach(CachedRepository::close);
        database.close();
        LOGGER.info("[DatabaseManager] Closed.");
    }

    // ── INTERNAL ─────────────────────────────────────────────────────────────

    private <K, V> NotCache<K, V> buildDefaultCache() {
        return NotCache.<K, V>builder()
                .maxSize(defaultCacheMaxSize)
                .ttlMillis(defaultTtlMillis)
                .evictionPolicy(defaultEvictionPolicy)
                .build();
    }

    // ── STATIC FACTORIES ─────────────────────────────────────────────────────

    public static Builder sqlite(File dataFolder, String fileName) {
        return new Builder(new dev.notmarra.notlib.database.type.SQLite()
                .setup(Database.generateProperties(dataFolder, fileName)));
    }

    public static Builder mariadb(String host, String port, String dbName, String user, String password) {
        return new Builder(new dev.notmarra.notlib.database.type.MariaDB()
                .setup(Database.generateProperties(user, password, dbName, port, host)));
    }

    /** Bring your own pre-configured {@link Database} instance. */
    public static Builder of(Database database) {
        return new Builder(database);
    }

    // ── BUILDER ──────────────────────────────────────────────────────────────

    public static class Builder {
        private final Database database;
        private WriteStrategy defaultWriteStrategy       = WriteStrategy.WRITE_THROUGH;
        private long defaultFlushIntervalMillis          = 30_000L;
        private int defaultCacheMaxSize                  = 500;
        private long defaultTtlMillis                   = 10 * 60 * 1000L; // 10 min
        private CacheEvictionPolicy defaultEvictionPolicy = CacheEvictionPolicy.LRU;

        private Builder(Database database) {
            this.database = database;
        }

        public Builder defaultWriteStrategy(WriteStrategy strategy) {
            this.defaultWriteStrategy = strategy; return this;
        }

        public Builder defaultFlushIntervalSeconds(long seconds) {
            this.defaultFlushIntervalMillis = seconds * 1000L; return this;
        }

        public Builder defaultCacheMaxSize(int size) {
            this.defaultCacheMaxSize = size; return this;
        }

        public Builder defaultTtlMinutes(long minutes) {
            this.defaultTtlMillis = minutes * 60 * 1000L; return this;
        }

        public Builder defaultTtlMillis(long millis) {
            this.defaultTtlMillis = millis; return this;
        }

        public Builder defaultEvictionPolicy(CacheEvictionPolicy policy) {
            this.defaultEvictionPolicy = policy; return this;
        }

        public DatabaseManager build() {
            return new DatabaseManager(this);
        }
    }
}
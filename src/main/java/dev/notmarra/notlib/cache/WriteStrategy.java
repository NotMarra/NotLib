package dev.notmarra.notlib.cache;

/**
 * Defines when dirty cache entries are persisted to the database.
 */
public enum WriteStrategy {

    /**
     * Write-Through: every mutation is persisted to the database immediately
     * AND updated in the cache. Guarantees consistency, higher DB load.
     */
    WRITE_THROUGH,

    /**
     * Write-Behind (Write-Back): mutations update the cache immediately and
     * are batched to the database periodically. Lower DB load, slight risk of
     * data loss on crash. Use {@code CachedRepository#flush()} to force sync.
     */
    WRITE_BEHIND,

    /**
     * Read-Through only: the cache is populated on reads, but writes always
     * go directly to the database (and invalidate the cache entry).
     * Simplest strategy – good when write patterns are infrequent.
     */
    READ_THROUGH
}

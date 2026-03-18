package dev.notmarra.notlib.cache;

import java.time.Instant;

/**
 * Represents a single cached value with TTL and dirty-tracking support.
 *
 * @param <V> the type of the cached value
 */
public class CacheEntry<V> {
    private V value;
    private final Instant createdAt;
    private Instant lastAccessed;
    private final long ttlMillis;
    private boolean dirty;

    public CacheEntry(V value, long ttlMillis) {
        this.value = value;
        this.createdAt = Instant.now();
        this.lastAccessed = this.createdAt;
        this.ttlMillis = ttlMillis;
        this.dirty = false;
    }

    // ── EXPIRY ───────────────────────────────────────────────────────────────

    public boolean isExpired() {
        if (ttlMillis <= 0) return false; // TTL <= 0 means immortal
        return Instant.now().isAfter(createdAt.plusMillis(ttlMillis));
    }

    public long remainingTtlMillis() {
        if (ttlMillis <= 0) return Long.MAX_VALUE;
        long elapsed = Instant.now().toEpochMilli() - createdAt.toEpochMilli();
        return Math.max(0, ttlMillis - elapsed);
    }

    // ── ACCESS ───────────────────────────────────────────────────────────────

    public V getValue() {
        this.lastAccessed = Instant.now();
        return value;
    }

    public void setValue(V value) {
        this.value = value;
        this.dirty = true;
    }

    // ── DIRTY FLAG ───────────────────────────────────────────────────────────

    public boolean isDirty() { return dirty; }
    public void markClean() { this.dirty = false; }
    public void markDirty() { this.dirty = true; }

    // ── META ─────────────────────────────────────────────────────────────────

    public Instant getCreatedAt()    { return createdAt; }
    public Instant getLastAccessed() { return lastAccessed; }
    public long getTtlMillis()       { return ttlMillis; }

    @Override
    public String toString() {
        return "CacheEntry{dirty=" + dirty
                + ", expired=" + isExpired()
                + ", remainingTtl=" + remainingTtlMillis() + "ms"
                + ", value=" + value + "}";
    }
}


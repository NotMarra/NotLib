package dev.notmarra.notlib.cache;

/**
 * Defines how the cache evicts entries when it reaches capacity.
 */
public enum CacheEvictionPolicy {

    /**
     * Least Recently Used – evicts the entry that was accessed the longest time ago.
     */
    LRU,

    /**
     * First In First Out – evicts the oldest inserted entry regardless of access pattern.
     */
    FIFO,

    /**
     * Time To Live only – no size limit; entries are only evicted when they expire.
     * Use this when you control entry count from the outside.
     */
    TTL_ONLY
}

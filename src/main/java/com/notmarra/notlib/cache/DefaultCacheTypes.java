package com.notmarra.notlib.cache;

public enum DefaultCacheTypes implements ICacheType {
    PLAYER("player");
    
    private final String cacheKey;
    
    DefaultCacheTypes(String cacheKey) {
        this.cacheKey = cacheKey;
    }
    
    @Override
    public String getCacheKey() {
        return cacheKey;
    }
}

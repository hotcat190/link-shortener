package com.example.linkshortener.sharding;

/**
 * Thread-local context holder for sharding key.
 * Stores the shortenedUrl used to determine which shard to route to.
 */
public class ShardingContext {

    private static final ThreadLocal<String> CURRENT_SHARDING_KEY = new ThreadLocal<>();

    public static void setShardingKey(String shortenedUrl) {
        CURRENT_SHARDING_KEY.set(shortenedUrl);
    }

    public static String getShardingKey() {
        return CURRENT_SHARDING_KEY.get();
    }

    public static void clear() {
        CURRENT_SHARDING_KEY.remove();
    }

    /**
     * Determines the target shard based on the sharding key.
     * Uses hash-based routing: hashCode % 2
     * 
     * @return ClientDatabase enum indicating target shard
     */
    public static ClientDatabase determineTargetShard() {
        String key = getShardingKey();
        if (key == null || key.isEmpty()) {
            // Default to SHARD_1 if no key is set
            return ClientDatabase.SHARD_1;
        }
        
        int shardIndex = Math.abs(key.hashCode()) % 2;
        return shardIndex == 0 ? ClientDatabase.SHARD_1 : ClientDatabase.SHARD_2;
    }
}

package com.example.linkshortener.sharding;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests to verify hash-based sharding logic.
 * Demonstrates that different keys route to different shards predictably.
 */
class ShardingContextTest {

    @AfterEach
    void cleanup() {
        ShardingContext.clear();
    }

    @Test
    @DisplayName("Verify sharding distribution for sample keys")
    void testShardingDistribution() {
        // Test various keys and their shard assignments
        String[] testKeys = {"A", "B", "C", "D", "abc123", "xyz789", "short1", "short2"};
        
        System.out.println("=== Sharding Distribution Test ===");
        for (String key : testKeys) {
            ShardingContext.setShardingKey(key);
            ClientDatabase shard = ShardingContext.determineTargetShard();
            int hashMod = Math.abs(key.hashCode()) % 2;
            
            System.out.printf("Key: %-10s | HashCode: %11d | Mod 2: %d | Shard: %s%n",
                    key, key.hashCode(), hashMod, shard);
            
            // Verify the logic is consistent
            ClientDatabase expected = hashMod == 0 ? ClientDatabase.SHARD_1 : ClientDatabase.SHARD_2;
            assertEquals(expected, shard, "Shard mismatch for key: " + key);
            
            ShardingContext.clear();
        }
    }

    @Test
    @DisplayName("Key 'A' should route to specific shard based on hashCode")
    void testKeyA() {
        String key = "A";
        ShardingContext.setShardingKey(key);
        
        int hashMod = Math.abs(key.hashCode()) % 2;
        ClientDatabase expected = hashMod == 0 ? ClientDatabase.SHARD_1 : ClientDatabase.SHARD_2;
        
        assertEquals(expected, ShardingContext.determineTargetShard());
        System.out.println("Key 'A' (hashCode=" + key.hashCode() + ") -> " + expected);
    }

    @Test
    @DisplayName("Key 'B' should route to specific shard based on hashCode")
    void testKeyB() {
        String key = "B";
        ShardingContext.setShardingKey(key);
        
        int hashMod = Math.abs(key.hashCode()) % 2;
        ClientDatabase expected = hashMod == 0 ? ClientDatabase.SHARD_1 : ClientDatabase.SHARD_2;
        
        assertEquals(expected, ShardingContext.determineTargetShard());
        System.out.println("Key 'B' (hashCode=" + key.hashCode() + ") -> " + expected);
    }

    @Test
    @DisplayName("Null or empty key should default to SHARD_1")
    void testNullKey() {
        // No key set
        assertEquals(ClientDatabase.SHARD_1, ShardingContext.determineTargetShard());
        
        // Empty key
        ShardingContext.setShardingKey("");
        assertEquals(ClientDatabase.SHARD_1, ShardingContext.determineTargetShard());
    }

    @Test
    @DisplayName("Context should be cleared after clear() call")
    void testContextClear() {
        ShardingContext.setShardingKey("testKey");
        assertNotNull(ShardingContext.getShardingKey());
        
        ShardingContext.clear();
        assertNull(ShardingContext.getShardingKey());
    }

    @Test
    @DisplayName("Same key should always route to same shard (consistency)")
    void testConsistency() {
        String key = "consistent-key-123";
        
        for (int i = 0; i < 100; i++) {
            ShardingContext.setShardingKey(key);
            ClientDatabase shard = ShardingContext.determineTargetShard();
            
            int expectedMod = Math.abs(key.hashCode()) % 2;
            ClientDatabase expected = expectedMod == 0 ? ClientDatabase.SHARD_1 : ClientDatabase.SHARD_2;
            
            assertEquals(expected, shard, "Inconsistent routing on iteration " + i);
            ShardingContext.clear();
        }
    }

    /**
     * Main method for quick manual testing without JUnit.
     */
    public static void main(String[] args) {
        System.out.println("=== Manual Sharding Verification ===\n");
        
        String[] testKeys = {"A", "B", "test1", "test2", "abc", "xyz"};
        
        for (String key : testKeys) {
            ShardingContext.setShardingKey(key);
            ClientDatabase shard = ShardingContext.determineTargetShard();
            
            System.out.printf("Key: %-8s | HashCode: %11d | Mod2: %d | -> %s%n",
                    key,
                    key.hashCode(),
                    Math.abs(key.hashCode()) % 2,
                    shard);
            
            ShardingContext.clear();
        }
        
        // Specific verification for "A" and "B"
        System.out.println("\n=== Specific Verification ===");
        System.out.println("'A'.hashCode() = " + "A".hashCode() + " -> mod 2 = " + (Math.abs("A".hashCode()) % 2));
        System.out.println("'B'.hashCode() = " + "B".hashCode() + " -> mod 2 = " + (Math.abs("B".hashCode()) % 2));
    }
}

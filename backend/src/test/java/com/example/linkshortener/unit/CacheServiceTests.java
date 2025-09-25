package com.example.linkshortener.unit;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.linkshortener.data.entity.Data;
import com.example.linkshortener.data.repository.DataRepository;
import com.example.linkshortener.service.CacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@ExtendWith(MockitoExtension.class)
class CacheServiceTest {

    // Mock the main dependencies
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private DataRepository dataRepository;

    // Mock the specific Redis operations that the service uses
    @Mock
    private HashOperations<String, Object, Object> hashOperations;
    @Mock
    private ValueOperations<String, String> valueOperations;

    // The class we are testing
    private CacheService cacheService;

    @BeforeEach
    void setup() {
        // STEP 1: Configure the mocks' behavior FIRST.
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        
        // STEP 2: NOW, manually create the service instance using the configured mocks.
        cacheService = new CacheService(redisTemplate);

        // STEP 3: Manually inject the other mocked dependency (@Autowired field).
        ReflectionTestUtils.setField(cacheService, "dataRepository", dataRepository);
    }

    @Test
    void saveToCache_withTtl_shouldSetExpiration() {
        // Arrange
        String shortenedUrl = "abcde";
        String originalUrl = "https://example.com";
        Long ttl = 60L;
        String key = "url:" + shortenedUrl;

        // Act
        cacheService.saveToCache(shortenedUrl, originalUrl, ttl);

        // Assert
        // Verify that the URL was put into the hash
        verify(hashOperations).put(key, "url", originalUrl);
        // Verify that an expiration was set on the key
        verify(redisTemplate).expire(key, ttl, TimeUnit.SECONDS);
        // Verify that the key was NOT set to persist indefinitely
        verify(redisTemplate, never()).persist(key);
    }

    @Test
    void saveToCache_withoutTtl_shouldPersistKey() {
        // Arrange
        String shortenedUrl = "abcde";
        String originalUrl = "https://example.com";
        String key = "url:" + shortenedUrl;

        // Act
        cacheService.saveToCache(shortenedUrl, originalUrl, null);

        // Assert
        verify(hashOperations).put(key, "url", originalUrl);
        // Verify that the key was set to persist (no expiration)
        verify(redisTemplate).persist(key);
        // Verify that an expiration was NOT set
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    void getOriginalUrlFromCache_whenKeyExists_shouldReturnUrl() {
        // Arrange
        String shortenedUrl = "fghij";
        String expectedUrl = "https://google.com";
        String key = "url:" + shortenedUrl;
        when(hashOperations.hasKey(key, "url")).thenReturn(true);
        when(hashOperations.get(key, "url")).thenReturn(expectedUrl);

        // Act
        String actualUrl = cacheService.getOriginalUrlFromCache(shortenedUrl);

        // Assert
        assertEquals(expectedUrl, actualUrl);
    }

    @Test
    void getOriginalUrlFromCache_whenKeyDoesNotExist_shouldReturnNull() {
        // Arrange
        String shortenedUrl = "klmno";
        String key = "url:" + shortenedUrl;
        when(hashOperations.hasKey(key, "url")).thenReturn(false);

        // Act
        String actualUrl = cacheService.getOriginalUrlFromCache(shortenedUrl);

        // Assert
        assertNull(actualUrl);
        // Verify that 'get' was never called since the key doesn't exist
        verify(hashOperations, never()).get(key, "url");
    }

    @Test
    void deleteFromCache_shouldCallRedisDelete() {
        // Arrange
        String shortenedUrl = "pqrst";
        String key = "url:" + shortenedUrl;

        // Act
        cacheService.deleteFromCache(shortenedUrl);

        // Assert
        verify(redisTemplate).delete(key);
    }

    @Test
    void incrementClickCount_shouldCallIncrementAndExpire() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Arrange
        String shortenedUrl = "uvwxy";
        String key = "clicks:" + shortenedUrl;

        // Act
        cacheService.incrementClickCount(shortenedUrl);

        // Assert
        verify(valueOperations).increment(key);
        verify(redisTemplate).expire(key, 1, TimeUnit.HOURS);
    }

    @Test
    void syncClickCountsToDatabase_whenDataExists_shouldUpdateAndClearRedisKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Arrange
        String shortenedUrl = "xyz123";
        String redisKey = "clicks:" + shortenedUrl;
        int initialDbClickCount = 100;
        String redisClickValue = "20"; // Redis value is a string
        long expectedIncrement = Long.parseLong(redisClickValue) / 2; // 10

        // Create a mock data object
        Data mockData = new Data();
        mockData.setShortenedUrl(shortenedUrl);
        mockData.setClickCount(initialDbClickCount);

        // Mock Redis responses
        when(redisTemplate.keys("clicks:*")).thenReturn(Set.of(redisKey));
        when(valueOperations.get(redisKey)).thenReturn(redisClickValue);

        // Mock repository response
        when(dataRepository.findByShortenedUrl(shortenedUrl)).thenReturn(Optional.of(mockData));

        // Act
        cacheService.syncClickCountsToDatabase();

        // Assert
        // Verify the repository was searched
        verify(dataRepository).findByShortenedUrl(shortenedUrl);
        // Verify the data was saved
        verify(dataRepository).save(mockData);
        // Verify the click count was updated correctly
        assertEquals(initialDbClickCount + expectedIncrement, mockData.getClickCount());
        // Verify the key was deleted from Redis after processing
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    void syncClickCountsToDatabase_whenDataDoesNotExistInDb_shouldStillClearRedisKey() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Arrange
        String shortenedUrl = "abc456";
        String redisKey = "clicks:" + shortenedUrl;
        String redisClickValue = "10";

        when(redisTemplate.keys("clicks:*")).thenReturn(Set.of(redisKey));
        when(valueOperations.get(redisKey)).thenReturn(redisClickValue);
        // Mock the repository to find nothing
        when(dataRepository.findByShortenedUrl(shortenedUrl)).thenReturn(Optional.empty());

        // Act
        cacheService.syncClickCountsToDatabase();

        // Assert
        // Verify we never tried to save anything to the database
        verify(dataRepository, never()).save(any(Data.class));
        // Verify the key was still deleted from Redis to prevent reprocessing
        verify(redisTemplate).delete(redisKey);
    }

    @Test
    void syncClickCountsToDatabase_whenNoClickKeys_shouldDoNothing() {
        // Arrange
        // Mock Redis to return an empty set of keys
        when(redisTemplate.keys("clicks:*")).thenReturn(Set.of());

        // Act
        cacheService.syncClickCountsToDatabase();

        // Assert
        // Verify that no interactions happened with the repository or value operations
        verify(dataRepository, never()).findByShortenedUrl(anyString());
        verify(dataRepository, never()).save(any(Data.class));
        verify(valueOperations, never()).get(anyString());
    }

    @Test
    void syncClickCountsToDatabase_whenValueIsNotNumeric_shouldNotCrashAndStillDeleteKey() {
        // Arrange: Set up a scenario with one good key and one bad key.
        String badShortenedUrl = "bad-data";
        String goodShortenedUrl = "good-data";
        String badRedisKey = "clicks:" + badShortenedUrl;
        String goodRedisKey = "clicks:" + goodShortenedUrl;

        // Mock Redis to return a key with a non-numeric value and a key with a valid one.
        when(redisTemplate.keys("clicks:*")).thenReturn(Set.of(badRedisKey, goodRedisKey));
        when(valueOperations.get(badRedisKey)).thenReturn("not-a-number"); // This will cause the exception
        when(valueOperations.get(goodRedisKey)).thenReturn("10");

        // Mock repository for the good data
        Data mockData = new Data();
        mockData.setShortenedUrl(goodShortenedUrl);
        mockData.setClickCount(100);
        when(dataRepository.findByShortenedUrl(goodShortenedUrl)).thenReturn(Optional.of(mockData));

        // Act & Assert:
        // We assert that calling the method does NOT throw an unhandled exception.
        // In the current buggy code, this test would fail because a NumberFormatException would escape.
        assertDoesNotThrow(() -> {
            cacheService.syncClickCountsToDatabase();
        });

        // Further Assertions:
        // 1. Verify that we still processed the GOOD key successfully.
        verify(dataRepository).save(any(Data.class));

        // 2. Verify that we NEVER tried to find or save the BAD data in the repository.
        verify(dataRepository, never()).findByShortenedUrl(badShortenedUrl);

        // 3. MOST IMPORTANT: Verify that the service cleaned up BOTH keys from Redis
        // to prevent future failures.
        verify(redisTemplate).delete(badRedisKey);
        verify(redisTemplate).delete(goodRedisKey);
    }
}
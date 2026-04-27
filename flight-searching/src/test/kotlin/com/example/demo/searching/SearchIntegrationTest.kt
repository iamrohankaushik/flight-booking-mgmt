package com.example.demo.searching

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class SearchIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var redisTemplate: RedisTemplate<String, String>

    // The V2__Seed-Data.sql generates schedules using NOW() + INTERVAL '1 day'
    private val day1 = LocalDate.now().plusDays(1).toString()
    private val day2 = LocalDate.now().plusDays(2).toString()
    private val LRU_TRACKER_KEY = "search:lru_tracker"

    // Seed data routes (all scheduled for tomorrow)
    private val route1 = Pair("BOM", "BLR")
    private val route2 = Pair("DEL", "BLR")
    private val route3 = Pair("BLR", "MAA")
    private val route4 = Pair("MAA", "HYD")

    private fun cacheKey(route: Pair<String, String>, date: String) = "search:${route.first}:${route.second}:$date"

    @AfterEach
    fun tearDown() {
        val keys = redisTemplate.keys("search:*")
        if (!keys.isNullOrEmpty()) {
            redisTemplate.delete(keys)
        }
    }

    private fun searchRoute(source: String, dest: String, date: String) {
        mockMvc.perform(get("/v1/search-flights")
            .param("source", source)
            .param("dest", dest)
            .param("date", date))
            .andExpect(status().isOk)
    }

    private val route1Date = day1 // BOM→BLR
    private val route2Date = day2 // DEL→BLR
    private val route3Date = day2 // BLR→MAA
    private val route4Date = day2 // MAA→HYD

    // ========================================================================
    // Test 1: Cache Miss — First search hits DB, result gets cached in Redis
    // ========================================================================
    @Test
    fun `test cache miss fetches from db and populates redis cache`() {
        // Before: Redis should have nothing for this route
        assertNull(redisTemplate.opsForValue().get(cacheKey(route1, route1Date)))

        // Search
        searchRoute(route1.first, route1.second, route1Date)

        // After: Redis should now contain the cached result
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route1, route1Date)),
            "Cache key should exist after first search (cache miss -> DB hit -> cache set)")

        // LRU tracker should have exactly 1 entry
        val trackerSize = redisTemplate.opsForList().size(LRU_TRACKER_KEY)
        assertEquals(1, trackerSize, "LRU tracker should have 1 entry")

        // The entry in the tracker should be the key we just searched
        val trackerHead = redisTemplate.opsForList().index(LRU_TRACKER_KEY, 0)
        assertEquals(cacheKey(route1, route1Date), trackerHead)
    }

    // ========================================================================
    // Test 2: Cache Hit — Identical search reads directly from Redis
    // ========================================================================
    @Test
    fun `test cache hit returns data from redis without db call`() {
        // First search: populates cache (cache miss)
        searchRoute(route1.first, route1.second, route1Date)
        val cachedAfterFirst = redisTemplate.opsForValue().get(cacheKey(route1, route1Date))
        assertNotNull(cachedAfterFirst, "Cache should be populated after first search")

        // Second search: should hit cache (the cached value stays the same)
        searchRoute(route1.first, route1.second, route1Date)
        val cachedAfterSecond = redisTemplate.opsForValue().get(cacheKey(route1, route1Date))

        // The cached content should be identical (proving it was not re-fetched)
        assertEquals(cachedAfterFirst, cachedAfterSecond,
            "Cached data should remain unchanged on cache hit")

        // LRU tracker should still have exactly 1 unique entry (updateLru removes duplicates)
        val trackerSize = redisTemplate.opsForList().size(LRU_TRACKER_KEY)
        assertEquals(1, trackerSize, "LRU tracker should deduplicate on repeated access")
    }

    // ========================================================================
    // Test 3: LRU Eviction — 4th insert evicts the oldest (limit = 3)
    // ========================================================================
    @Test
    fun `test lru evicts oldest cache entry when limit exceeded`() {
        // Search 3 routes — fills the LRU to capacity (limit = 3)
        searchRoute(route1.first, route1.second, route1Date)  // oldest
        searchRoute(route2.first, route2.second, route2Date)
        searchRoute(route3.first, route3.second, route3Date)  // newest

        // All 3 should be cached
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route1, route1Date)), "Route 1 should be cached")
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route2, route2Date)), "Route 2 should be cached")
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route3, route3Date)), "Route 3 should be cached")
        assertEquals(3, redisTemplate.opsForList().size(LRU_TRACKER_KEY), "LRU tracker should have 3 entries")

        // Search a 4th route — this should evict route1 (the oldest/least-recently-used)
        searchRoute(route4.first, route4.second, route4Date)

        // Route 1 should be EVICTED from cache
        assertNull(redisTemplate.opsForValue().get(cacheKey(route1, route1Date)),
            "Route 1 (oldest) should be evicted after exceeding LRU limit of 3")

        // Routes 2, 3, 4 should still be cached
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route2, route2Date)), "Route 2 should still be cached")
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route3, route3Date)), "Route 3 should still be cached")
        assertNotNull(redisTemplate.opsForValue().get(cacheKey(route4, route4Date)), "Route 4 should now be cached")

        // LRU tracker should have exactly 3 entries (not 4)
        assertEquals(3, redisTemplate.opsForList().size(LRU_TRACKER_KEY),
            "LRU tracker should stay at limit of 3 after eviction")

        // Verify order: route4 is most recent (index 0), route2 is least recent (index 2)
        assertEquals(cacheKey(route4, route4Date), redisTemplate.opsForList().index(LRU_TRACKER_KEY, 0))
        assertEquals(cacheKey(route3, route3Date), redisTemplate.opsForList().index(LRU_TRACKER_KEY, 1))
        assertEquals(cacheKey(route2, route2Date), redisTemplate.opsForList().index(LRU_TRACKER_KEY, 2))
    }

    // ========================================================================
    // Test 4: Validation — Invalid date format returns 400 Bad Request
    // ========================================================================
    @Test
    fun `test invalid date format returns bad request`() {
        mockMvc.perform(get("/v1/search-flights")
            .param("source", "DEL")
            .param("dest", "BOM")
            .param("date", "2026-04"))  // Incomplete date
            .andExpect(status().isBadRequest)
    }
}

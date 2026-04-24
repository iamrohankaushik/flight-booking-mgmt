package com.example.demo.searching.service.impl

import com.example.demo.searching.dto.FlightScheduleDto
import com.example.demo.searching.dto.FlightSearchRequest
import com.example.demo.searching.exception.InvalidSearchRequestException
import com.example.demo.searching.exception.SearchServiceException
import com.example.demo.searching.repository.SearchRepository
import com.example.demo.searching.service.SearchService
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SearchServiceImpl(
    private val searchRepository: SearchRepository,
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : SearchService {

    private val logger = LoggerFactory.getLogger(SearchServiceImpl::class.java)
    private val LRU_TRACKER_KEY = "search:lru_tracker"
    private val CACHE_LIMIT = 3L

    override fun searchFlights(request: FlightSearchRequest): List<FlightScheduleDto> {
        val cacheKey = "search:${request.source}:${request.dest}:${request.date}"
        
        // 1. Check Cache
        try {
            val cachedData = redisTemplate.opsForValue().get(cacheKey)
            if (cachedData != null) {
                logger.info("Cache HIT for key: $cacheKey")
                updateLru(cacheKey)
                return objectMapper.readValue(cachedData, object : TypeReference<List<FlightScheduleDto>>() {})
            }
        } catch (e: Exception) {
            logger.warn("Redis read failed for key $cacheKey: ${e.message}. Falling back to database.", e)
        }

        // 2. Cache Miss - Fetch from Database
        logger.info("Cache MISS for key: $cacheKey. Querying database...")
        
        val parsedDate = try {
            LocalDate.parse(request.date)
        } catch (e: Exception) {
            throw InvalidSearchRequestException("Invalid date format. Expected YYYY-MM-DD.")
        }
        
        val results = try {
            searchRepository.searchSchedules(request.source, request.dest, parsedDate)
        } catch (e: Exception) {
            logger.error("Database query failed for search: ${e.message}", e)
            throw SearchServiceException("Failed to retrieve flight schedules.")
        }

        // 3. Store in Cache and Manage LRU Size
        if (results.isNotEmpty()) {
            try {
                val jsonResult = objectMapper.writeValueAsString(results)
                redisTemplate.opsForValue().set(cacheKey, jsonResult)
                updateLru(cacheKey)
                enforceCacheLimit()
            } catch (e: Exception) {
                logger.warn("Redis write failed for key $cacheKey: ${e.message}. Ignoring cache update.", e)
            }
        } else {
            logger.info("No flights found for key: $cacheKey. Skipping cache update.")
        }

        return results
    }

    /**
     * Moves the key to the front of the Recently Used list.
     */
    private fun updateLru(cacheKey: String) {
        // Remove existing instances of the key to prevent duplicates
        redisTemplate.opsForList().remove(LRU_TRACKER_KEY, 0, cacheKey)
        // Push the key to the front (Left = Most Recent)
        redisTemplate.opsForList().leftPush(LRU_TRACKER_KEY, cacheKey)
        logger.info("LRU updated for key: $cacheKey")
    }

    /**
     * Checks if the LRU list exceeds the limit. If so, evicts the oldest entry.
     */
    private fun enforceCacheLimit() {
        val size = redisTemplate.opsForList().size(LRU_TRACKER_KEY) ?: 0
        if (size > CACHE_LIMIT) {
            // Pop the oldest key from the back (Right = Least Recent)
            val oldestKey = redisTemplate.opsForList().rightPop(LRU_TRACKER_KEY)
            if (oldestKey != null) {
                logger.info("LRU limit ($CACHE_LIMIT) exceeded. Evicting oldest key: $oldestKey")
                redisTemplate.delete(oldestKey)
                logger.info("LRU updated for key: $oldestKey")
            }
        }
    }
}

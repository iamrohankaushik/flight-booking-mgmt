package com.example.demo.common.service.impl

import com.example.demo.common.exception.SeatUnavailableException
import com.example.demo.common.service.RedisLockService
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class RedisLockServiceImpl(
    private val redisTemplate: StringRedisTemplate
) : RedisLockService {

    override suspend fun acquireLocks(keys: List<String>, duration: Duration): List<String> {
        val acquiredLocks = mutableListOf<String>()
        try {
            keys.forEach { key ->
                val success = redisTemplate.opsForValue().setIfAbsent(key, "locked", duration)
                if (success != true) {
                    throw SeatUnavailableException("Key $key is already locked")
                }
                acquiredLocks.add(key)
            }
            return acquiredLocks
        } catch (e: Exception) {
            releaseLocks(acquiredLocks)
            throw e
        }
    }

    override suspend fun releaseLocks(keys: List<String>) {
        keys.forEach { redisTemplate.delete(it) }
    }
}

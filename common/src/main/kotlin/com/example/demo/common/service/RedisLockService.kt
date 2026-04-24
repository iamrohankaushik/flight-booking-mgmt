package com.example.demo.common.service

import java.time.Duration

interface RedisLockService {
    suspend fun acquireLocks(keys: List<String>, duration: Duration): List<String>
    suspend fun releaseLocks(keys: List<String>)
}

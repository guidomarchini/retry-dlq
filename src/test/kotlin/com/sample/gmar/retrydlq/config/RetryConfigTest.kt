package com.sample.gmar.retrydlq.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RetryConfigTest {

    @Test
    fun `should create RetryConfig with default values`() {
        val config = RetryConfig()
        assertEquals(3, config.maxAttempts)
        assertEquals(60000L, config.retryDelayMillis)
        assertTrue(config.dlqEnabled)
    }

    @Test
    fun `should throw exception for non-positive maxAttempts`() {
        assertThrows<IllegalArgumentException> { RetryConfig(maxAttempts = 0) }
    }

    @Test
    fun `should throw exception for non-positive retryDelayMillis`() {
        assertThrows<IllegalArgumentException> { RetryConfig(retryDelayMillis = 0) }
    }

}
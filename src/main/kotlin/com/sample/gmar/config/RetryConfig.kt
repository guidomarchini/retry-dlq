package com.sample.gmar.config

data class RetryConfig(
    val maxAttempts: Int = 3,
    val retryDelayMillis: Long = 60 * 1000,
    val dlqEnabled: Boolean = true
) {
    init {
        require(maxAttempts > 0) { "Max retries must be > 0" }
        require(retryDelayMillis > 0) { "Retry delay must be > 0" }
    }
}

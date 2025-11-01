package com.sample.gmar.retrydlq.service

import com.sample.gmar.retrydlq.config.RetryConfig
import com.sample.gmar.retrydlq.persistence.DeadLetterRepository

class DummyRetryableService(
    maxAttempts: Int = 1,
    dlqEnabled: Boolean = true,
    deadLetterRepository: DeadLetterRepository
) : RetryableService<String>(
    RetryConfig(maxAttempts, 1, dlqEnabled),
    deadLetterRepository
) {
    // Function to be set in tests to define the processing behavior
    var processFunction: ((String) -> Unit)? = null

    override fun process(payload: String) { processFunction?.invoke(payload) }
    override fun serializePayload(payload: String): String = payload
    override fun deserializePayload(payloadString: String): String = payloadString
}
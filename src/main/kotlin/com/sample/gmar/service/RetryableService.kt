package com.sample.gmar.service

import com.sample.gmar.config.RetryConfig
import com.sample.gmar.model.DeadLetterEntry
import com.sample.gmar.model.ProcessResult
import com.sample.gmar.persistence.DeadLetterRepository

abstract class RetryableService<T>(
    private val retryConfig: RetryConfig,
    protected val deadLetterRepository: DeadLetterRepository
) {

    /**
     * Process the payload once. Implement the actual processing logic here.
     */
    abstract fun process(payload: T)

    /**
     * Process a dead letter entry by deserializing it and attempting to process it again.
     * If the processing fails, it updates the dead letter entry with the new attempt count and error message.
     * Returns the result of the processing attempt.
     */
    fun process(deadLetterEntry: DeadLetterEntry): ProcessResult {
//        logger.debug("Reprocessing entry with id ${deadLetterEntry.id} (attempt ${deadLetterEntry.attempts+1})")
        val deserializedPayload = deserializePayload(deadLetterEntry.payload)
        try {
            process(deserializedPayload)
            deadLetterRepository.delete(deadLetterEntry)
            return ProcessResult.Success(deadLetterEntry.attempts+1)

        } catch (exception: Exception) {
            return saveDlqEntry(deadLetterEntry.copy(
                errorMessage = exception.message ?: "Unknown error",
                attempts = deadLetterEntry.attempts + 1
            ))
        }
    }

    /**
     * Process a payload with retry logic.
     * If processing fails, it retries up to the configured maximum attempts, delaying between attempts the configured time.
     * If all attempts fail and DLQ is enabled, it sends the payload to the Dead Letter Queue.
     */
    @JvmOverloads
    fun processWithRetry(
        payload: T,
        attempt: Int = 1
    ): ProcessResult {
        try {
//            logger.debug("Processing payload (attempt $attempt/${retryConfig.maxAttempts}): $payload")
            process(payload)
//            logger.info("Payload processed successfully after $attempt attempts")

            return ProcessResult.Success(attempt)

        } catch (ex: Exception) {
            if (attempt >= retryConfig.maxAttempts) {
//                logger.error("All retries exhausted for payload")
                return handleFailedProcessing(payload, ex, attempt)
            }

//            logger.warn("Processing failed (attempt $attempt/${retryConfig.maxAttempts}): ${ex.message}")
            Thread.sleep(retryConfig.retryDelayMillis)
            return processWithRetry(payload, attempt + 1)
        }
    }

    /**
     * Service name to identify this process
     */
    open fun getServiceName(): String = this::class.simpleName ?: "UnknownService"

    protected abstract fun serializePayload(payload: T): String
    protected abstract fun deserializePayload(payloadString: String): T

    private fun handleFailedProcessing(payload: T, ex: Exception, attempt: Int): ProcessResult {
        if (retryConfig.dlqEnabled) {
            return sendToDeadLetterQueue(payload, ex, attempt)
        }

        return ProcessResult.Failed(attempt, ex)
    }

    private fun sendToDeadLetterQueue(
        payload: T,
        exception: Exception,
        attemptCount: Int
    ): ProcessResult {
        return DeadLetterEntry(
            serviceName = getServiceName(),
            payload = serializePayload(payload),
            errorMessage = exception.message ?: "Unknown error",
            attempts = attemptCount
        ).let { saveDlqEntry(it) }
    }

    private fun saveDlqEntry(
        dlqEntry: DeadLetterEntry
    ): ProcessResult {
        return try {
            deadLetterRepository.saveOrUpdate(dlqEntry)
//                .also { logger.info("Entry sent to Dead Letter Queue: ${it.id}") }
                .let { ProcessResult.SentToDLQ(dlqEntry.attempts) }
        } catch (ex: Exception) {
            ProcessResult.Failed(dlqEntry.attempts, ex)
//                .also { logger.error("Failed to save to Dead Letter Queue: ${ex.message}", ex) }
        }
    }
}

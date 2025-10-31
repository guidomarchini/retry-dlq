package com.sample.gmar.service

import com.sample.gmar.model.DeadLetterEntry
import com.sample.gmar.model.DeadLetterEntryObjectMother
import com.sample.gmar.model.ProcessResult
import com.sample.gmar.persistence.DeadLetterRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.mockito.kotlin.*
import java.util.concurrent.atomic.AtomicInteger

class RetryableServiceTest {
    private val deadLetterRepository: DeadLetterRepository = mock()

    @AfterEach
    fun resetMocks() {
        reset(deadLetterRepository)
    }

    @Test
    fun `A retryableService should return its simple class name as serviceName`() {
        val dummyService = DummyRetryableService(1, false, deadLetterRepository)

        assertEquals("DummyRetryableService", dummyService.getServiceName())
    }

    @Test
    fun `processWithRetry should call the process function`() {
        val maxAttempts = 1
        val dummyService = DummyRetryableService(maxAttempts, false, deadLetterRepository)
        val functionCalledCount = AtomicInteger(0)
        dummyService.processFunction = { functionCalledCount.incrementAndGet() }

        val result = dummyService.processWithRetry("test payload")

        assertEquals(maxAttempts, functionCalledCount.get())
        verifyNoInteractions(deadLetterRepository)
        assertTrue(result is ProcessResult.Success)
    }

    @Test
    fun `processWithRetry should call the process function and retry up to maxAttempts`() {
        val maxAttempts = 3
        val dummyService = DummyRetryableService(maxAttempts, false, deadLetterRepository)
        val functionCalledCount = AtomicInteger(0)
        dummyService.processFunction = { if (functionCalledCount.incrementAndGet() < maxAttempts) throw RuntimeException("Processing failed") }

        val result = dummyService.processWithRetry("test payload")

        assertEquals(maxAttempts, functionCalledCount.get())
        verifyNoInteractions(deadLetterRepository)
        assertTrue(result is ProcessResult.Success)
    }

    @Test
    fun `processWithRetry should not retry nor send to dlq if attempts is 1 and dlq is disabled`() {
        val dummyService = DummyRetryableService(1, false, deadLetterRepository)
        dummyService.processFunction = { throw RuntimeException("Processing failed") }

        val result = dummyService.processWithRetry("test payload")

        verifyNoInteractions(deadLetterRepository)
        assertTrue(result is ProcessResult.Failed)
    }

    @Test
    fun `processWithRetry should not retry if attempts is 1 and send to dlq if its enabled`() {
        val dummyService = DummyRetryableService(1, true, deadLetterRepository)
        val errorMessage = "Processing failed"
        dummyService.processFunction = { throw RuntimeException(errorMessage) }
        val payload = "test payload"
        whenever(deadLetterRepository.saveOrUpdate(any()))
            .thenAnswer { it.arguments[0] }

        val result = dummyService.processWithRetry(payload)

        assertTrue(result is ProcessResult.SentToDLQ)
        val entityCaptor: KArgumentCaptor<DeadLetterEntry> = argumentCaptor()
        verify(deadLetterRepository).saveOrUpdate(entityCaptor.capture())

        val savedEntry = entityCaptor.firstValue
        assertAll (
            { assertNotNull(savedEntry) },
            { assertEquals("DummyRetryableService", savedEntry.serviceName) },
            { assertEquals(1, savedEntry.attempts) },
            { assertEquals(errorMessage, savedEntry.errorMessage) },
            { assertEquals(payload, savedEntry.payload) }
        )

    }

    @Test
    fun `processWithRetry should return a fail result if dlq fails to save in db`() {
        val dummyService = DummyRetryableService(1, true, deadLetterRepository)
        val errorMessage = "Processing failed"
        dummyService.processFunction = { throw RuntimeException(errorMessage) }
        whenever(deadLetterRepository.saveOrUpdate(any()))
            .thenThrow(RuntimeException("DB error"))

        val result = dummyService.processWithRetry("test payload")

        assertTrue(result is ProcessResult.Failed)
    }

    @Test
    fun `processWithRetry should retry if attempts is higher than 1 and send to dlq if its enabled and all calls failed`() {
        val maxAttempts = 3
        val dummyService = DummyRetryableService(maxAttempts, true, deadLetterRepository)
        val errorMessage = "Processing failed"
        val functionCalledCount = AtomicInteger(0)
        dummyService.processFunction = {
            functionCalledCount.incrementAndGet()
            throw RuntimeException("Processing failed")
        }
        val payload = "test payload"
        whenever(deadLetterRepository.saveOrUpdate(any()))
            .thenAnswer { invocation -> invocation.arguments[0] }

        val result = dummyService.processWithRetry(payload)

        assertTrue(result is ProcessResult.SentToDLQ)
        assertEquals(functionCalledCount.get(), maxAttempts)
        val entityCaptor: KArgumentCaptor<DeadLetterEntry> = argumentCaptor()
        verify(deadLetterRepository).saveOrUpdate(entityCaptor.capture())

        val savedEntry = entityCaptor.firstValue
        assertAll(
            { assertNotNull(savedEntry) },
            { assertEquals("DummyRetryableService", savedEntry.serviceName) },
            { assertEquals(maxAttempts, savedEntry.attempts) },
            { assertEquals(errorMessage, savedEntry.errorMessage) },
            { assertEquals(payload, savedEntry.payload) }
        )
    }

    @Test
    fun `process a deadLetterEntry should deserialize the content, call the process function and delete it if successful`() {
        val dummyService = DummyRetryableService(1, false, deadLetterRepository)
        var receivedParameter: String? = null
        dummyService.processFunction = { receivedParameter = it }
        val payload = "test payload"
        val deadLetterEntry = DeadLetterEntryObjectMother.aDeadLetterEntry(
            payload = payload
        )

        val result = dummyService.process(deadLetterEntry)

        assertEquals(payload, receivedParameter)
        verify(deadLetterRepository).delete(deadLetterEntry)
        assertTrue(result is ProcessResult.Success)
    }

    @Test
    fun `process a deadLetterEntry should update the deadLetterEntry if processing fails`() {
        val dummyService = DummyRetryableService(1, false, deadLetterRepository)
        val newErrorMessage = "Processing failed"
        dummyService.processFunction = { throw RuntimeException(newErrorMessage) }
        val deadLetterEntry = DeadLetterEntryObjectMother.aDeadLetterEntry(
            payload = "test payload",
            errorMessage = "Initial error",
            attempts = 1
        )
        whenever(deadLetterRepository.saveOrUpdate(any()))
            .thenAnswer { it.arguments[0] }

        val result = dummyService.process(deadLetterEntry)

        assertTrue(result is ProcessResult.SentToDLQ)
        val entityCaptor: KArgumentCaptor<DeadLetterEntry> = argumentCaptor()
        verify(deadLetterRepository).saveOrUpdate(entityCaptor.capture())

        val savedEntry = entityCaptor.firstValue
        assertAll(
            { assertNotNull(savedEntry) },
            { assertEquals(2, savedEntry.attempts) },
            { assertEquals(newErrorMessage, savedEntry.errorMessage) }
        )
    }
}
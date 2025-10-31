package com.sample.gmar.service

import com.sample.gmar.model.DeadLetterEntryObjectMother
import com.sample.gmar.model.ProcessResult
import com.sample.gmar.model.ServiceNameCount
import com.sample.gmar.persistence.DeadLetterRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.*

class DeadLetterServiceTest {
    private var deadLetterRepository: DeadLetterRepository = mock()
    private var serviceRegistry: RetryableServiceRegistry = mock()
    private var service: RetryableService<Any> = mock()
    private var deadLetterService: DeadLetterService = DeadLetterService(deadLetterRepository, serviceRegistry)

    @AfterEach
    fun resetMocks() {
        reset(deadLetterRepository, serviceRegistry, service)
    }

    @Test
    fun `listDlqMessages should return messages from repository`() {
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(serviceName = "TestService")
        whenever(deadLetterRepository.findByServiceName("TestService"))
            .thenReturn(listOf(entry))

        val result = deadLetterService.listDlqMessages("TestService")

        assertEquals(listOf(entry), result)
    }

    @Test
    fun `retryDlqMessage should throw exception if the service is not registered`() {
        val serviceName = "TestService"
        whenever(deadLetterRepository.findByServiceName(serviceName))
            .thenReturn(null)

        assertThrows<NoSuchElementException>
            { deadLetterService.retryAllDlq(serviceName) }
    }

    @Test
    fun `retryDlqMessage should process the entry`() {
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 1L, serviceName = "TestService")
        whenever(deadLetterRepository.findById(1L))
            .thenReturn(entry)
        whenever(serviceRegistry.getService("TestService"))
            .thenReturn(service)
        whenever(service.process(any()))
            .thenReturn(ProcessResult.Success(1))
        doNothing().whenever(deadLetterRepository)
            .delete(entry)

        deadLetterService.retryDlqMessage(1L)

        verify(service).process(entry)
    }

    @Test
    fun `retryDlqMessage should throw if entry not found`() {
        whenever(deadLetterRepository.findById(2L))
            .thenReturn(null)

        assertThrows<NoSuchElementException>
            { deadLetterService.retryDlqMessage(2L) }
    }

    @Test
    fun `retryDlqMessage should throw if service not found`() {
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 3L, serviceName = "UnknownService")
        whenever(deadLetterRepository.findById(3L))
            .thenReturn(entry)
        whenever(serviceRegistry.getService("UnknownService"))
            .thenReturn(null)

        assertThrows<NoSuchElementException>
            { deadLetterService.retryDlqMessage(3L) }
    }

    @Test
    fun `retryAllDlq should process all entries`() {
        val entry1 = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 4L, serviceName = "TestService")
        val entry2 = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 5L, serviceName = "TestService")
        whenever(serviceRegistry.getService("TestService"))
            .thenReturn(service)
        whenever(deadLetterRepository.findByServiceName("TestService"))
            .thenReturn(listOf(entry1, entry2))
        whenever(service.process(any()))
            .thenReturn(ProcessResult.Success(1))
        doNothing().whenever(deadLetterRepository)
            .delete(any())

        deadLetterService.retryAllDlq("TestService")

        verify(service).process(entry1)
        verify(service).process(entry2)
    }

    @Test
    fun `getMessageQuantityByServiceName should return counts`() {
        val count = ServiceNameCount(2L, "TestService")
        whenever(deadLetterRepository.countMessagesByServiceName())
            .thenReturn(listOf(count))

        val result = deadLetterService.getMessageQuantityByServiceName()

        assertEquals(listOf(count), result)
    }

    @Test
    fun `getServiceNames should return all service names`() {
        whenever(serviceRegistry.getAllServiceNames())
            .thenReturn(setOf("A", "B"))

        val result = deadLetterService.getServiceNames()

        assertEquals(setOf("A", "B"), result)
    }

    @Test
    fun `deleteDlqMessage should delete entry if found`() {
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 6L)
        whenever(deadLetterRepository.findById(6L))
            .thenReturn(entry)
        doNothing().whenever(deadLetterRepository)
            .delete(entry)

        deadLetterService.deleteDlqMessage(6L)

        verify(deadLetterRepository).delete(entry)
    }

    @Test
    fun `deleteDlqMessage should throw if entry not found`() {
        whenever(deadLetterRepository.findById(7L))
            .thenReturn(null)

        assertThrows<NoSuchElementException>
            { deadLetterService.deleteDlqMessage(7L) }
    }

    @Test
    fun `purgeDlq should delete all entries for service`() {
        val entry1 = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 8L, serviceName = "TestService")
        val entry2 = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 9L, serviceName = "TestService")
        whenever(deadLetterRepository.findByServiceName("TestService"))
            .thenReturn(listOf(entry1, entry2))
        doNothing().whenever(deadLetterRepository)
            .delete(any())

        deadLetterService.purgeDlq("TestService")

        verify(deadLetterRepository).delete(entry1)
        verify(deadLetterRepository).delete(entry2)
    }
}
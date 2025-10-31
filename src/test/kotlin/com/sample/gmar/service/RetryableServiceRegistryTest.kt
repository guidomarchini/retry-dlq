package com.sample.gmar.service

import org.junit.jupiter.api.Assertions.assertAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class RetryableServiceRegistryTest {
    @Test
    fun `getService should return the correct service by name`() {
        val serviceA = mock<RetryableService<Any>>()
        val serviceB = mock<RetryableService<Any>>()
        whenever(serviceA.getServiceName())
            .thenReturn("serviceA")
        whenever(serviceB.getServiceName())
            .thenReturn("serviceB")

        val registry = RetryableServiceRegistry(listOf(serviceA, serviceB))

        assertAll(
            { assertEquals(serviceA, registry.getService("serviceA")) },
            { assertEquals(serviceB, registry.getService("serviceB")) },
            { assertNull(registry.getService("serviceC")) }
        )
    }

    @Test
    fun `getAllServiceNames should return all registered service names`() {
        val serviceA = mock<RetryableService<Any>>()
        val serviceB = mock<RetryableService<Any>>()
        whenever(serviceA.getServiceName())
            .thenReturn("serviceA")
        whenever(serviceB.getServiceName())
            .thenReturn("serviceB")

        val registry = RetryableServiceRegistry(listOf(serviceA, serviceB))

        val names = registry.getAllServiceNames()
        assertEquals(setOf("serviceA", "serviceB"), names)
    }
}

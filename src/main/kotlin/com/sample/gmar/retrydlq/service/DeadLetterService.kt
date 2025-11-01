package com.sample.gmar.retrydlq.service

import com.sample.gmar.retrydlq.model.DeadLetterEntry
import com.sample.gmar.retrydlq.model.ServiceNameCount
import com.sample.gmar.retrydlq.persistence.DeadLetterRepository


class DeadLetterService(
    private val deadLetterRepository: DeadLetterRepository,
    private val serviceRegistry: RetryableServiceRegistry
) {
    fun listDlqMessages(serviceName: String): List<DeadLetterEntry> =
        deadLetterRepository.findByServiceName(serviceName)

    fun retryDlqMessage(messageId: Long) {
        val entry: DeadLetterEntry = deadLetterRepository.findById(messageId)
            ?: throw NoSuchElementException("DLQ entry with ID $messageId not found")
        val service: RetryableService<*> = serviceRegistry.getService(entry.serviceName)
            ?: throw NoSuchElementException("unable to find a service to handle the message")

        service.process(entry)
    }

    fun retryAllDlq(serviceName: String) {
        val service: RetryableService<*> = serviceRegistry.getService(serviceName)
            ?: throw NoSuchElementException("unable to find a service to handle the message")

        listDlqMessages(serviceName)
            .forEach { service.process(it) }
    }

    fun getMessageQuantityByServiceName(): List<ServiceNameCount> =
        deadLetterRepository.countMessagesByServiceName()

    fun getServiceNames(): Set<String> =
        serviceRegistry.getAllServiceNames()

    fun deleteDlqMessage(messageId: Long) =
        deadLetterRepository.findById(messageId)
            ?.also { deadLetterRepository.delete(it) }
            ?: throw NoSuchElementException("DLQ entry with ID $messageId not found")

    fun purgeDlq(serviceName: String) =
        listDlqMessages(serviceName)
            .forEach { runCatching { deadLetterRepository.delete(it) } }
}
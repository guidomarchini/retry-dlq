package com.sample.gmar.model

import java.time.LocalDateTime

object DeadLetterEntryObjectMother {
    fun aDeadLetterEntry(
        id: Long? = null,
        serviceName: String = "TestService",
        payload: String = "{}",
        errorMessage: String = "Error occurred",
        attempts: Int = 0,
        createdAt: LocalDateTime = LocalDateTime.now()
    ) = DeadLetterEntry(
        id = id,
        serviceName = serviceName,
        payload = payload,
        errorMessage = errorMessage,
        attempts = attempts,
        createdAt = createdAt
    )
}
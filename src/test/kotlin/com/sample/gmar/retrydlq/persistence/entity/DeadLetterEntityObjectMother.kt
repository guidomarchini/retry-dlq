package com.sample.gmar.retrydlq.persistence.entity

import java.time.LocalDateTime

object DeadLetterEntityObjectMother {
    fun aDeadLetterEntity(
        id: Long? = null,
        serviceName: String = "defaultService",
        payload: String = "{}",
        errorMessage: String = "Error occurred",
        attempts: Int = 0,
        createdAt: LocalDateTime = LocalDateTime.now()
    ) = DeadLetterEntity(
        id = id,
        serviceName = serviceName,
        payload = payload,
        errorMessage = errorMessage,
        attempts = attempts,
        createdAt = createdAt
    )

}
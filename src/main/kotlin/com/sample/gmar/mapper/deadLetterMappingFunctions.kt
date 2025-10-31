package com.sample.gmar.mapper

import com.sample.gmar.model.DeadLetterEntry
import com.sample.gmar.persistence.entity.DeadLetterEntity

fun DeadLetterEntry.toEntity(): DeadLetterEntity {
    return DeadLetterEntity(
        id = this.id,
        serviceName = this.serviceName,
        payload = this.payload,
        errorMessage = this.errorMessage,
        attempts = this.attempts,
        createdAt = this.createdAt
    )
}

fun DeadLetterEntity.toModel(): DeadLetterEntry {
    return DeadLetterEntry(
        id = this.id,
        serviceName = this.serviceName,
        payload = this.payload,
        errorMessage = this.errorMessage,
        attempts = this.attempts,
        createdAt = this.createdAt
    )
}
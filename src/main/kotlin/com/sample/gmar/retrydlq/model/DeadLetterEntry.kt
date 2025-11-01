package com.sample.gmar.retrydlq.model

import java.time.LocalDateTime

data class DeadLetterEntry(
    val id: Long? = null,
    val serviceName: String,
    val payload: String,
    val errorMessage: String,
    val attempts: Int,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
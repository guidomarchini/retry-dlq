package com.sample.gmar.persistence.entity

import java.time.LocalDateTime
import jakarta.persistence.*

@Entity
@Table(name = "dead_letter")
class DeadLetterEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val serviceName: String,

    val payload: String,

    val errorMessage: String,

    val attempts: Int = 0,

    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor(): this(null, "", "", "", 0, LocalDateTime.now())
}
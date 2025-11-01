package com.sample.gmar.retrydlq.persistence

import com.sample.gmar.retrydlq.mapper.toEntity
import com.sample.gmar.retrydlq.mapper.toModel
import com.sample.gmar.retrydlq.model.DeadLetterEntry
import com.sample.gmar.retrydlq.model.ServiceNameCount
import org.springframework.data.repository.findByIdOrNull

class DeadLetterRepository(
    val jpaRepository: DeadLetterJpaRepository
) {

    fun saveOrUpdate(entry: DeadLetterEntry): DeadLetterEntry =
        jpaRepository.save(entry.toEntity()).toModel()

    fun findById(id: Long): DeadLetterEntry? =
        jpaRepository.findByIdOrNull(id)?.toModel()

    fun findByServiceName(serviceName: String): List<DeadLetterEntry> =
        jpaRepository.findByServiceName(serviceName).map { it.toModel() }

    fun countMessagesByServiceName(): List<ServiceNameCount> =
        jpaRepository.countByServiceName()

    fun delete(entry: DeadLetterEntry) {
        jpaRepository.delete(entry.toEntity())
    }
}

package com.sample.gmar.persistence

import com.sample.gmar.mapper.toEntity
import com.sample.gmar.mapper.toModel
import com.sample.gmar.model.DeadLetterEntry
import com.sample.gmar.model.ServiceNameCount
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

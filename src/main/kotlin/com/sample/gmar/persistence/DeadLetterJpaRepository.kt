package com.sample.gmar.persistence

import com.sample.gmar.model.ServiceNameCount
import com.sample.gmar.persistence.entity.DeadLetterEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

/**
 * JPA repository for ProductEntity.
 * Separated from the ProductRepository interface to avoid leaking JPA specifics.
 */
interface DeadLetterJpaRepository : JpaRepository<DeadLetterEntity, Long> {
    fun findByServiceName(serviceName: String): List<DeadLetterEntity>

    @Query("SELECT new com.sample.gmar.model.ServiceNameCount(count(*), serviceName) FROM DeadLetterEntity GROUP BY serviceName")
    fun countByServiceName(): List<ServiceNameCount>
}
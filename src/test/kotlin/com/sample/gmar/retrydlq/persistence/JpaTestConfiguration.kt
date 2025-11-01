package com.sample.gmar.retrydlq.persistence

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.ActiveProfiles

@ComponentScan(basePackages = ["com.sample.gmar.retrydlq.persistence"])
@EntityScan(basePackages = ["com.sample.gmar.retrydlq.persistence.entity"])
@EnableJpaRepositories(basePackages = ["com.sample.gmar.retrydlq.persistence"])
@ActiveProfiles("test")
open class JpaTestConfiguration {
    @Bean
    open fun deadLetterRepository(jpaRepository: DeadLetterJpaRepository): DeadLetterRepository = DeadLetterRepository(jpaRepository)
}

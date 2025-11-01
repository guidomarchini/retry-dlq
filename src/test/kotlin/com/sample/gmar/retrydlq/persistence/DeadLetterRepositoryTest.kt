package com.sample.gmar.retrydlq.persistence

import com.sample.gmar.retrydlq.mapper.toModel
import com.sample.gmar.retrydlq.model.DeadLetterEntryObjectMother
import com.sample.gmar.retrydlq.model.ServiceNameCount
import com.sample.gmar.retrydlq.persistence.entity.DeadLetterEntity
import com.sample.gmar.retrydlq.persistence.entity.DeadLetterEntityObjectMother
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource

@DataJpaTest
@ContextConfiguration(classes = [JpaTestConfiguration::class])
@TestPropertySource(properties = [
    "spring.jpa.hibernate.ddl-auto=create-drop"
])
class DeadLetterRepositoryTest {
    @Autowired private lateinit var entityManager: TestEntityManager
    @Autowired private lateinit var repository: DeadLetterRepository

    @Test
    fun `saveOrUpdate should persist and return DeadLetterEntry`() {
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(id = null)

        val result = repository.saveOrUpdate(entry)

        assertNotNull(result.id)
    }

    @Test
    fun `saveOrUpdate should update and return DeadLetterEntry`() {
        val entity = DeadLetterEntityObjectMother.aDeadLetterEntity(
            id = null,
            attempts = 1,
            errorMessage = "Error"
        )
        entityManager.persistAndFlush(entity)

        val newAttempts = 2
        val newError = "Another error"
        val entryId = entity.id
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(
            id = entryId,
            attempts = newAttempts,
            errorMessage = newError
        )

        val result = repository.saveOrUpdate(entry)

        assertAll(
            { assertEquals(entryId, result.id) },
            { assertEquals(newAttempts, result.attempts) },
            { assertEquals(newError, result.errorMessage) }
        )
    }

    @Test
    fun `findById should return DeadLetterEntry if found`() {
        val entity = DeadLetterEntityObjectMother.aDeadLetterEntity(id = null)
        entityManager.persistAndFlush(entity)

        val result = repository.findById(entity.id!!)

        assertAll(
            {  assertNotNull(result) },
            { assertEquals(entity.id, result!!.id) },
            { assertEquals(entity.serviceName, result!!.serviceName) },
            { assertEquals(entity.payload, result!!.payload) },
        )
    }

    @Test
    fun `findById should return null if not found`() {
        val result = repository.findById(2L)

        assertNull(result)
    }

    @Test
    fun `findByServiceName should return list of DeadLetterEntry`() {
        val entity = DeadLetterEntityObjectMother.aDeadLetterEntity(id = null, serviceName = "TestService")
        entityManager.persistAndFlush(entity)

        val result = repository.findByServiceName("TestService")

        assertEquals(result, listOf(entity.toModel()))
    }

    @Test
    fun `delete should remove entity if found`() {
        val entity: DeadLetterEntity = DeadLetterEntityObjectMother.aDeadLetterEntity(id = null)
        val newEntityId = entityManager.persistAndFlush(entity).id!!

        val entry = entity.toModel()
        repository.delete(entry)

        val found = repository.findById(newEntityId)

        assertNull(found)
    }

    @Test
    fun `delete should not remove if entity not found`() {
        val entry = DeadLetterEntryObjectMother.aDeadLetterEntry(id = 2L)
        repository.delete(entry)

        val found = repository.findById(2L)

        assertNull(found)
    }

    @Test
    fun `countMessagesByServiceName should return correct counts`() {
        val serviceA = "ServiceA"
        val serviceB = "ServiceB"
        val entityA1 = DeadLetterEntityObjectMother.aDeadLetterEntity(id = null, serviceName = serviceA)
        val entityA2 = DeadLetterEntityObjectMother.aDeadLetterEntity(id = null, serviceName = serviceA)
        val entityB = DeadLetterEntityObjectMother.aDeadLetterEntity(id = null, serviceName = serviceB)

        entityManager.persistAndFlush(entityA1)
        entityManager.persistAndFlush(entityA2)
        entityManager.persistAndFlush(entityB)

        val result = repository.countMessagesByServiceName()

        val expected = setOf(
            ServiceNameCount(2L, serviceA),
            ServiceNameCount(1L, serviceB)
        )
        assertEquals(result.toSet(), expected)
    }


}

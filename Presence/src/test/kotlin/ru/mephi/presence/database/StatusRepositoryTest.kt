package ru.mephi.presence.database

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import kotlin.test.Test

/*
@Testcontainers
@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
class StatusRepositoryTest {
    @Container
    val redisContainer: GenericContainer<*> = GenericContainer(DockerImageName.parse("redis:7.0"))
        .withExposedPorts(6379)
        .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))

    private lateinit var statusRepository: StatusRepository


    @BeforeAll
    fun setup() {
        redisContainer.portBindings = listOf("6379:6379")
        redisContainer.start()
        redisContainer.waitingFor(Wait.forListeningPort())

        // Получаем хост и маппированный порт
        val redisHost = redisContainer.host
        val redisPort = redisContainer.getMappedPort(6379)

        println("Redis is running at $redisHost:$redisPort")

        val connectionFactory = LettuceConnectionFactory(redisHost, redisPort)
        connectionFactory.afterPropertiesSet()
        val redisTemplate = ReactiveRedisTemplate(connectionFactory, RedisSerializationContext.string())
        statusRepository = StatusRepository(redisTemplate)
    }

    @Test
    fun `setActive should return true when status is set`() {
        val email = "test@example.com"

        StepVerifier.create(statusRepository.setActive(email))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `getStatus should return inactive when status is not set`() {
        val email = "inactive@example.com"

        StepVerifier.create(statusRepository.getStatus(email))
            .expectNext("inactive")
            .verifyComplete()
    }

    @Test
    fun `setUserTracking should return true when tracking is set with MANY users`() {
        val emailToSet = "user@example.com"
        val userWhoTracking = "tracker@example.com"

        StepVerifier.create(statusRepository.setUserTracking(emailToSet, userWhoTracking))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `setUserTracking should return true when tracking is set with ONE user`() {
        val emailToSet = "user@example.com"
        val usersWhoTracking = setOf("tracker1@example.com", "tracker2@example.com", "tracker3@example.com")

        StepVerifier.create(statusRepository.setUserTracking(emailToSet, usersWhoTracking))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `set user inactive return true`() {
        val emailToSet = "user@example.com"
        val usersWhoTracking = setOf("tracker1@example.com", "tracker2@example.com", "tracker3@example.com")

        StepVerifier.create(statusRepository.setUserTracking(emailToSet, usersWhoTracking))
            .expectNext(true)
            .verifyComplete()

        val emailToRemove  = "user@example.com"

        StepVerifier.create(statusRepository.setInactive(emailToRemove))
            .expectNext(true)
            .verifyComplete()
    }
}
*/
package ru.mephi.presence

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.web.reactive.config.EnableWebFlux

@SpringBootApplication
@EnableWebFlux
@EnableKafka
@EnableDiscoveryClient
class PresenceApplication

fun main(args: Array<String>) {
    runApplication<PresenceApplication>(*args)
}

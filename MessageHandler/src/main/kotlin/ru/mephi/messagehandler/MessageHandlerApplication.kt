package ru.mephi.messagehandler

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient


@SpringBootApplication
@EnableDiscoveryClient
class MessageHandlerApplication

fun main(args: Array<String>) {
    runApplication<MessageHandlerApplication>(*args)
}

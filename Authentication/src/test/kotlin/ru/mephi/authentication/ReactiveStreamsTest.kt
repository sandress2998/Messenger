package ru.mephi.authentication

import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono


class ReactiveStreamsTest {
    @Test
    fun thenTest() {
        Mono.just("start")
            .flatMap { Mono.error<String>(RuntimeException("Something went wrong")) }
            .then(
                run {
                    println("It's in then")
                    Mono.defer {
                        println("In Mono.defer")
                        Mono.just("in mono defer")
                    }
                }
            )
            .onErrorResume {
                println("In onErrorResume 1")
                Mono.error(RuntimeException("Something went wrong"))
            }
            .onErrorResume {
                println("In onErrorResume 2")
                Mono.just("...")
            }
            .flatMap { it: String ->
               println(it)
               Mono.just("Hi")
            }
            .doOnTerminate { println("Terminate") }
            .subscribe({ println(it) }, { println("Error: ${it.message}") })
        assert(true)
    }

}
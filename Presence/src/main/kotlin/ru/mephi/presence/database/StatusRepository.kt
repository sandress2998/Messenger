package ru.mephi.presence.database

import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers

@Repository
class StatusRepository(
    private val redisTemplate: ReactiveRedisTemplate<String, String>
) {
    private val redisSetOps = redisTemplate.opsForSet()
    private val redisValueOps = redisTemplate.opsForValue()

    fun setActive(email: String): Mono<Boolean> {
        return redisValueOps.set("status:$email", "active")
    }

    fun getStatus(email: String): Mono<String> {
        return redisValueOps.get("status:$email")
            .switchIfEmpty(Mono.just("inactive"))
    }

    fun setUserTracking(emailToSet: String, userWhoTracking: String): Mono<Boolean> {
        val addUserTracking = redisSetOps.add("tracking:$userWhoTracking", emailToSet)
        val addUserToBeTracked = redisSetOps.add("tracked:$emailToSet", userWhoTracking)

        return Mono.zip(addUserTracking, addUserToBeTracked)
            .map { tuple ->
                // Возвращаем true, если обе операции успешны
                tuple.t1 > 0 && tuple.t2 > 0
            }
            .flatMap { setActive(emailToSet) }
    }

    fun setUserTracking(emailToSet: String, usersWhoTracking: Set<String>): Mono<Boolean> {
        val usersWhoTrackingFlux = Flux.fromIterable(usersWhoTracking)

        return usersWhoTrackingFlux.parallel() // Включаем параллельную обработку
            .runOn(Schedulers.parallel()) // Указываем планировщик
            .flatMap { emailWhoTracks ->
                val addUserTracking = redisSetOps.add("tracking:$emailWhoTracks", emailToSet)
                val addUserToBeTracked = redisSetOps.add("tracked:$emailToSet", emailWhoTracks)

                Mono.zip(addUserTracking, addUserToBeTracked)
                    .map { tuple ->
                        tuple.t1 > 0 && tuple.t2 > 0
                    }
                    .doOnError { error ->
                        println("Error adding tracking for $emailWhoTracks: ${error.message}")
                    }
            }
            .sequential() // Возвращаемся к последовательному потоку
            .all { it } // Проверяем, что все операции успешны
            .doOnSuccess { success ->
                if (success) {
                    println("All tracking relationships added successfully.")
                } else {
                    println("Some tracking relationships failed to add.")
                }
            }
            .flatMap { setActive(emailToSet) }
    }

    fun setInactive(emailToRemove: String): Mono<Boolean> {
        return redisSetOps.members("tracked:$emailToRemove")
            .flatMap { emailToReduceTracking ->
                redisSetOps.remove("tracking:$emailToReduceTracking", emailToRemove)
                    .doOnSuccess { removed ->
                        if (removed > 0) {
                            println("Removed $emailToRemove from tracking:$emailToReduceTracking")
                        } else {
                            println("Failed to remove $emailToRemove from tracking:$emailToReduceTracking")
                        }
                    }
                    .doOnError { error ->
                        println("Error removing $emailToRemove from tracking:$emailToReduceTracking: ${error.message}")
                    }
            }
            .then(redisSetOps.delete("tracked:$emailToRemove"))
            .doOnSuccess { deleted ->
                if (deleted) {
                    println("Deleted tracked:$emailToRemove")
                } else {
                    println("Failed to delete tracked:$emailToRemove")
                }
            }
            .then (redisSetOps.members("tracking:$emailToRemove")
            .flatMap { emailWhoTracked: String ->
                redisSetOps.remove("tracked:$emailWhoTracked", emailToRemove)
                    .doOnSuccess { removed ->
                        if (removed > 0) {
                            println("Removed $emailToRemove from tracked:$emailWhoTracked")
                        } else {
                            println("Failed to remove $emailToRemove from tracked:$emailWhoTracked")
                        }
                    }
                    .doOnError { error ->
                        println("Error removing $emailToRemove from tracked:$emailWhoTracked : ${error.message}")
                    }

            }
            .then())
            .then(redisValueOps.delete("status:$emailToRemove"))
            .doOnSuccess { deleted ->
                if (deleted) {
                    println("Deleted status:$emailToRemove")
                } else {
                    println("Failed to delete status:$emailToRemove")
                }
            }
            .thenReturn(true) // Возвращаем true, если все операции выполнены
            .onErrorReturn(false) // Возвращаем false в случае ошибки
    }

    fun fetchUsersTracking(email: String): Mono<List<String>> {
        return redisSetOps.members("tracked:$email").collectList()

    }
}

/*
fun removeUserTracking(emailToRemove: String): Mono<Boolean> {
        return redisSetOps.members("tracked:$emailToRemove")
            .flatMap { emailToReduceTracking ->
                redisSetOps.remove("tracking:$emailToReduceTracking", emailToRemove)
            }
            .then(redisSetOps.delete("tracked:$emailToRemove"))
            .then(redisValueOps.delete("status:$emailToRemove"))
    }
 */

/*
// тут нужно послать запрос в user-service, чтобы понять, какие контакты есть у пользователя,
// и какие из них активны
fun addUserTracking(emailToAdd: String, trackingUsers: Set<String>): Mono<Boolean> {
    val users = Flux.fromIterable(trackingUsers)

    return users.flatMap { emailWhoTracks ->
        // Добавляем emailToAdd в tracking:emailWhoTracks
        val addUserTracking = redisSetOps.add("tracking:$emailWhoTracks", emailToAdd)

        // Добавляем emailWhoTracks в tracked:emailToAdd
        val addUserToBeTracked = redisSetOps.add("tracked:$emailToAdd", emailWhoTracks)

        // Комбинируем результаты двух операций
        Mono.zip(addUserTracking, addUserToBeTracked)
            .map { tuple ->
                // Возвращаем true, если обе операции успешны
                tuple.t1 > 0 && tuple.t2 > 0
            }
    }.all { it }
}
*/
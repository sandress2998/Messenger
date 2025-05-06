package ru.mephi.authentication

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.context.annotation.EnableLoadTimeWeaving
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import ru.mephi.authentication.config.TimerAspectConfig
import ru.mephi.authentication.controller.AuthorizationController
import ru.mephi.authentication.database.dao.PasswordRepository
import ru.mephi.authentication.database.dao.RefreshRepository
import ru.mephi.authentication.model.service.JwtService
import ru.mephi.authentication.model.service.PasswordService
import ru.mephi.authentication.model.service.RefreshService
import ru.mephi.authentication.model.service.SecurityService

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
class AuthenticationApplication

fun main(args: Array<String>) {
    runApplication<AuthenticationApplication>(*args)
}



/*
@Component
class MetricsInitializer (
    private val timerAspectConfig: TimerAspectConfig
) {
    // этот метод хоть и создает то, что надо, но метрики не собираются. Видимо, Spring WebFlux
    // здесь проседает
    @PostConstruct
    fun init() {
        println("Инициализируем метрики")

        // controllerMetricsInit
        // это сделать сложновато, реализовано в контроллерах

        // serviceMetricsInit
        listOf(
            RefreshRepository::class.java,
            PasswordRepository::class.java,
            JwtService::class.java,
            PasswordService::class.java,
            RefreshService::class.java,
            SecurityService::class.java,
        ).run {
            forEach { currentClass ->
                currentClass.declaredMethods
                    .filter { method ->
                        // Фильтруем только методы, объявленные непосредственно в этих классах
                        method.declaringClass == currentClass
                    }
                    .forEach { method ->
                        timerAspectConfig.setBusinessTimer("${currentClass.simpleName}.${method.name}")
                    }
            }

        // sqlRepositoryMetricsInit
        listOf(PasswordRepository::class.java)
            .run {
                forEach { currentClass ->
                    currentClass.declaredMethods
                        .filter { method ->
                            // Фильтруем только методы, объявленные непосредственно в этих классах
                            method.declaringClass == currentClass
                        }
                        .forEach { method ->
                            timerAspectConfig.setSQLDatabaseTimer("${currentClass.simpleName}.${method.name}")
                        }
                }
            }

        // nosqlRepositoryMetricsInit
        listOf(RefreshRepository::class.java)
            .run {
                forEach { currentClass ->
                    currentClass.declaredMethods
                        .filter { method ->
                            // Фильтруем только методы, объявленные непосредственно в этих классах
                            method.declaringClass == currentClass
                        }
                        .forEach { method ->
                            timerAspectConfig.setNoSQLDatabaseTimer("${currentClass.simpleName}.${method.name}")
                        }
                }

            }
        }
    }
}
 */
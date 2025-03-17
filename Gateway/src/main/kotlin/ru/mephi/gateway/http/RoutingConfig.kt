package ru.mephi.gateway.http

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RoutingConfiguration {
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            .route("auth_route") { r ->
                r.path("/auth/**") // Обрабатывает запросы на путь /api/**
                    .filters { f ->
                        // Переписываем путь, удаляя префикс /api
                        f.rewritePath("/auth/(?<segment>.*)", "/auth/${'$'}{segment}")
                    }
                    .uri("lb://authentication-service:8090") // Перенаправляет на сервис аутентификации
            }
            .build()
    }
}
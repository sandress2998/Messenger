package ru.mephi.gateway.http

import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod


@Configuration
class RoutingConfiguration {
    @Bean
    fun customRouteLocator(builder: RouteLocatorBuilder): RouteLocator {
        return builder.routes()
            // Общий маршрут для сервиса аутентификации
            .route("auth_route") { r ->
                r.path("/auth/**") // Обрабатывает запросы на путь /api/**
                    .filters { f ->
                        // Переписываем путь, удаляя префикс /api
                        f.rewritePath("/auth/(?<segment>.*)", "/auth/${'$'}{segment}")
                    }
                    .uri("lb://authentication-service:8090") // Перенаправляет на сервис аутентификации
            }
            .route("chat_route") { r ->
                r.path("/chats", "/chats/{chatId}", "/chats/{chatId}/members",
                    "/chats/{chatId}/members/{memberId}", "/chats/{chatsId}/users")
                    .and()
                    .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE)
                    .uri("lb://chat-service:8082") // Проксирование на chat-service
            }
            // Маршрут для /users
            .route("user_route") { r ->
                r.path("/users") // Обрабатывает запросы на путь /users/{userId}
                    .and()
                    .method(HttpMethod.GET, HttpMethod.PATCH, HttpMethod.POST, HttpMethod.DELETE)
                    .uri("lb://user-service:8083") // Перенаправляем на user-service
            }
            .route("create_message_route") { r ->
                r.path("/chats/{chatId}/messages")
                    .and()
                    .method(HttpMethod.POST)
                    .uri("lb://message-handler-service:8091")
            }
            .route("delete_all_messages") { r ->
                r.path("/chats/{chatId}/messages")
                    .and()
                    .method(HttpMethod.DELETE)
                    .uri("lb://message-handler-service:8091")
            }
            .route("concrete_message_route") { r ->
                r.path("/chats/{chatId}/messages/{messageId}")
                    .and()
                    .method(HttpMethod.GET, HttpMethod.PATCH, HttpMethod.DELETE)
                    .uri("lb://message-handler-service:8091")
            }
            .route("update_status_route") { r ->
                r.path("/chats/{chatId}/messages/{messageId}/status/{status}")
                    .and()
                    .method(HttpMethod.PATCH)
                    .uri("lb://message-handler-service:8091")
            }
            .route("generic_messages_route") { r ->
                r.path("/messages")
                    .and()
                    .method(HttpMethod.GET, HttpMethod.POST)
                    .uri("lb://message-handler-service:8091")
            }
            .route("unread_changes_route") { r ->
                r.path("/chats/{chatId}/messages")
                    .and()
                    .method(HttpMethod.GET)
                    .uri("lb://message-handler-service:8091")
            }
            .build()
    }
}
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
            // Общий маршрут для /chats/{chatId}/messages (GET, POST, DELETE)
            .route("chat_messages_route") { r ->
                r.path("/chats/{chatId}/messages") // Обрабатывает запросы на путь /chats/{chatId}/messages
                    .and() // Добавляем условия для HTTP-методов
                    .method(HttpMethod.GET, HttpMethod.POST, HttpMethod.DELETE) // Обрабатываем GET, POST, DELETE
                    .filters { f ->
                        // Переписываем путь, если нужно
                        f.rewritePath("/chats/(?<chatId>[^/]+)/messages", "/chats/${'$'}{chatId}/messages")
                    }
                    .uri("lb://message-handler-service:8091") // Перенаправляем на message-handler-service
            }
            // Маршрут для обновления сообщения
            .route("update_message_route") { r ->
                r.path("/messages/{messageId}") // Обрабатывает запросы на путь /messages/{messageId}
                    .and()
                    .method(HttpMethod.PATCH) // Обрабатываем только PATCH
                    .filters { f ->
                        f.rewritePath("/messages/(?<messageId>[^/]+)", "/messages/${'$'}{messageId}")
                    }
                    .uri("lb://message-handler-service:8091")
            }
            // Маршрут для удаления сообщений в чате по отправителю
            .route("delete_messages_by_sender_route") { r ->
                r.path("/chats/{chatId}/members/{senderId}") // Обрабатывает запросы на путь /chats/{chatId}/members/{senderId}
                    .and()
                    .method(HttpMethod.DELETE) // Обрабатываем только DELETE
                    .filters { f ->
                        f.rewritePath("/chats/(?<chatId>[^/]+)/members/(?<senderId>[^/]+)", "/chats/${'$'}{chatId}/members/${'$'}{senderId}")
                    }
                    .uri("lb://message-handler-service:8091")
            }
            // Маршрут для удаления конкретного сообщения
            .route("delete_message_route") { r ->
                r.path("/messages/{messageId}") // Обрабатывает запросы на путь /messages/{messageId}
                    .and()
                    .method(HttpMethod.DELETE) // Обрабатываем только DELETE
                    .filters { f ->
                        f.rewritePath("/messages/(?<messageId>[^/]+)", "/messages/${'$'}{messageId}")
                    }
                    .uri("lb://message-handler-service:8091")
            }
            // Маршрут для /chats/{chatId} (GET, PATCH, DELETE)
            .route("chat_route") { r ->
                r.path("/chats/{chatId}") // Обрабатывает запросы на путь /chats/{chatId}
                    .and()
                    .method(HttpMethod.GET, HttpMethod.PATCH, HttpMethod.DELETE) // Обрабатываем GET, PATCH, DELETE
                    .filters { f ->
                        f.rewritePath("/chats/(?<chatId>[^/]+)", "/chats/${'$'}{chatId}")
                    }
                    .uri("lb://chat-service:8082") // Перенаправляем на chat-service
            }
            // Маршрут для /users/{userId}/chats (GET)
            .route("user_chats_route") { r ->
                r.path("/users/{userId}/chats") // Обрабатывает запросы на путь /users/{userId}/chats
                    .and()
                    .method(HttpMethod.GET) // Обрабатываем только GET
                    .filters { f ->
                        f.rewritePath("/users/(?<userId>[^/]+)/chats", "/users/${'$'}{userId}/chats")
                    }
                    .uri("lb://chat-service:8082")
            }
            // Маршрут для /chats/{chatId}/members (GET, POST)
            .route("chat_members_route") { r ->
                r.path("/chats/{chatId}/members") // Обрабатывает запросы на путь /chats/{chatId}/members
                    .and()
                    .method(HttpMethod.GET, HttpMethod.POST) // Обрабатываем GET, POST
                    .filters { f ->
                        f.rewritePath("/chats/(?<chatId>[^/]+)/members", "/chats/${'$'}{chatId}/members")
                    }
                    .uri("lb://chat-service:8082")
            }
            // Маршрут для /chats (POST)
            .route("create_chat_route") { r ->
                r.path("/chats") // Обрабатывает запросы на путь /chats
                    .and()
                    .method(HttpMethod.POST) // Обрабатываем только POST
                    .filters { f ->
                        f.rewritePath("/chats", "/chats")
                    }
                    .uri("lb://chat-service:8082")
            }
            // Маршрут для /chats/{chatId}/members/{userId} (PATCH, DELETE)
            .route("chat_member_route") { r ->
                r.path("/chats/{chatId}/members/{userId}") // Обрабатывает запросы на путь /chats/{chatId}/members/{userId}
                    .and()
                    .method(HttpMethod.PATCH, HttpMethod.DELETE) // Обрабатываем PATCH, DELETE
                    .filters { f ->
                        f.rewritePath("/chats/(?<chatId>[^/]+)/members/(?<userId>[^/]+)", "/chats/${'$'}{chatId}/members/${'$'}{userId}")
                    }
                    .uri("lb://chat-service:8082")
            }
            // Маршрут для /users/{userId} (GET, PATCH)
            .route("user_route") { r ->
                r.path("/users/{userId}") // Обрабатывает запросы на путь /users/{userId}
                    .and()
                    .method(HttpMethod.GET, HttpMethod.PATCH) // Обрабатываем GET и PATCH
                    .filters { f ->
                        f.rewritePath("/users/(?<userId>[^/]+)", "/users/${'$'}{userId}") // Переписываем путь
                    }
                    .uri("lb://user-service:8083") // Перенаправляем на user-service
            }
            .build()
    }
}
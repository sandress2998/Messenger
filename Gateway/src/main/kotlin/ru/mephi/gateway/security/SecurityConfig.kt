package ru.mephi.gateway.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig(private val jwtFilter: JwtFilter) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .csrf { csrf -> csrf.disable() } // Отключаем CSRF
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/auth/signup").permitAll() // Разрешаем доступ к /auth/** без аутентификации
                    .pathMatchers("/auth/signin").permitAll()
                    .pathMatchers("/auth/refresh").permitAll()
                    .anyExchange().authenticated() // Все остальные запросы требуют аутентификации
            }
            .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION) // Добавляем JwtFilter
            .build()
    }
}
package ru.mephi.authentication.webclient

import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import ru.mephi.authentication.webclient.dto.CreateUserDTO

@Service
class UserService (
    private val userServiceWebClient: WebClient
) {
    fun createUser(request: CreateUserDTO): Mono<CreateUserDTO> {
        return userServiceWebClient.post()
            .uri("/users")
            .bodyValue(request)
            .retrieve()
            .onStatus({ httpStatusCode -> httpStatusCode.is4xxClientError }) { response ->
                return@onStatus Mono.error(
                    WebClientResponseException.create(
                        response.statusCode().value(), // Код статуса как Int
                        "Error: ${response.statusCode()}", // Сообщение об ошибке
                        response.headers().asHttpHeaders(), // Заголовки ответа
                        byteArrayOf(), // Тело ответа (ByteArray)
                        null // Кодировка (Charset)
                    )
                )
            }
            .onStatus({ httpStatusCode -> httpStatusCode.is5xxServerError }) { response ->
                return@onStatus Mono.error(
                    WebClientResponseException.create(
                        response.statusCode().value(), // Код статуса как Int
                        "Error: ${response.statusCode()}", // Сообщение об ошибке
                        response.headers().asHttpHeaders(), // Заголовки ответа
                        byteArrayOf(), // Тело ответа (ByteArray)
                        null // Кодировка (Charset)
                    )
                )
            }
            .bodyToMono(CreateUserDTO::class.java)
    }
}
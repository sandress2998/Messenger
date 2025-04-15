package ru.mephi.presence.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.presence.database.StatusRepository
import ru.mephi.presence.model.dto.ChatActiveMembersResponse
import ru.mephi.presence.model.service.StatusService
import java.util.*


@Service
class StatusServiceImpl(
    private val statusRepository: StatusRepository,
): StatusService {

    override fun connectToChat(userId: UUID, chatId: UUID): Mono<Void> {
        return statusRepository.connectToChat(userId, chatId)
    }

    override fun disconnectFromChat(userId: UUID, chatId: UUID): Mono<Void> {
        return statusRepository.disconnectFromChat(userId, chatId)
    }

    override fun fetchActiveMembers(userId: UUID, chatId: UUID): Mono<ChatActiveMembersResponse> {
        return statusRepository.fetchUsersExceptOne(userId, chatId)
            .map { activeMembersList ->
                ChatActiveMembersResponse(chatId, activeMembersList)
            }
    }
}

/*
@Service
class StatusServiceImpl(
    private val statusRepository : StatusRepository
): StatusService {
    override fun setActive(email: String): Mono<Boolean> {
        return statusRepository.setActive(email)
    }

    override fun setInactive(email: String): Mono<Boolean> {
        return statusRepository.setInactive(email)
    }

    override fun getStatus(email: String): Mono<String> {
        return statusRepository.getStatus(email)
    }

    override fun setUserTracking(email: String, userWhoTracking: String): Mono<Boolean> {
        return statusRepository.setUserTracking(email, userWhoTracking)
    }

    override fun setUserTracking(email: String, usersWhoTracking: Set<String>): Mono<Boolean> {
        return statusRepository.setUserTracking(email, usersWhoTracking)
    }

    override fun fetchUserTracking(email: String): Mono<List<String>> {
        return statusRepository.fetchUsersTracking(email)
    }
}
 */
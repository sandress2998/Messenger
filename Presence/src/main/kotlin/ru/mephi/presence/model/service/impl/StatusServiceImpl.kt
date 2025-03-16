package ru.mephi.presence.model.service.impl

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.presence.database.StatusRepository
import ru.mephi.presence.model.dto.ChatActiveMembersRequest
import ru.mephi.presence.model.dto.ChatActiveMembersResponse
import ru.mephi.presence.model.service.StatusService


@Service
class StatusServiceImpl(
    private val statusRepository: StatusRepository,
): StatusService {

    override fun connectToChat(email: String, chatID: String): Mono<Void> {
        return statusRepository.connectToChat(email, chatID)
    }

    override fun disconnectFromChat(email: String, chatID: String): Mono<Void> {
        return statusRepository.disconnectFromChat(email, chatID)
    }

    override fun fetchActiveMembers(request: ChatActiveMembersRequest): Mono<ChatActiveMembersResponse> {
        return statusRepository.fetchUsersExceptOne(request.requesting, request.chatID)
            .map { activeMembersList ->
                ChatActiveMembersResponse(request.chatID, activeMembersList)
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
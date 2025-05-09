package ru.mephi.presence.model.service.impl

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import ru.mephi.presence.database.StatusRepository
import ru.mephi.presence.model.ActivityStatus
import ru.mephi.presence.model.dto.rest.UserActivityResponse
import ru.mephi.presence.model.service.StatusService
import java.util.*


@Service
class StatusServiceImpl(
    private val statusRepository: StatusRepository,
): StatusService {
    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations"
    )
    override fun setActive(userId: UUID): Mono<Void> {
        return statusRepository.setActive(userId)
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations"
    )
    override fun setInactive(userId: UUID): Mono<Void> {
        return statusRepository.setInactive(userId)
    }

    @Timed(
        value = "business.operation.time",  description = "Time taken to execute business operations"
    )
    override fun isActive(userId: UUID): Mono<UserActivityResponse> {
        return statusRepository.isActive(userId)
            .map { isActive ->
                when (isActive) {
                    true -> UserActivityResponse(ActivityStatus.ACTIVE)
                    false -> UserActivityResponse(ActivityStatus.INACTIVE)
                }
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
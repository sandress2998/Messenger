package ru.mephi.chatservice.service

import ru.mephi.chatservice.models.entity.Chat
import ru.mephi.chatservice.models.entity.ChatMember
import ru.mephi.chatservice.repository.ChatMembersRepository
import ru.mephi.chatservice.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.chatservice.models.ChatRole
import ru.mephi.chatservice.models.dto.*
import ru.mephi.chatservice.models.exception.NotFoundException
import ru.mephi.chatservice.models.exception.AccessDeniedException
import ru.mephi.chatservice.models.exception.FailureResult
import ru.mephi.chatservice.repository.UserRepository
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMembersRepository:ChatMembersRepository,
    private val userRepository: UserRepository
) {
    // Операции только с чатами
    @Transactional
    fun createChat(chat: Chat, userInitiatorId: UUID): Mono<RequestResult> {
        return chatRepository.save(chat)
            .flatMap { savedChat ->
                chatMembersRepository.save(ChatMember(savedChat.id!!, userInitiatorId, ChatRole.ADMIN))
            }
            .map { chatMember ->
                ChatCreationResponse(chatMember.chatId!!, chatMember.id!!) as RequestResult
            }
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    @Transactional
    fun updateChat(chat: Chat, chatId: UUID, userId: UUID, ): Mono<RequestResult> {
        val name = chat.name
        return isUserAdminInChat(chatId, userId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    chatRepository.update(chatId, name)
                }
            }
            .thenReturn(SuccessResult() as RequestResult)
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    @Transactional
    fun deleteChat(chatId: UUID, userInitiatorId: UUID): Mono<RequestResult> {
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(chatMembersRepository.deleteChatMembersByChatId(chatId))
            .then(chatRepository.deleteById(chatId))
            .thenReturn(SuccessResult() as RequestResult)
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    fun getChatsInfoByUserId(userId: UUID): Flux<RequestResult> {
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMember ->
                getChatInfoByChatId(chatMember.chatId!!, userId)
            }
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    // Сделать пагинацию
    fun getChatMembersByChatId(chatId: UUID, userInitiatorId: UUID): Flux<RequestResult> {
        return isUserMemberInChat(chatId, userInitiatorId)
            .flatMapMany { isMember: Boolean ->  // Используем flatMapMany вместо flatMap, т.к. нужно вернуть Flux
                if (!isMember) {
                    Flux.error(AccessDeniedException(AccessDeniedException.Cause.NOT_MEMBER))
                } else {
                    chatMembersRepository.getChatMembersByChatId(chatId)
                        .flatMap { chatMember: ChatMember ->
                            userRepository.getUsernameById(chatMember.userId!!)
                                // пропускаем тех пользователей, которых не нашли?
                                //.filter { username -> username != null}
                                .map { username: String? ->
                                    MemberInfoResponse(chatMember.id!!, username!!, chatMember.role) as RequestResult
                                }
                                .switchIfEmpty(
                                    Mono.just(MemberInfoResponse(chatMember.id!!, "DELETED_ACCOUNT", chatMember.role) as RequestResult)
                                )
                        }
                        .onErrorResume { error ->
                            when (error) {
                                is FailureResult -> Mono.error(error)
                                else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                            }
                        }
                }
            }
    }

    //Операции только с участниками чатов
    @Transactional
    fun addMemberToChat(memberInfo: MemberCreationRequest, chatId: UUID, userInitiatorId: UUID): Mono<RequestResult> {
        val someMemberId = memberInfo.someMemberId
        val newRole = memberInfo.role
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(chatMembersRepository.getById(someMemberId))
            .flatMap { oldChatMember ->
                val userId = oldChatMember.userId
                chatMembersRepository.save(ChatMember(chatId, userId, newRole))
            }
            .thenReturn(SuccessResult() as RequestResult)
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    @Transactional
    fun updateChatMemberToChat(chatMember: ChatMember, userInitiatorId: UUID): Mono<RequestResult> {
        val chatId = chatMember.chatId!!
        val newRole = chatMember.role
        val memberId = chatMember.id!!
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(chatMembersRepository.getById(memberId))
            .switchIfEmpty {
                Mono.error(NotFoundException("Member not found"))
            }
            .flatMap { fetchedChatMember ->
                if (newRole == ChatRole.MEMBER && fetchedChatMember.role == ChatRole.ADMIN) {
                    isMemberSingleAdmin(chatId, fetchedChatMember)
                } else {
                    Mono.empty()
                }
            }
            .then(chatMembersRepository.update(memberId, newRole))
            .thenReturn(SuccessResult() as RequestResult)
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    @Transactional
    fun addUserToChat(request: MemberFromUserCreationRequest, chatId: UUID, userInitiatorId: UUID): Mono<RequestResult> {
        val email = request.email
        val role = request.role
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(userRepository.getUserIdAndUsernameByEmail(email))
            .flatMap { pair ->
                val userId = pair.first!!
                val username = pair.second!!
                chatMembersRepository.save(ChatMember(chatId, userId, role))
                    .map { savedChatMember ->
                        MemberInfoResponse(savedChatMember.id!!, username, savedChatMember.role) as RequestResult
                    }
            }
            .switchIfEmpty(Mono.error(NotFoundException("User not found")))
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    @Transactional
    fun deleteMemberFromChat(chatId: UUID, memberToDeleteId: UUID, userInitiatorId: UUID): Mono<RequestResult> {
        return chatMembersRepository.getById(memberToDeleteId)
            .flatMap { member ->
                if (chatId != member.chatId) {
                    Mono.error(FailureResult("ChatId is wrong"))
                } else {
                    if (member.userId == userInitiatorId) {
                        quitFromChat(member)
                    } else {
                        deleteChatMemberByAnotherMemberFromChat(member, userInitiatorId)
                    }
                }
            }
            .switchIfEmpty(Mono.error(NotFoundException("Member not found")))
            .onErrorResume { error ->
                when (error) {
                    is FailureResult -> Mono.error(error)
                    else -> Mono.error(RuntimeException(error.message ?: "Unknown error"))
                }
            }
    }

    private fun deleteChatMemberByAnotherMemberFromChat(memberToDelete: ChatMember, userInitiatorId: UUID): Mono<RequestResult> {
        val chatId = memberToDelete.chatId!!
        val memberToDeleteId = memberToDelete.id!!
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(isMemberSingleAdmin(chatId, memberToDelete))
            .then(privateDeleteChatMemberFromChat(chatId, memberToDeleteId))
    }

    private fun quitFromChat(member: ChatMember): Mono<RequestResult> {
        val chatId = member.chatId!!
        return isMemberSingleAdmin(chatId, member)
            .then(privateDeleteChatMemberFromChat(chatId, member.id!!))
            .thenReturn(SuccessResult() as RequestResult)
    }

    private fun getChatInfoByChatId(chatId: UUID, userId: UUID): Mono<RequestResult>{
        val chat = chatRepository.findById(chatId)
        val chatMembersQuantity = chatMembersRepository.countByChatId(chatId)
        val chatMember = chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
        return Mono.zip(chat, chatMembersQuantity, chatMember)
            .map { tuple ->
                val fetchedChat = tuple.t1
                val memberQuantity = tuple.t2.toInt()
                val fetchedChatMember = tuple.t3
                ChatInfoResponse(fetchedChat.id!!, fetchedChatMember.id!!, fetchedChat.name, memberQuantity) as RequestResult
            }
    }

    private fun privateDeleteChatMemberFromChat(chatId: UUID, memberId: UUID): Mono<RequestResult> {
        return chatMembersRepository.deleteChatMemberById(memberId)
            .then(chatMembersRepository.countByChatId(chatId))
            .flatMap { quantityOfRemained ->
                if (quantityOfRemained.toInt() == 0) {
                    chatRepository.deleteById(chatId)
                } else {
                    Mono.empty()
                }
            }
            .thenReturn(SuccessResult() as RequestResult)
    }

    private fun isMemberSingleAdmin(chatId: UUID, member: ChatMember): Mono<Void> {
        if (member.role == ChatRole.ADMIN) {
            return chatMembersRepository.getAdminCount(chatId)
                .flatMap { adminQuantity ->
                    if (adminQuantity.toInt() == 1) {
                        Mono.error(FailureResult("Chat must have at least 1 admin"))
                    } else {
                        Mono.empty()
                    }
                }
        } else {
            return Mono.empty()
        }
    }

    // Добавлено sandress2998
    private fun isUserAdminInChat(chatId: UUID, userId: UUID): Mono<Boolean> {
        return chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
            .map { chatMember ->
                println("Success: user with id $userId is an admin of chat $chatId")
                chatMember.role == ChatRole.ADMIN
            }
            .switchIfEmpty {
                println("User with id $userId isn't an admin of chat $chatId")
                Mono.just(false)
            }
            .doOnError {
                println("Error has happened in \"fun isUserAdminInChat\"")
            }
    }

    // Добавлено sandress2998
    private fun isUserMemberInChat(chatId: UUID, userId: UUID): Mono<Boolean> {
        return chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
            .map {
                println("Success: user with id $userId is a member of chat $chatId")
                true
            }
            .switchIfEmpty {
                println("User with id $userId isn't a member of chat $chatId")
                Mono.just(false)
            }
            .doOnError {
                println("Error has happened in \"fun isUserAdminInChat\"")
            }
    }
}
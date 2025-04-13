package ru.mephi.chatservice.service

import org.springframework.boot.autoconfigure.security.SecurityProperties.User
import ru.mephi.chatservice.models.entity.Chat
import ru.mephi.chatservice.models.entity.ChatMember
import ru.mephi.chatservice.repository.ChatMembersRepository
import ru.mephi.chatservice.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.chatservice.models.ActivityStatus
import ru.mephi.chatservice.models.ChatRole
import ru.mephi.chatservice.models.dto.*
import ru.mephi.chatservice.models.exception.NotFoundException
import ru.mephi.chatservice.models.exception.AccessDeniedException
import ru.mephi.chatservice.models.exception.FailureResult
import ru.mephi.chatservice.repository.UserRepository
import ru.mephi.chatservice.webclient.MessageHandlerService
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMembersRepository:ChatMembersRepository,
    private val userRepository: UserRepository,
    private val messageHandlerService: MessageHandlerService,
    private val transactionalOperator: TransactionalOperator
) {
    // Операции только с чатами
    // @Transactional только для документации, они никак не управляют транзакциями
    @Transactional
    fun createChat(chat: Chat, userInitiatorId: UUID): Mono<ChatCreationResponse> {
        return chatRepository.save(chat)
            .flatMap { savedChat ->
                chatMembersRepository.save(
                    ChatMember(savedChat.id!!, userInitiatorId, ChatRole.ADMIN)
                )

            }
            .flatMap { member ->
                messageHandlerService.createMessageReadReceipt(userInitiatorId, member.chatId)
                    .thenReturn(ChatCreationResponse(member.chatId, member.id!!))
            }
            .`as`(transactionalOperator::transactional) // Правильное применение оператора
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
            .`as`(transactionalOperator::transactional)
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
            .then(messageHandlerService.deleteChat(chatId))
            .thenReturn(SuccessResult() as RequestResult)
            .`as`(transactionalOperator::transactional)
    }

    fun getChatsInfoByUserId(userId: UUID): Flux<ChatInfoResponse> {
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMember ->
                getChatInfoByChatId(chatMember.chatId, userId)
            }
    }

    // Сделать пагинацию
    fun getChatMembersByChatId(chatId: UUID, userInitiatorId: UUID): Flux<MemberInfoResponse> {
        return isUserMemberInChat(chatId, userInitiatorId)
            .flatMapMany { isMember: Boolean ->  // Используем flatMapMany вместо flatMap, т.к. нужно вернуть Flux
                if (!isMember) {
                    Flux.error(AccessDeniedException(AccessDeniedException.Cause.NOT_MEMBER))
                } else {
                    chatMembersRepository.getChatMembersByChatId(chatId)
                        .flatMap { chatMember: ChatMember ->
                            userRepository.getUserInfoById(chatMember.userId)
                                // пропускаем тех пользователей, которых не нашли?
                                //.filter { username -> username != null}
                                .map { userInfo: UserInfo ->
                                    MemberInfoResponse(chatMember.id!!, userInfo.username!!, chatMember.role, userInfo.activity!!)
                                }
                                .switchIfEmpty(
                                    Mono.just(MemberInfoResponse(chatMember.id!!, "DELETED_ACCOUNT", chatMember.role, ActivityStatus.INACTIVE))
                                )
                        }
                }
            }
    }

    //Операции только с участниками чатов
    @Transactional
    fun addMemberToChat(memberInfo: MemberCreationRequest, chatId: UUID, userInitiatorId: UUID): Mono<MemberInfoResponse> {
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
                checkIfUserIsAlreadyMember(userId, chatId)
                    .then(chatMembersRepository.save(ChatMember(chatId, userId, newRole)))
                    .flatMap { newMember ->
                        messageHandlerService.createMessageReadReceipt(userId, chatId)
                            .thenReturn(newMember)
                    }
            }
            .flatMap { member ->
                userRepository.getUserInfoById(member.userId)
                    .map { userInfo ->
                        MemberInfoResponse(member.id!!, userInfo.username!!, member.role, userInfo.activity!!)
                    }
                    .switchIfEmpty(Mono.error(NotFoundException("User not found")))
            }
            .`as`(transactionalOperator::transactional)
    }

    @Transactional
    fun updateChatMemberToChat(chatId: UUID, memberId: UUID, newRole: ChatRole, userInitiatorId: UUID): Mono<RequestResult> {
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
            .`as`(transactionalOperator::transactional)
    }

    @Transactional
    fun addUserToChat(request: MemberFromUserCreationRequest, chatId: UUID, userInitiatorId: UUID): Mono<MemberInfoResponse> {
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
            .then(userRepository.getUserInfoByEmail(email))
            .flatMap { userInfoExpanded ->
                val userId = userInfoExpanded.id
                if (userId == null) {
                    Mono.error(NotFoundException("User not found"))
                } else {
                    val username = userInfoExpanded.username!!
                    val activityStatus = userInfoExpanded.activity!!
                    checkIfUserIsAlreadyMember(userId, chatId)
                        .then(chatMembersRepository.save(ChatMember(chatId, userId, role)))
                        .flatMap { savedChatMember ->
                            messageHandlerService.createMessageReadReceipt(userId, chatId)
                                .thenReturn(MemberInfoResponse(savedChatMember.id!!, username, savedChatMember.role, activityStatus))
                        }
                }
            }
            .switchIfEmpty(Mono.error(NotFoundException("User not found")))
            .`as`(transactionalOperator::transactional)
    }

    @Transactional
    fun deleteMemberFromChat(chatId: UUID, memberToDeleteId: UUID, userInitiatorId: UUID): Mono<RequestResult> {
        return chatMembersRepository.getById(memberToDeleteId)
            .flatMap { member ->
                if (chatId != member.chatId) {
                    Mono.error(NotFoundException("ChatId is wrong"))
                } else {
                    if (member.userId == userInitiatorId) {
                        quitFromChat(member)
                    } else {
                        deleteChatMemberByAnotherMemberFromChat(member, userInitiatorId)
                    }
                }.thenReturn(SuccessResult() as RequestResult)
            }
            .switchIfEmpty(Mono.error(NotFoundException("Member not found")))
            .`as`(transactionalOperator::transactional)
    }

    fun getUserRoleInChat(chatId: UUID, userId: UUID): Mono<UserRoleInChat> {
        return chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
            .map { member -> UserRoleInChat(member.role) }
            .switchIfEmpty( Mono.just(UserRoleInChat(ChatRole.NOT_MEMBER)) )
    }

    fun getChatsId(userId: UUID): Flux<ChatId> {
        return chatMembersRepository.getChatsIdByUserId(userId)
            .flatMap { chatId -> Mono.just(ChatId(chatId)) }
    }

    fun getUsersIdByChatId(chatId: UUID): Flux<UserId> {
        return chatMembersRepository.getUsersIdByChatId(chatId)
            .flatMap { userId -> Mono.just(UserId(userId)) }
    }

    private fun deleteChatMemberByAnotherMemberFromChat(memberToDelete: ChatMember, userInitiatorId: UUID): Mono<Void> {
        val chatId = memberToDelete.chatId
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
            .then(messageHandlerService.deleteMessageReadReceipt(memberToDelete.userId, chatId))
            .then()
    }

    private fun quitFromChat(memberToDelete: ChatMember): Mono<Void> {
        val chatId = memberToDelete.chatId
        return isMemberSingleAdmin(chatId, memberToDelete)
            .then(privateDeleteChatMemberFromChat(chatId, memberToDelete.id!!))
            .then(messageHandlerService.deleteMessageReadReceipt(memberToDelete.userId, chatId))
            .then()
    }

    private fun getChatInfoByChatId(chatId: UUID, userId: UUID): Mono<ChatInfoResponse> {
        val chat = chatRepository.findById(chatId)
        val chatMembersQuantity = chatMembersRepository.countByChatId(chatId)
        val chatMember = chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
        return Mono.zip(chat, chatMembersQuantity, chatMember)
            .map { tuple ->
                val fetchedChat = tuple.t1
                val memberQuantity = tuple.t2.toInt()
                val fetchedChatMember = tuple.t3
                ChatInfoResponse(fetchedChat.id!!, fetchedChatMember.id!!, fetchedChat.name, memberQuantity) //as RequestResult
            }
    }

    private fun privateDeleteChatMemberFromChat(chatId: UUID, memberId: UUID): Mono<Void> {
        return chatMembersRepository.deleteChatMemberById(memberId)
            .then(chatMembersRepository.countByChatId(chatId))
            .flatMap { quantityOfRemained ->
                if (quantityOfRemained.toInt() == 0) {
                    chatRepository.deleteById(chatId)
                } else {
                    Mono.empty()
                }
            }
    }

    private fun isMemberSingleAdmin(chatId: UUID, member: ChatMember): Mono<Void> {
        return if (member.role == ChatRole.ADMIN) {
            chatMembersRepository.getAdminCount(chatId)
                .flatMap { adminQuantity ->
                    if (adminQuantity.toInt() == 1) {
                        Mono.error(FailureResult("Chat must have at least 1 admin"))
                    } else {
                        Mono.empty()
                    }
                }
        } else {
            Mono.empty()
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

    private fun checkIfUserIsAlreadyMember(userId: UUID, chatId: UUID): Mono<Void> {
        return chatMembersRepository.getChatMembersByChatId(chatId)
            .flatMap { member ->
                if (member.userId == userId) {
                    Mono.error<FailureResult>(FailureResult("User is already in chat"))
                } else {
                    Mono.empty()
                }
            }
            .then()
    }
}
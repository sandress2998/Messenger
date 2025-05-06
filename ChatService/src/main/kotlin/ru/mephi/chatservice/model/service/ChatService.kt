package ru.mephi.chatservice.model.service

import ru.mephi.chatservice.database.entity.Chat
import ru.mephi.chatservice.database.entity.ChatMember
import ru.mephi.chatservice.database.repository.ChatMembersRepository
import ru.mephi.chatservice.database.repository.ChatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import ru.mephi.chatservice.model.ActivityStatus
import ru.mephi.chatservice.model.ChatAction
import ru.mephi.chatservice.model.ChatMemberAction
import ru.mephi.chatservice.model.ChatRole
import ru.mephi.chatservice.model.dto.kafka.UserAction
import ru.mephi.chatservice.model.dto.kafka.UserActionForChatMembersIngoingMessage
import ru.mephi.chatservice.model.dto.rest.*
import ru.mephi.chatservice.model.exception.NotFoundException
import ru.mephi.chatservice.model.exception.AccessDeniedException
import ru.mephi.chatservice.model.exception.FailureResult
import ru.mephi.chatservice.database.repository.UserRepository
import ru.mephi.chatservice.webclient.MessageHandlerService
import ru.mephi.chatservice.webclient.UserService
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMembersRepository: ChatMembersRepository,
    private val userRepository: UserRepository,
    private val messageHandlerService: MessageHandlerService,
    private val chatNotificationService: ChatNotificationService,
    private val transactionalOperator: TransactionalOperator,
    private val activityService: ActivityService,
    private val userService: UserService
) {
    // ВНИМАНИЕ! В НЕКОТОРЫХ ФУНКЦИЯХ ПОСТАВЛЕНЫ ЗАГЛУШКИ С ActivityStatus.ACTIVE

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
                    .then(activityService.addToChat(userInitiatorId, member.chatId))
                    .thenReturn(ChatCreationResponse(member.chatId, member.id!!))
            }
            .`as`(transactionalOperator::transactional) // Правильное применение оператора
    }

    @Transactional
    fun updateChat(chat: Chat, chatId: UUID, userId: UUID): Mono<RequestResult> {
        val name = chat.name
        return isUserAdminInChat(chatId, userId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    chatRepository.update(chatId, name)
                }
            }
            .then(chatMembersRepository.countByChatId(chatId))
            .flatMap { membersQuantity ->
                val chatInfo = ChatInfo(null, chat.name,  membersQuantity.toInt())
                chatNotificationService.notifyAboutChatAction(chatId, ChatAction.UPDATED, chatInfo)
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
            .then(chatNotificationService.notifyAboutChatAction(chatId, ChatAction.DELETED))
            .then(activityService.deleteChat(chatId))
            .thenReturn(SuccessResult() as RequestResult)
            .`as`(transactionalOperator::transactional)
    }

    fun getChatsInfoByUserId(userId: UUID): Flux<ChatInfo> {
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMember ->
                getChatInfoByChatId(chatMember.chatId, userId)
            }
    }

    // Сделать пагинацию
    fun getChatMembersByChatId(chatId: UUID, userInitiatorId: UUID): Flux<MemberInfo> {
        return isUserMemberInChat(chatId, userInitiatorId)
            .flatMapMany { isMember: Boolean ->  // Используем flatMapMany вместо flatMap, т.к. нужно вернуть Flux
                if (!isMember) {
                    Flux.error(AccessDeniedException(AccessDeniedException.Cause.NOT_MEMBER))
                } else {
                    activityService.getActiveMembers(chatId)
                        .flatMapMany { activeMembers ->
                            chatMembersRepository.getChatMembersByChatId(chatId)
                                .flatMap { chatMember: ChatMember ->
                                    val userId = chatMember.userId
                                    userRepository.getUsernameById(userId)
                                        .map { username ->
                                            val activityStatus = when (activeMembers.contains(userId)) {
                                                true -> ActivityStatus.ACTIVE
                                                false -> ActivityStatus.INACTIVE
                                            }
                                            MemberInfo(chatMember.id!!, username!!, chatMember.role, activityStatus)
                                        }
                                }
                        }

                }
            }
    }

    /* удалено, т.к. теперь будет единственный способ добавления пользователя в чат
    //Операции только с участниками чатов
    @Transactional
    fun addMemberToChat(memberInfo: MemberCreationRequest, chatId: UUID, userInitiatorId: UUID): Mono<MemberInfo> {
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
                val memberId = member.id!!
                val role = member.role
                val userId = member.userId
                userRepository.getUsernameById(userId)
                    .switchIfEmpty(Mono.error(NotFoundException("User not found")))
                    .flatMap { username ->
                        activityService.getUserActivityStatus(userId)
                            .flatMap { activityStatus ->
                                val info = MemberInfo(memberId, username!!, role, activityStatus)
                                println("Activity status of user $userId is $activityStatus")
                                when (activityStatus) {
                                    ActivityStatus.ACTIVE -> activityService.addToChat(member.userId, chatId)
                                    ActivityStatus.INACTIVE -> Mono.empty()
                                }
                                .then(chatNotificationService.notifyAboutChatMemberAction(chatId, member.id, ChatMemberAction.NEW, info))
                                .thenReturn(info)
                            }
                    }

            }
            .`as`(transactionalOperator::transactional)
    }
     */

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
                .then(chatMembersRepository.update(memberId, newRole))
                .then(userRepository.getUsernameById(fetchedChatMember.userId))
                .flatMap { username ->
                    val memberInfo = MemberInfo(null, username!!, newRole)
                    chatNotificationService.notifyAboutChatMemberAction(chatId, memberId, ChatMemberAction.UPDATED, memberInfo)
                }
            }
            .thenReturn(SuccessResult() as RequestResult)
            .`as`(transactionalOperator::transactional)
    }

    @Transactional
    fun addUserToChat(request: MemberFromUserCreationRequest, chatId: UUID, userInitiatorId: UUID): Mono<MemberInfo> {
        val (tag, role) = request
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(userRepository.getUserIdAndUsernameByTag(tag))
            .flatMap { pair ->
                val userId = pair.first
                if (userId == null) {
                    Mono.error(NotFoundException("User not found"))
                } else {
                    val username = pair.second!!
                    checkIfUserIsAlreadyMember(userId, chatId)
                        .then(chatMembersRepository.save(ChatMember(chatId, userId, role)))
                        .flatMap { savedChatMember ->
                            val memberId = savedChatMember.id!!
                            messageHandlerService.createMessageReadReceipt(userId, chatId)
                                .then(activityService.getUserActivityStatus(userId))
                                    .flatMap { activityStatus ->
                                        val memberInfo = MemberInfo(null, username, role, activityStatus)
                                        when (activityStatus) {
                                            ActivityStatus.ACTIVE -> activityService.addToChat(userId, chatId)
                                            ActivityStatus.INACTIVE -> Mono.empty()
                                        }
                                        .then(chatNotificationService.notifyAboutChatMemberAction(chatId, memberId, ChatMemberAction.NEW, memberInfo))
                                        .thenReturn(memberInfo.copy(memberId = memberId))
                                    }
                        }
                }
            }
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
                }
                .then(chatNotificationService.notifyAboutChatMemberAction(chatId, memberToDeleteId, ChatMemberAction.DELETED))
                .then(activityService.deleteFromChat(member.userId, chatId))
                .thenReturn(SuccessResult() as RequestResult)
            }
            .switchIfEmpty(Mono.error(NotFoundException("Member not found")))
            .`as`(transactionalOperator::transactional)
    }

    fun getChatMemberId(chatId: UUID, userId: UUID): Mono<ChatMemberInfo> {
        return chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
            .map { member -> ChatMemberInfo(member.id!!) }
            .switchIfEmpty( Mono.error(NotFoundException("User is not member of chat $chatId")) )
    }

    fun getChatsId(userId: UUID): Flux<ChatId> {
        return chatMembersRepository.getChatsIdByUserId(userId)
            .flatMap { chatId -> Mono.just(ChatId(chatId)) }
    }

    fun getUserInfoByMemberId(chatId: UUID, memberId: UUID, userInitiatorId: UUID): Mono<UserInfo> {
        return isUserMemberInChat(chatId, userInitiatorId)
            .flatMap { isMember ->
                if (isMember) {
                    chatMembersRepository.getById(memberId)
                        .flatMap { member ->
                            val userId = member.userId
                            userService.getUserInfo(userId)
                        }
                        .switchIfEmpty(Mono.error(NotFoundException("Member not found")))
                } else {
                    Mono.error(AccessDeniedException(AccessDeniedException.Cause.NOT_MEMBER))
                }
            }
    }

    @Transactional
    fun handleUserActionNotification(userActionIngoingMessage: UserActionForChatMembersIngoingMessage): Mono<Void> {
        val (userId, action) = userActionIngoingMessage
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMember ->
                val (chatId, _, role, memberId) = chatMember
                when (action) {
                    UserAction.UPDATED -> {
                        userRepository.getUsernameById(userId)
                            .flatMap { username ->
                                val memberInfo = MemberInfo(null, username!!, role, null)
                                chatNotificationService.notifyAboutChatMemberAction(chatId, memberId!!, ChatMemberAction.UPDATED, memberInfo)
                            }
                    }
                    // для каждого чата надо удалить пользователя
                    UserAction.DELETED -> {
                        if (chatMember.role != ChatRole.ADMIN) {
                            deleteMemberFromChat(chatId, memberId!!, userId)
                        } else {
                            deleteAdminAndSelectNewAdmin(chatMember)
                        }
                    }
                }
            }
            .then()
    }

    private fun deleteAdminAndSelectNewAdmin(memberToDelete: ChatMember): Mono<Void> {
        val (chatId, userId, _, memberId) = memberToDelete
        return chatMembersRepository.countByChatId(chatId)
            .flatMap { membersQuantity ->
                if (membersQuantity.toInt() == 1) {
                    chatRepository.deleteById(chatId)
                        .then(chatMembersRepository.deleteById(memberId!!))
                } else {
                    chatMembersRepository.getAdminCount(chatId)
                        .flatMap { adminQuantity ->
                            chatMembersRepository.deleteById(memberId!!)
                            .then(activityService.deleteFromChat(userId, chatId))
                            .then(Mono.defer {
                                if (adminQuantity.toInt() == 1) {
                                    chatMembersRepository.promoteRandomMemberToAdmin(chatId)
                                        .flatMap { selectedMember ->
                                            userRepository.getUsernameById(selectedMember.userId)
                                                .flatMap { username ->
                                                    val memberInfo = MemberInfo(selectedMember.id!!, username!!, ChatRole.ADMIN)
                                                    chatNotificationService.notifyAboutChatMemberAction(chatId, memberId, ChatMemberAction.UPDATED, memberInfo)
                                                }
                                        }
                                } else {
                                    Mono.empty()
                                }
                            })
                            .then(Mono.defer {
                                chatNotificationService.notifyAboutChatMemberAction(chatId, memberId, ChatMemberAction.DELETED)
                            })
                        }
                }
            }
            .then()
    }

    private fun deleteChatMemberByAnotherMemberFromChat(memberToDelete: ChatMember, userInitiatorId: UUID): Mono<Void> {
        val (chatId, _, _, memberToDeleteId) = memberToDelete
        return isUserAdminInChat(chatId, userInitiatorId)
            .flatMap { isAdmin: Boolean ->
                if (!isAdmin) {
                    Mono.error<AccessDeniedException>(AccessDeniedException(AccessDeniedException.Cause.NOT_ADMIN))
                } else {
                    Mono.empty()
                }
            }
            .then(isMemberSingleAdmin(chatId, memberToDelete))
            .then(privateDeleteChatMemberFromChat(chatId, memberToDeleteId!!))
            .then(messageHandlerService.deleteMessageReadReceipt(memberToDelete.userId, chatId))
            .then()
    }

    private fun quitFromChat(memberToDelete: ChatMember): Mono<Void> {
        val (chatId, userId, _, id) = memberToDelete
        return isMemberSingleAdmin(chatId, memberToDelete)
            .then(privateDeleteChatMemberFromChat(chatId, id!!))
            .then(messageHandlerService.deleteMessageReadReceipt(userId, chatId))
            .then()
    }

    private fun getChatInfoByChatId(chatId: UUID, userId: UUID): Mono<ChatInfo> {
        val chat = chatRepository.findById(chatId)
        val chatMembersQuantity = chatMembersRepository.countByChatId(chatId)
        val chatMember = chatMembersRepository.getChatMemberByChatIdAndUserId(chatId, userId)
        return Mono.zip(chat, chatMembersQuantity, chatMember)
            .map { tuple ->
                val fetchedChat = tuple.t1
                val memberQuantity = tuple.t2.toInt()
                //val fetchedChatMember = tuple.t3
                ChatInfo(fetchedChat.id!!, fetchedChat.name, memberQuantity) //as RequestResult
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
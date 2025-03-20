package ru.mephi.chatservice.service

import ru.mephi.chatservice.models.entity.Chat
import ru.mephi.chatservice.models.entity.ChatMember
import ru.mephi.chatservice.repository.ChatMembersRepository
import ru.mephi.chatservice.repository.ChatRepository
import ru.mephi.chatservice.models.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*

@Service
class ChatService(
    private val chatRepository: ChatRepository,
    private val chatMembersRepository:ChatMembersRepository
) {
    // Операции только с чатами
    @Transactional
    fun createChat(chat: Chat): Mono<Chat>{
        return chatRepository.save(chat)
    }

    @Transactional
    fun updateChat(chat: Chat): Mono<Chat>{
        //return chatRepository.save(chat)
        if(chat.id == null){
            return Mono.error(NotFoundException("Chat id not provided"))
        }
        return chatRepository.getChatById(id = chat.id)
            .switchIfEmpty {
                Mono.error(NotFoundException("Chat ${chat.id} not found"))
            }
    }

    @Transactional
    fun deleteChat(chatId: UUID): Mono<Void>{
        return chatRepository.deleteById(chatId).then(deleteChatMembers(chatId))
    }

    fun getChatByChatId(chatId: UUID): Mono<Chat>{
        return chatRepository.findById(chatId)
    }

    //Получение чатов для пользователя
    fun getChatsIdsByMemberId(userId: UUID): Flux<UUID>{
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMembers -> Mono.just(chatMembers.chatId) }
    }

    fun getChatsByMemberId(userId: UUID): Flux<Chat>{
        return chatMembersRepository.getChatMembersByUserId(userId)
            .flatMap { chatMembers -> chatRepository.getChatById(chatMembers.chatId) }
    }

    //Получение пользователей по чату
    fun getChatMembersIdsByChatId(chatId: UUID): Flux<UUID>{
        return chatMembersRepository.getChatMembersByChatId(chatId)
            .flatMap { chatMembers -> Mono.just(chatMembers.userId) }
    }

    fun getChatMembersByChatId(chatId: UUID): Flux<ChatMember>{
        return chatMembersRepository.getChatMembersByChatId(chatId)
    }

    //Операции только с участниками чатов
    @Transactional
    fun addChatMemberToChat(chatMember: ChatMember): Mono<ChatMember>{
        return chatMembersRepository.save(chatMember)
    }

    @Transactional
    fun updateChatMemberToChat(chatMember: ChatMember): Mono<ChatMember>{
        return chatMembersRepository.getChatMemberByChatIdAndUserId(chatId = chatMember.chatId,memberId = chatMember.userId)
            .switchIfEmpty {
                Mono.error(NotFoundException("ChatMember not found"))
            }
    }

    @Transactional
    fun deleteChatMemberFromChat(chatId: UUID, memberId: UUID): Mono<Void>{
        return chatMembersRepository.deleteChatMembersByChatIdAndUserId(chatId, memberId)
    }

    @Transactional
    fun deleteChatMembers(chatId: UUID): Mono<Void>{
        return chatMembersRepository.deleteChatMembersByChatId(chatId)
    }
}
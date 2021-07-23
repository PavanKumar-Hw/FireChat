package com.example.firechat.chat.domain

import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.chat.data.repository.ChatRepository
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatUseCaseImpl @Inject constructor(private val chatRepository: ChatRepository) :
    ChatUseCase {
    override fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChildObserver,
        b: (Result<MessageModel>) -> Unit
    ) {
        chatRepository.loadMessagesAdded(messagesID, observer, b)
    }

    override fun <T> createUsersChat(chatId: String, b: (Result<T>) -> Unit) {
        chatRepository.createUsersChat(chatId, b)
    }

    override fun <T> createReceiverChat(chatId: String, b: (Result<T>) -> Unit) {
        chatRepository.createReceiverChat(chatId, b)
    }

}
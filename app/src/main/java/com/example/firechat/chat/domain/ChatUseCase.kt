package com.example.firechat.chat.domain

import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.Result

interface ChatUseCase {
    fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChildObserver,
        b: ((Result<MessageModel>) -> Unit)
    )

    fun <T> createUsersChat(
        chatId: String,
        b: ((Result<T>) -> Unit)
    )

    fun <T> createReceiverChat(
        chatId: String,
        b: ((Result<T>) -> Unit)
    )

}
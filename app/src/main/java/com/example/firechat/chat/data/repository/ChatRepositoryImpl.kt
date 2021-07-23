package com.example.firechat.chat.data.repository

import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.FirebaseDataSource
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(val firebaseDataSource: FirebaseDataSource) :
    ChatRepository {

    override fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChildObserver,
        b: (Result<MessageModel>) -> Unit
    ) {
        firebaseDataSource.attachMessagesObserver(MessageModel::class.java, messagesID, observer, b)
    }

    override fun <T> createUsersChat(chatId: String, b: (Result<T>) -> Unit) {
        firebaseDataSource.createAndObserveChat(chatId,b)
    }

    override fun <T> createReceiverChat(chatId: String, b: (Result<T>) -> Unit) {
        TODO("Not yet implemented")
    }

}
package com.example.firechat.chat.data.repository

import android.content.Context
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.FirebaseDataSource
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(private val firebaseDataSource: FirebaseDataSource) :
    ChatRepository {

    @ApplicationContext
    private lateinit var context: Context

    override fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChildObserver,
        b: (Result<MessageModel>) -> Unit
    ) {
        firebaseDataSource.attachMessagesObserver(MessageModel::class.java, messagesID, observer, b)
    }

    override fun sendMessage(messageUserMap: HashMap<String, Any>, b: (String?) -> Unit) {
        firebaseDataSource.sendMessageToUser(messageUserMap, b)
    }

}
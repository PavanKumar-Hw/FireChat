package com.example.firechat.chats.data.repository

import android.content.Context
import com.example.firechat.chats.data.models.ChatListModel
import com.example.firechat.common.*
import com.google.firebase.database.DataSnapshot
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatsRepositoryImpl @Inject constructor(private val firebaseDataSource: FirebaseDataSource) :
    ChatsRepository {

    @ApplicationContext
    private lateinit var context: Context

    override fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChatsChildObserver,
        b: (DataSnapshot, Boolean?, String) -> Unit
    ) {
        firebaseDataSource.attachChatsObserver(messagesID, observer, b)
    }

}
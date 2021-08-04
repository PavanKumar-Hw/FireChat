package com.example.firechat.chats.data.repository

import com.example.firechat.common.FirebaseReferenceChatsChildObserver
import com.google.firebase.database.DataSnapshot

interface ChatsRepository {
    fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChatsChildObserver,
        b: ((DataSnapshot, Boolean?, String) -> Unit)
    )
}
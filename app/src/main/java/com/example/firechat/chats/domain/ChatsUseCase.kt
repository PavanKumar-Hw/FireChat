package com.example.firechat.chats.domain

import com.example.firechat.common.FirebaseReferenceChatsChildObserver
import com.google.firebase.database.DataSnapshot

interface ChatsUseCase {
    fun loadChatsAdded(
        messagesID: String,
        observer: FirebaseReferenceChatsChildObserver,
        b: ((DataSnapshot, Boolean?, String) -> Unit)
    )
}
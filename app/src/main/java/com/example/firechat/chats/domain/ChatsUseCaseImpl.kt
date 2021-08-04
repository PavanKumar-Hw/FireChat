package com.example.firechat.chats.domain

import com.example.firechat.chats.data.repository.ChatsRepository
import com.example.firechat.common.FirebaseReferenceChatsChildObserver
import com.google.firebase.database.DataSnapshot
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatsUseCaseImpl @Inject constructor(private val chatRepository: ChatsRepository) :
    ChatsUseCase {

    override fun loadChatsAdded(
        messagesID: String,
        observer: FirebaseReferenceChatsChildObserver,
        b: (DataSnapshot, Boolean?, String) -> Unit
    ) {
        chatRepository.loadMessagesAdded(messagesID, observer, b)
    }


}
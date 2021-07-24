package com.example.firechat.chat.domain

import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.FirebaseReferenceValueObserver
import com.example.firechat.common.Result
import com.google.firebase.database.DataSnapshot
import java.util.HashMap

interface ChatUseCase {
    fun loadMessagesAdded(
        messagesID: String,
        observer: FirebaseReferenceChildObserver,
        b: ((Result<MessageModel>) -> Unit)
    )

    fun sendMessage(
        messageUserMap: HashMap<String, Any>,
        b: ((String?) -> Unit)
    )

    fun observeSenderTypingStatus(
        refPath: String,
        typingObserver: FirebaseReferenceValueObserver,
        b: (DataSnapshot?, Boolean) -> Unit
    )

    fun observeActiveStatus(
        refPath: String,
        activeStateObserver: FirebaseReferenceValueObserver,
        b: ((DataSnapshot?, Boolean) -> Unit)
    )

    fun updateUserTypingStatus(refPath: String, status: String)
}
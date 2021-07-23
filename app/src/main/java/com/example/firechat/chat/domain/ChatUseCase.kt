package com.example.firechat.chat.domain

import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.Result
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
}
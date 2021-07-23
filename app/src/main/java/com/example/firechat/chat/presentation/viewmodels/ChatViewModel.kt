package com.example.firechat.chat.presentation.viewmodels

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.example.firechat.chat.domain.ChatUseCase
import com.example.firechat.common.FirebaseReferenceChildObserver
import com.example.firechat.common.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    @Inject
    private lateinit var fbRefMessagesChildObserver: FirebaseReferenceChildObserver



    private fun <T> createUsersChat(chatId: String, receiverChatId: String) {
        chatUseCase.createUsersChat(chatId) { result: Result<T> ->
            if (result is Result.Success) {
                createReceiverChat(receiverChatId)
            } else if (result is Result.Error) {
                Toast.makeText(context, "Chat not created", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun createReceiverChat(chatId: String) {
        chatUseCase.createReceiverChat(chatId)
        { result: Result<Any> ->
            if (result is Result.Error) {
                Toast.makeText(context, "Chat not created", Toast.LENGTH_LONG).show()
            }
        }
    }
}
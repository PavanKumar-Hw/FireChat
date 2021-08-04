package com.example.firechat.chats.presentation.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firechat.chats.data.models.ChatListModel
import com.example.firechat.chats.domain.ChatsUseCase
import com.example.firechat.common.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatsViewModel @Inject constructor(
    private val chatsUseCase: ChatsUseCase,
    private val fbReferenceChatsChildObserver: FirebaseReferenceChatsChildObserver
) : ViewModel() {

    var messageList = MediatorLiveData<MutableList<ChatListModel>>()

    private val _addedMessage = MutableLiveData<ChatListModel>()
    val userIds: ArrayList<String> = ArrayList()

    fun loadChats(messagePath: String) {
        messageList.addSource(_addedMessage) {
            messageList.addNewItem(it)
        }

        chatsUseCase.loadChatsAdded(
            messagePath,
            fbReferenceChatsChildObserver
        ) { result, b, userId ->
            wrapSnapshotToClass(
                ChatListModel::class.java,
                result
            )?.let {
                if (b != null) {
                    if (b) {
                        _addedMessage.value = it
                        userIds.add(userId)
                    } else {
                        messageList.updateItemAt(it, userIds.indexOf(userId))
                    }
                } else {
                    messageList.removeItem(it)
                    userIds.remove(userId)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fbReferenceChatsChildObserver.clear()
    }
}
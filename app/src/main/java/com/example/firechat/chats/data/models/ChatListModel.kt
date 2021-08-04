package com.example.firechat.chats.data.models

import com.example.firechat.common.NodeNames
import com.google.firebase.database.PropertyName

class ChatListModel(
    @get:PropertyName(NodeNames.USER_ID) @set:PropertyName(NodeNames.USER_ID)
    var userId: String? = "",
    @get:PropertyName(NodeNames.USER_NAME) @set:PropertyName(NodeNames.USER_NAME)
    var userName: String? = "",
    @get:PropertyName(NodeNames.PHOTO_PATH) @set:PropertyName(NodeNames.PHOTO_PATH)
    var photoPath: String? = "",
    @get:PropertyName(NodeNames.UNREAD_COUNT) @set:PropertyName(NodeNames.UNREAD_COUNT)
    var unreadCount: Int = 0,
    @get:PropertyName(NodeNames.LAST_MESSAGE) @set:PropertyName(NodeNames.LAST_MESSAGE)
    var lastMessage: String? = "",
    @get:PropertyName(NodeNames.LAST_MESSAGE_TIME) @set:PropertyName(NodeNames.LAST_MESSAGE_TIME)
    var lastMessageTime: Any? = null,
    @get:PropertyName(NodeNames.TIME_STAMP) @set:PropertyName(NodeNames.TIME_STAMP)
    var timeStamp: Any? = null,
    @get:PropertyName(NodeNames.TYPING) @set:PropertyName(NodeNames.TYPING)
    var typing: String? = ""
)
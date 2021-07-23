package com.example.firechat.chat.data.models

import com.example.firechat.NodeNames
import com.google.firebase.database.PropertyName

data class MessageModel(
    @get:PropertyName(NodeNames.MESSAGE) @set:PropertyName(NodeNames.MESSAGE)
    var message: String? = "",
    @get:PropertyName(NodeNames.MESSAGE_FROM) @set:PropertyName(NodeNames.MESSAGE_FROM)
    var messageFrom: String? = "",
    @get:PropertyName(NodeNames.MESSAGE_TO) @set:PropertyName(NodeNames.MESSAGE_TO)
    var messageTo: String? = "",
    @get:PropertyName(NodeNames.MESSAGE_ID) @set:PropertyName(NodeNames.MESSAGE_ID)
    var messageId: String? = "",
    @get:PropertyName(NodeNames.MESSAGE_TIME) @set:PropertyName(NodeNames.MESSAGE_TIME)
    var messageTime: Any? = null,
    @get:PropertyName(NodeNames.MESSAGE_TYPE) @set:PropertyName(NodeNames.MESSAGE_TYPE)
    var messageType: String? = "",
    @get:PropertyName(NodeNames.LOCATION) @set:PropertyName(NodeNames.LOCATION)
    var location: Location? = null,
    @get:PropertyName(NodeNames.REQ_OPTIONS) @set:PropertyName(NodeNames.REQ_OPTIONS)
    var requestOptions: RequestOptions? = null,
    @get:PropertyName(NodeNames.IMAGE_PATH) @set:PropertyName(NodeNames.IMAGE_PATH)
    var imagePath: String? = "",
    @get:PropertyName(NodeNames.VIDEO_PATH) @set:PropertyName(NodeNames.VIDEO_PATH)
    var videoPath: String? = ""
)
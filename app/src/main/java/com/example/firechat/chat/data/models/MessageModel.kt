package com.example.firechat.chat.data.models

import com.google.firebase.database.PropertyName

data class MessageModel(
    @get:PropertyName("message") @set:PropertyName("message")
    var message: String? = "",
    @get:PropertyName("messageFrom") @set:PropertyName("messageFrom")
    var messageFrom: String? = "",
    @get:PropertyName("messageTo") @set:PropertyName("messageTo")
    var messageTo: String? = "",
    @get:PropertyName("messageId") @set:PropertyName("messageId")
    var messageId: String? = "",
    @get:PropertyName("messageTime") @set:PropertyName("messageTime")
    var messageTime: Long = 0,
    @get:PropertyName("messageType") @set:PropertyName("messageType")
    var messageType: String? = "",
    @get:PropertyName("location") @set:PropertyName("location")
    var location: Location? = null,
    @get:PropertyName("requestOptions") @set:PropertyName("requestOptions")
    var requestOptions: RequestOptions? = null,
    @get:PropertyName("imagePath") @set:PropertyName("imagePath")
    var imagePath: String? = "",
    @get:PropertyName("videoPath") @set:PropertyName("videoPath")
    var videoPath: String? = ""
)
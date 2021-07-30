package com.example.firechat.chat.data.entities

import androidx.room.Entity

@Entity
data class LocRequestNotifications(
    val messageId: String,
    var messageTime: Long? = null,
    val messageFrom: String,
    val messageTo: String,
    val messageType: String,
    val fromUserName: String,
    val toUserName: String
)
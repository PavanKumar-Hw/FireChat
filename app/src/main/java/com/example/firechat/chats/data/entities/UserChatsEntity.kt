package com.example.firechat.chats.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.firechat.contacts.data.entities.UserContactEntity

@Entity(
    foreignKeys = [ForeignKey(
        entity = UserContactEntity::class,
        parentColumns = ["contactId"],
        childColumns = ["chatContactId"]
    )]
)
data class UserChatsEntity(
    @PrimaryKey
    @ColumnInfo(name = "chatRootId")
    var chatRootId: String,
    @ColumnInfo(name = "chatContactId")
    var chatContactId: String,
    var lastMessage: String,
    var lastSeen: Long,
    var timeStamp: Long,
    var unreadCount: Int,
    var lastMessageType: String,
)

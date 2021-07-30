package com.example.firechat.chat.data.entities

import androidx.room.*
import com.example.firechat.chat.data.entities.typeconverters.LocationConverter
import com.example.firechat.chat.data.entities.typeconverters.RequestOptionsConverter
import com.example.firechat.chat.data.models.Location
import com.example.firechat.chat.data.models.RequestOptions
import com.example.firechat.chats.data.entities.UserChatsEntity

@Entity(
    indices = [Index(
        value = ["messageId"],
        unique = true
    )],
    foreignKeys = [ForeignKey(
        entity = UserChatsEntity::class,
        parentColumns = ["chatRootId"],
        childColumns = ["messageRootId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MessagesEntity(
    @PrimaryKey
    @ColumnInfo(name = "messageId")
    var messageId: String,
    @ColumnInfo(name = "messageRootId")
    var messageRootId: String,
    var message: String,
    var messageTo: String,
    var messageFrom: String,
    var messageType: String,
    var imagePath: String,
    var videoPath: String,
    @TypeConverters(RequestOptionsConverter::class)
    var requestOptions: RequestOptions,
    @TypeConverters(LocationConverter::class)
    var location: Location,
    var messageTime: Any? = null,
    var isDeleted: Boolean
)

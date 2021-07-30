package com.example.firechat.chat.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.Index

@Entity(
    foreignKeys = [ForeignKey(
        entity = MessagesEntity::class,
        parentColumns = ["messageId"],
        childColumns = ["syncMessageId"],
        onDelete = CASCADE
    )],
    indices = [Index(value = ["syncMessageId"], unique = true)]
)
data class MessagesRefEntity(
    val message: String,
    val messageTo: String,
    @ColumnInfo(name = "syncMessageId")
    val syncMessageId: String,
    val messageFrom: String
)
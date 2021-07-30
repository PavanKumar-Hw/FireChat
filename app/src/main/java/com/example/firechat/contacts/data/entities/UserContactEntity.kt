package com.example.firechat.contacts.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UserContactEntity(
    @PrimaryKey
    var contactId: String,
    var contactName: String,
    var contactInfo: String,
    var contactProfilePic: String,
    var contactOnlineStatus: String,
    var contactLastSeen: String
)
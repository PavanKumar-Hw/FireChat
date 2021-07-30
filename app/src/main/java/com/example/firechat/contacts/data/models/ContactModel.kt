package com.example.firechat.contacts.data.models

import java.io.Serializable

class ContactModel(
    var contactId: String,
    var contactName: String,
    var contactInfo: String,
    var contactProfilePic: String,
    var contactOnlineStatus: String,
    var contactLastSeen: String
) : Serializable

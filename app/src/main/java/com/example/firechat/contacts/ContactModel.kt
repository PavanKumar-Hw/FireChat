package com.example.firechat.contacts

import java.io.Serializable

class ContactModel(
    val userId: String,
    val userName: String,
    val profileImage: String
) : Serializable

package com.example.firechat.chat.data.models

import com.google.firebase.database.PropertyName

data class RequestOptions(
    @get:PropertyName("requestStatus")
    @set:PropertyName("requestStatus")
    var requestStatus: String? = "",
    @get:PropertyName("requestExpiry")
    @set:PropertyName("requestExpiry")
    var requestExpiry: String? = ""
)

package com.example.firechat.chat.data.models

import com.example.firechat.NodeNames
import com.google.firebase.database.PropertyName

data class RequestOptions(
    @get:PropertyName(NodeNames.REQ_STATUS)
    @set:PropertyName(NodeNames.REQ_STATUS)
    var requestStatus: String? = "",
    @get:PropertyName(NodeNames.REQ_EXPIRY)
    @set:PropertyName(NodeNames.REQ_EXPIRY)
    var requestExpiry: String? = ""
)

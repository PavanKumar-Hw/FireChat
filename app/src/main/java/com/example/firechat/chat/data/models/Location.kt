package com.example.firechat.chat.data.models

import com.example.firechat.common.NodeNames
import com.google.firebase.database.PropertyName

data class Location(
    @get:PropertyName(NodeNames.LOCATION_CURRENT) @set:PropertyName(NodeNames.LOCATION_CURRENT)
    var requestingUserLoc: String? = "",
    @get:PropertyName(NodeNames.LOCATION_CHAT) @set:PropertyName(NodeNames.LOCATION_CHAT)
    var senderUserLoc: String? = "",
    @get:PropertyName(NodeNames.LOCATION_HALFWAY) @set:PropertyName(NodeNames.LOCATION_HALFWAY)
    var halfwayPoint: String? = ""
)

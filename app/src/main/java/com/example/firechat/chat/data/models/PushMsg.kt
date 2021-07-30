package com.example.firechat.chat.data.models


import com.google.gson.annotations.SerializedName

data class PushMsg(
    @SerializedName("from")
    val from: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("to")
    val to: String,
    @SerializedName("type")
    val type: String
)
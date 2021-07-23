package com.example.firechat.common

class Constants {
    companion object {

        const val SEND_LOC_REQ: Int = 1
        const val ACCEPT_LOC_REQ: Int = 1

        const val SENDER_ID: String = "282333194999"

        const val FIREBASE_KEY: String =
            "AAAAQbxeqvc:APA91bERmwmkbC1NxkQl-ZHiOz0s3kMoAv0DBLMZBhl-CS9Y5xVg9cfUcPlJukdkK3kA8H3rOS1S9WHcdQhwuoiQbrr2bBuO0xSo3v5PhszfFueelvQd6SstFJiasIafoiqnCA_fz1YZ"

        var FIRE_BASE_TOKEN = ""
        const val STATUS_ONLINE = "online"
        const val STATUS_OFFLINE = "offline"
        const val STATUS_TYPING = "typing..."

        const val TYPING_STOPPED = "0"
        const val TYPING_STARTED = "1"
        const val REQUEST_STATUS_CANCELLED = "cancelled"
        const val REQUEST_STATUS_REJECTED = "rejected"
        const val REQUEST_STATUS_ACCEPTED = "accepted"

        const val MESSAGE_TYPE_TEXT = "text"
        const val MESSAGE_TYPE_IMAGE = "image"
        const val MESSAGE_TYPE_VIDEO = "video"
        const val MESSAGE_TYPE_LOC = "location"
        const val MESSAGE_TYPE_LOC_REQ = "locationReq"

        const val NOTIFICATION_TITLE = "title"
        const val NOTIFICATION_MESSAGE = "message"
        const val NOTIFICATION_FROM = "from"

        const val currentUserId = "jwUOcUWEnJWgiGELCqOcQxNga5Y2"

        const val NOTIFICATION_TO = "to"
        const val NOTIFICATION_DATA = "data"

        var IsLocPermissionGranted = false

        const val MESSAGE_HOLDER_TYPE_TEXT = 1
        const val MESSAGE_HOLDER_TYPE_LOC_REQ = 2
        const val MESSAGE_HOLDER_TYPE_LOC = 3

        const val CHANNEL_ID = "halfway_app"
        const val CHANNEL_NAME = "halfway_app_notifications"
        const val CHANNEL_DESC = "Halfway notifications"
    }
}
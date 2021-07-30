package com.example.firechat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.chat.data.models.PushMsg
import com.example.firechat.chat.presentation.activities.ChatActivity
import com.example.firechat.common.Constants
import com.example.firechat.common.Extras
import com.example.firechat.common.Util
import com.example.firechat.common.fromString
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class FireBaseInstanceIdGenerator : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Constants.FIRE_BASE_TOKEN = p0
        Util.updateDeviceToken(null, p0)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val msg: PushMsg? = remoteMessage.data[Constants.NOTIFICATION_DATA]?.let {
            Gson().fromString(
                it,
                object : TypeToken<PushMsg>() {}.type
            )
        } as PushMsg?
        val message: MessageModel? = msg?.message?.let {
            Gson().fromString(
                it,
                object : TypeToken<MessageModel>() {}.type
            )
        } as MessageModel?

        val intentChat = Intent(this, ChatActivity::class.java)
        intentChat.putExtra(Extras.USER_KEY, message?.messageFrom)
        intentChat.putExtra(Extras.USER_NAME, message?.message)
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intentChat, PendingIntent.FLAG_ONE_SHOT)

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder: NotificationCompat.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.CHANNEL_ID,
                Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = Constants.CHANNEL_DESC
            notificationManager.createNotificationChannel(channel)
            notificationBuilder = NotificationCompat.Builder(this, Constants.CHANNEL_ID)
        } else {
            notificationBuilder = NotificationCompat.Builder(this)
        }

        notificationBuilder.setSmallIcon(R.drawable.default_profile)
        notificationBuilder.color = resources.getColor(R.color.purple_700)
        notificationBuilder.setContentTitle(msg?.title)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setSound(defaultSoundUri)
        notificationBuilder.setContentIntent(pendingIntent)

        if (message?.imagePath?.startsWith("https://firebasestorage.") == true) {
            try {
                val bigPictureStyle = NotificationCompat.BigPictureStyle()
                Glide.with(this)
                    .asBitmap()
                    .load(message)
                    .into(object : CustomTarget<Bitmap?>(200, 100) {
                        override fun onLoadCleared(placeholder: Drawable?) {}
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap?>?
                        ) {
                            bigPictureStyle.bigPicture(resource)
                            notificationBuilder.setStyle(bigPictureStyle)
                            notificationManager.notify(999, notificationBuilder.build())
                        }
                    })
            } catch (ex: Exception) {
                notificationBuilder.setContentText("New File Received")
            }
        } else {
            notificationBuilder.setContentText(message?.message)
            notificationManager.notify(999, notificationBuilder.build())
        }
    }
}
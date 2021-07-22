package com.example.firechat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.firechat.chat.presentation.activities.ChatActivity
import com.example.firechat.common.Constants
import com.example.firechat.common.Extras
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FireBaseInstanceIdGenerator : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Constants.FIRE_BASE_TOKEN = p0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val title: String? = remoteMessage.data[Constants.NOTIFICATION_TITLE]
        val message: String? = remoteMessage.data[Constants.NOTIFICATION_MESSAGE]

        val intentChat = Intent(this, ChatActivity::class.java)
        intentChat.putExtra(Extras.USER_KEY, remoteMessage.data[Constants.NOTIFICATION_FROM])
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
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setSound(defaultSoundUri)
        notificationBuilder.setContentIntent(pendingIntent)

        if (message!!.startsWith("https://firebasestorage.")) {
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
            notificationBuilder.setContentText(message)
            notificationManager.notify(999, notificationBuilder.build())
        }
    }
}
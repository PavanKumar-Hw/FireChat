package com.example.firechat.common

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.firechat.R
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.chats.data.models.ChatListModel
import com.example.firechat.fcmPushNotification.FCMSender
import com.google.firebase.database.*
import com.google.gson.Gson
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object Util {

    fun connectionAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (connectivityManager != null && connectivityManager.activeNetworkInfo != null) {
            connectivityManager.activeNetworkInfo!!.isAvailable
        } else {
            false
        }
    }

    fun updateDeviceToken(context: Context?, token: String) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val databaseReference = rootRef.child(NodeNames.TOKENS).child(Constants.currentUserId)
        val hashMap = HashMap<String, String>()
        hashMap[NodeNames.DEVICE_TOKEN] = token
        databaseReference.setValue(hashMap).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                context?.let {
                    Toast.makeText(
                        context,
                        R.string.failed_to_save_device_token,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun updateChatDetails(
        context: Context,
        currentUserId: String,
        chatUserId: String,
        lastMessage: String?
    ) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val chatRef = rootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId)
        val currentUserRef = rootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId)

        chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                var currentCount = 0
                if (dataSnapshot.child(NodeNames.UNREAD_COUNT).value != null) currentCount =
                    Integer.valueOf(dataSnapshot.child(NodeNames.UNREAD_COUNT).value.toString())

                val chatUserMap: HashMap<String, Any> = hashMapOf(
                    NodeNames.CHATS + "/" + chatUserId + "/" + currentUserId to ChatListModel(
                        userId = currentUserId,
                        unreadCount = currentCount + 1,
                        lastMessage = lastMessage ?: "",
                        lastMessageTime = ServerValue.TIMESTAMP,
                        timeStamp = ServerValue.TIMESTAMP
                    )
                )

                rootRef.updateChildren(
                    chatUserMap
                ) { databaseError, _ ->
                    if (databaseError != null)
                        Toast.makeText(
                            context,
                            context.getString(R.string.something_went_wrong, databaseError.message),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    context,
                    context.getString(R.string.something_went_wrong, databaseError.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })

        currentUserRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var currentCount = 0
                if (dataSnapshot.child(NodeNames.UNREAD_COUNT).value != null) currentCount =
                    dataSnapshot.child(NodeNames.UNREAD_COUNT).value.toString().toInt()
                val chatUserMap: HashMap<String, Any> = hashMapOf(
                    NodeNames.CHATS + "/" + currentUserId + "/" + chatUserId to ChatListModel(
                        userId = chatUserId,
                        unreadCount = currentCount,
                        lastMessage = lastMessage ?: "",
                        lastMessageTime = ServerValue.TIMESTAMP,
                        timeStamp = ServerValue.TIMESTAMP
                    )
                )
                rootRef.updateChildren(chatUserMap) { databaseError, _ ->
                    if (databaseError != null)
                        Toast.makeText(
                            context,
                            context.getString(R.string.something_went_wrong, databaseError.message),
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    context,
                    context.getString(R.string.something_went_wrong, databaseError.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun sendNotification(context: Context, title: String?, message: MessageModel, userId: String?) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val databaseReference = rootRef.child(NodeNames.TOKENS).child(userId!!)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(NodeNames.DEVICE_TOKEN).value != null) {
                    val deviceToken = dataSnapshot.child(NodeNames.DEVICE_TOKEN).value.toString()
                    try {
                        try {
                            val data = JSONObject()
                            val pushData = JSONObject()
                            data.put("type", title)
                            data.put(Constants.NOTIFICATION_FROM, message.messageFrom)
                            data.put(Constants.NOTIFICATION_TITLE, title)
                            data.put(Constants.NOTIFICATION_MESSAGE, Gson().fromObject(message))
                            data.put(Constants.NOTIFICATION_TO, deviceToken)
                            pushData.put(Constants.NOTIFICATION_DATA, data)
                            val push = FCMSender.Builder()
                                .serverKey(Constants.FIREBASE_KEY)
                                .setData(pushData)
                                .toTokenOrTopic(deviceToken)
                                .responseListener(object : FCMSender.ResponseListener {
                                    override fun onFailure(errorCode: Int, message: String) {
                                        Toast.makeText(
                                            context,
                                            "notification sent Failed to $deviceToken",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }

                                    override fun onSuccess(response: String) {
                                        Toast.makeText(
                                            context,
                                            "notification sent Successfully to $deviceToken",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }).build()
                            push.sendPush(context)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } catch (e: JSONException) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.failed_to_send_notification, e.message),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_send_notification, databaseError.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun getTimeAgo(time: Long): String {

        var timeTemp: Long = time
        val secondMillis: Long = 1000
        val minuteMillis: Long = 60 * secondMillis
        val hourMillis: Long = 60 * minuteMillis
        val dayMillis: Long = 24 * hourMillis

        if (timeTemp < 1000000000000L) {
            timeTemp *= 1000
        }
        val now = System.currentTimeMillis()
        if (timeTemp > now || timeTemp <= 0) {
            return ""
        }
        val diff = now - timeTemp
        return when {
            diff < minuteMillis -> {
                "just now"
            }
            diff < 2 * minuteMillis -> {
                "a minute ago"
            }
            diff < 59 * minuteMillis -> {
                diff.div(minuteMillis).toString() + " minutes ago"
            }
            diff < 90 * minuteMillis -> {
                "an hour ago"
            }
            diff < 24 * hourMillis -> {
                diff.div(hourMillis).toString() + " hours ago"
            }
            diff < 48 * hourMillis -> {
                "yesterday"
            }
            else -> {
                diff.div(dayMillis).toString() + " days ago"
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getLastSeen(lastSeen: Long): String {
        val currentTime = System.currentTimeMillis()
        val dayCount = (currentTime - lastSeen) / (24 * 60 * 60 * 1000)
        val calender = Calendar.getInstance()
        calender.timeInMillis = lastSeen
        val date = calender.time
        return when {
            dayCount > 1L -> {
                //DD/MM/YYYY format
                "last seen ${SimpleDateFormat("dd MMM yyyy").format(date)}"
            }
            dayCount == 1L -> {
                "last seen yesterday ${SimpleDateFormat("hh:mm aa").format(date)}"
            }
            else -> {
                //hh:mm aa
                "last seen today at ${SimpleDateFormat("hh:mm aa").format(date)}"
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun getTime(sentTime: Long): String {
        val currentTime = System.currentTimeMillis()
        val dayCount = (currentTime - sentTime) / (24 * 60 * 60 * 1000)
        val calender = Calendar.getInstance()
        calender.timeInMillis = sentTime
        val date = calender.time
        return when {
            dayCount > 1L -> {
                //DD/MM/YYYY format
                SimpleDateFormat("dd/MM/yyyy").format(date)
            }
            dayCount == 1L -> {
                "Yesterday"
            }
            else -> {
                //hh:mm aa
                SimpleDateFormat("hh:mm aa").format(date)
            }
        }
    }

    fun checkLocationPermission(context: Context?): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context!!, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
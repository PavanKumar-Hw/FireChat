package com.example.firechat.common

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.firechat.NodeNames
import com.example.firechat.R
import com.google.firebase.database.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

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
                Toast.makeText(
                    context,
                    R.string.failed_to_save_device_token,
                    Toast.LENGTH_SHORT
                ).show()
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
                var currentCount = "0"
                if (dataSnapshot.child(NodeNames.UNREAD_COUNT).value != null) currentCount =
                    dataSnapshot.child(NodeNames.UNREAD_COUNT).value.toString()
                val chatMap: HashMap<String, Any> = HashMap<String, Any>()
                chatMap[NodeNames.TIME_STAMP] = ServerValue.TIMESTAMP
                chatMap[NodeNames.UNREAD_COUNT] = Integer.valueOf(currentCount) + 1
                chatMap[NodeNames.LAST_MESSAGE] = lastMessage ?: ""
                chatMap[NodeNames.LAST_MESSAGE_TIME] = ServerValue.TIMESTAMP
                val chatUserMap: HashMap<String, Any> = HashMap<String, Any>()
                chatUserMap[NodeNames.CHATS + "/" + chatUserId + "/" + currentUserId] = chatMap
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
                val chatMap: HashMap<String, Any> = HashMap<String, Any>()
                chatMap[NodeNames.TIME_STAMP] = ServerValue.TIMESTAMP
                chatMap[NodeNames.LAST_MESSAGE] = lastMessage ?: ""
                chatMap[NodeNames.LAST_MESSAGE_TIME] = ServerValue.TIMESTAMP
                val chatUserMap: HashMap<String, Any> = HashMap<String, Any>()
                chatUserMap[NodeNames.CHATS + "/" + currentUserId + "/" + chatUserId] = chatMap
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
    }

    fun sendNotification(context: Context, title: String?, message: String?, userId: String?) {
        val rootRef = FirebaseDatabase.getInstance().reference
        val databaseReference = rootRef.child(NodeNames.TOKENS).child(userId!!)
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(NodeNames.DEVICE_TOKEN).value != null) {
                    val deviceToken = dataSnapshot.child(NodeNames.DEVICE_TOKEN).value.toString()
                    val notification = JSONObject()
                    val notificationData = JSONObject()
                    try {
                        notificationData.put(Constants.NOTIFICATION_TITLE, title)
                        notificationData.put(Constants.NOTIFICATION_MESSAGE, message)
                        notificationData.put(Constants.NOTIFICATION_FROM, userId)
                        notification.put(Constants.NOTIFICATION_TO, deviceToken)
                        notification.put(Constants.NOTIFICATION_DATA, notificationData)
                        val fcmApiUrl = "https://fcm.googleapis.com/fcm/send"
                        val contentType = "application/json"
                        val successListener: Any = Response.Listener<Any> {
                            Toast.makeText(
                                context,
                                "Notification Sent",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        val failureListener: Response.ErrorListener =
                            Response.ErrorListener { error ->
                                Toast.makeText(
                                    context,
                                    context.getString(
                                        R.string.failed_to_send_notification,
                                        error.message
                                    ), Toast.LENGTH_SHORT
                                ).show()
                            }
                        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                            fcmApiUrl, notification,
                            successListener as Response.Listener<JSONObject>?, failureListener
                        ) {
                            override fun getHeaders(): MutableMap<String, String> {
                                val params: MutableMap<String, String> = HashMap()
                                params["Authorization"] = "key=" + Constants.FIREBASE_KEY
                                params["Sender"] = "id=" + Constants.SENDER_ID
                                params["Content-Type"] = contentType
                                return params
                            }

                        }
                        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
                        requestQueue.add(jsonObjectRequest)
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

    fun checkLocationPermission(context: Context?): Boolean {
        return (ActivityCompat.checkSelfPermission(
            context!!, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }
}
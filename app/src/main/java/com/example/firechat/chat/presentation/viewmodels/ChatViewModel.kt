package com.example.firechat.chat.presentation.viewmodels

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.firechat.NodeNames
import com.example.firechat.R
import com.example.firechat.chat.data.models.Location
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.chat.data.models.RequestOptions
import com.example.firechat.chat.domain.ChatUseCase
import com.example.firechat.common.*
import com.google.firebase.database.ServerValue
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.HashMap
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatUseCase: ChatUseCase,
    private val fbRefMessagesChildObserver: FirebaseReferenceChildObserver
) : ViewModel() {

    lateinit var chatUserId: String
    val isLocPermissionGrantedForReq = MutableLiveData(false)
    val isLocPermissionGrantedForRes = MutableLiveData(false)
    val clearText = MutableLiveData(false)
    var messageList = MediatorLiveData<MutableList<MessageModel>>()

    private val _addedMessage = MutableLiveData<MessageModel>()

    fun createMessageBody(
        msg: String, msgType: String, isLocationReq: Boolean,
        lastKnownLocation: android.location.Location?,
        context: Context, isLocationRes: Boolean,
        locReqObj: MessageModel?
    ) {
        val pushId = generateMessageId()
        val currentUserRef = NodeNames.MESSAGES + "/" + Constants.currentUserId + "/" + chatUserId
        val chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + Constants.currentUserId
        try {
            if (isLocationReq || isLocationRes) {
                createLocationMessage(
                    msg, msgType, isLocationReq, lastKnownLocation,
                    context, isLocationRes, locReqObj, pushId, currentUserRef, chatUserRef
                )
            } else if (msg != "") {
                createTextMessage(msg, msgType, context, pushId, currentUserRef, chatUserRef)
            }
        } catch (ex: Exception) {
            Toast.makeText(
                context, context.getString(R.string.failed_to_send_message, ex.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createLocationMessage(
        msg: String, msgType: String, isLocationReq: Boolean,
        lastKnownLocation: android.location.Location?,
        context: Context, isLocationRes: Boolean,
        locReqObj: MessageModel?, pushId: String?, currentUserRef: String,
        chatUserRef: String
    ) {
        val location = if (isLocationReq) {
            Location(requestingUserLoc = lastKnownLocation?.latitude.toString() + "[$]" + lastKnownLocation?.longitude.toString())
        } else {
            Location(
                requestingUserLoc = locReqObj?.location?.requestingUserLoc,
                senderUserLoc = lastKnownLocation?.latitude?.plus(0.2)
                    .toString() + "[$]" + lastKnownLocation?.longitude?.plus(0.2)
                    .toString()
            )
        }
        val message = MessageModel(
            message = msg, messageFrom = Constants.currentUserId,
            messageTo = chatUserId, messageId = pushId,
            messageType = msgType, messageTime = ServerValue.TIMESTAMP,
            location = location
        )
        if (isLocationReq) {
            message.requestOptions = RequestOptions("", "")
        }
        val messageUserMap: HashMap<String, Any> = HashMap<String, Any>()
        messageUserMap["$currentUserRef/$pushId"] = message
        messageUserMap["$chatUserRef/$pushId"] = message
        prepareNotification(messageUserMap, msgType, msg, context)
    }

    private fun createTextMessage(
        msg: String, msgType: String,
        context: Context,
        pushId: String?,
        currentUserRef: String,
        chatUserRef: String
    ) {
        val message = MessageModel(
            message = msg, messageFrom = Constants.currentUserId,
            messageTo = chatUserId, messageId = pushId,
            messageType = msgType, messageTime = ServerValue.TIMESTAMP
        )
        val messageUserMap: HashMap<String, Any> = HashMap<String, Any>()
        messageUserMap["$currentUserRef/$pushId"] = message
        messageUserMap["$chatUserRef/$pushId"] = message
        clearText.value = true
        prepareNotification(messageUserMap, msgType, msg, context)
    }

    private fun generateMessageId(): String? {
        val userMessagePush =
            FirebaseDataSource.dbInstance.reference.child(NodeNames.MESSAGES)
                .child(Constants.currentUserId)
                .child(chatUserId)
                .push()
        return userMessagePush.key
    }

    private fun prepareNotification(
        messageUserMap: HashMap<String, Any>,
        msgType: String,
        msg: String,
        context: Context
    ) {
        chatUseCase.sendMessage(messageUserMap) {
            if (it != null) {
                Toast.makeText(
                    context,
                    context.getString(R.string.failed_to_send_message, it),
                    Toast.LENGTH_SHORT
                ).show()
            }
            run {
                Toast.makeText(
                    context, R.string.message_sent_successfully,
                    Toast.LENGTH_SHORT
                ).show()
                val title = when (msgType) {
                    Constants.MESSAGE_TYPE_TEXT -> "New Message"
                    Constants.MESSAGE_TYPE_IMAGE -> "New Image"
                    Constants.MESSAGE_TYPE_VIDEO -> "New Video"
                    Constants.MESSAGE_TYPE_LOC -> "Location"
                    Constants.MESSAGE_TYPE_LOC_REQ -> "Location Request"
                    else -> ""
                }
                Util.sendNotification(context, title, msg, chatUserId)
                val lastMessage = if (title != "New Message") title else msg
                Util.updateChatDetails(
                    context, Constants.currentUserId, chatUserId, lastMessage
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        fbRefMessagesChildObserver.clear()
    }

    fun checkPermission(context: Context, type: Int) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Constants.IsLocPermissionGranted = true
                        if (type == Constants.SEND_LOC_REQ) {
                            isLocPermissionGrantedForReq.value = true
                        } else if (type == Constants.ACCEPT_LOC_REQ) {
                            isLocPermissionGrantedForRes.value = true
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()
    }

    fun loadMessages(messagePath: String) {
        messageList.addSource(_addedMessage) {
            messageList.addNewItem(it)
        }

        chatUseCase.loadMessagesAdded(messagePath, fbRefMessagesChildObserver) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    result.data.let { _addedMessage.value = it }
                }
                Status.ERROR -> {
                }
                Status.LOADING -> {
                }
            }
        }
    }
}
package com.example.firechat.chat.presentation.adapters

import android.content.Context
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.NodeNames
import com.example.firechat.R
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.Constants
import com.example.firechat.databinding.LocationRequestLayoutBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class LocationRequestViewHolder(
    private val binding: LocationRequestLayoutBinding,
    val context: Context,
    private val actionModeCallBack: ActionMode.Callback,
    private val acceptRequest: ((item: MessageModel, pos: Int) -> Unit)?,
    private val rejectFunction: ((item: MessageModel, pos: Int) -> Unit)?,
    private val cancelRequest: ((item: MessageModel, pos: Int) -> Unit)?,
) : RecyclerView.ViewHolder(binding.root) {

    private var actionMode: ActionMode? = null

    fun onBind(message: MessageModel) {
        val currentUserId: String = Constants.currentUserId
        val fromUserId = message.messageFrom
        val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm")
        val dateTime = sfd.format(Date(message.messageTime))
        val splitString = dateTime.split(" ").toTypedArray()
        val messageTime = splitString[1]


        var currentUserRef = ""
        var chatUserRef = ""
        if (currentUserId == fromUserId) {

            currentUserRef =
                NodeNames.MESSAGES + "/" + message.messageFrom + "/" + message.messageTo

            chatUserRef = NodeNames.MESSAGES + "/" + message.messageTo + "/" + message.messageFrom
        } else {

            currentUserRef =
                NodeNames.MESSAGES + "/" + message.messageTo + "/" + message.messageFrom

            chatUserRef = NodeNames.MESSAGES + "/" + message.messageFrom + "/" + message.messageTo
        }

        val mRootRef = FirebaseDatabase.getInstance().reference
        val currentUserDB = mRootRef.child(currentUserRef)
        val chatUserDB = mRootRef.child(chatUserRef)
        binding.apply {
            if (fromUserId == currentUserId) {
                if (message.requestOptions?.requestStatus != "") {
                    callHideViewsMethod(
                        message.requestOptions?.requestStatus,
                        fromUserId,
                        currentUserId
                    )
                } else {
                    currentUserDB.child(message.messageId!!).child(NodeNames.REQ_OPTIONS)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                callHideViewsMethod(
                                    snapshot.child(NodeNames.REQ_STATUS).value.toString(),
                                    fromUserId,
                                    currentUserId
                                )
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Toast.makeText(
                                    context,
                                    "could not cancel the request",
                                    Toast.LENGTH_LONG
                                ).show()
                            }

                        })
                }
                llRequstSent.visibility = View.VISIBLE
                llRequestReceived.visibility = View.GONE
                btnCancelReq.setOnClickListener {
                    cancelRequest?.invoke(message, adapterPosition)

                    currentUserDB.child(message.messageId!!)
                        .child(NodeNames.REQ_OPTIONS).child(NodeNames.REQ_STATUS)
                        .setValue(Constants.REQUEST_STATUS_CANCELLED)

                    chatUserDB.child(message.messageId!!)
                        .child(NodeNames.REQ_OPTIONS).child(NodeNames.REQ_STATUS)
                        .setValue(Constants.REQUEST_STATUS_CANCELLED)
                }
            } else {

                chatUserDB.child(message.messageId!!).child(NodeNames.REQ_OPTIONS)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.child(NodeNames.REQ_STATUS).value == Constants.REQUEST_STATUS_CANCELLED ||
                                snapshot.child(NodeNames.REQ_STATUS).value == Constants.REQUEST_STATUS_ACCEPTED ||
                                snapshot.child(NodeNames.REQ_STATUS).value == Constants.REQUEST_STATUS_REJECTED
                            ) {
                                if (fromUserId != null) {
                                    hideViewsOnCancel(fromUserId, currentUserId)
                                }
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                context,
                                "could not cancel the request",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    })
                llRequstSent.visibility = View.GONE
                llRequestReceived.visibility = View.VISIBLE
                btnAcceptReq.setOnClickListener {
                    acceptRequest?.invoke(message, adapterPosition)

                    currentUserDB.child(message.messageId!!)
                        .child(NodeNames.REQ_OPTIONS).child(NodeNames.REQ_STATUS)
                        .setValue(Constants.REQUEST_STATUS_ACCEPTED)

                    chatUserDB.child(message.messageId!!)
                        .child(NodeNames.REQ_OPTIONS).child(NodeNames.REQ_STATUS)
                        .setValue(Constants.REQUEST_STATUS_ACCEPTED)
                }
                btnRejectReq.setOnClickListener {
                    rejectFunction?.invoke(message, adapterPosition)

                    currentUserDB.child(message.messageId!!)
                        .child(NodeNames.REQ_OPTIONS).child(NodeNames.REQ_STATUS)
                        .setValue(Constants.REQUEST_STATUS_REJECTED)

                    chatUserDB.child(message.messageId!!)
                        .child(NodeNames.REQ_OPTIONS).child(NodeNames.REQ_STATUS)
                        .setValue(Constants.REQUEST_STATUS_REJECTED)
                }
            }
            clRequestLoc.setTag(R.id.TAG_MESSAGE, message.message)
            clRequestLoc.setTag(R.id.TAG_MESSAGE_ID, message.messageId)
            clRequestLoc.setTag(R.id.TAG_MESSAGE_TYPE, message.messageType)

            clRequestLoc.setOnLongClickListener(View.OnLongClickListener {
                if (actionMode != null) return@OnLongClickListener false
                MessagesAdapter.selectedView = clRequestLoc
                actionMode =
                    (context as AppCompatActivity).startSupportActionMode(actionModeCallBack)
                clRequestLoc.setBackgroundColor(
                    context.getResources().getColor(R.color.colorAccent)
                )
                true
            })

        }
    }

    private fun callHideViewsMethod(
        requestStatus: String?,
        fromUserId: String,
        currentUserId: String
    ) {
        if (requestStatus == Constants.REQUEST_STATUS_CANCELLED ||
            requestStatus == Constants.REQUEST_STATUS_ACCEPTED ||
            requestStatus == Constants.REQUEST_STATUS_REJECTED
        ) {
            hideViewsOnCancel(fromUserId, currentUserId)
        }
    }

    private fun hideViewsOnCancel(fromUserId: String, currentUserId: String) {
        if (fromUserId == currentUserId) {
            binding.btnCancelReq.visibility = View.GONE
            binding.tvSentMsgValidTime.visibility = View.GONE
        } else {
            binding.btnRejectReq.visibility = View.GONE
            binding.btnAcceptReq.visibility = View.GONE
            binding.tvReceivedMsgValidTime.visibility = View.GONE
        }
    }
}
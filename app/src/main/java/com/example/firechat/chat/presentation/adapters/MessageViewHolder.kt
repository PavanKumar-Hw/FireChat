package com.example.firechat.chat.presentation.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.R
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.Constants
import com.example.firechat.databinding.MessageLayoutBinding
import java.text.SimpleDateFormat
import java.util.*

class MessageViewHolder(
    private val binding: MessageLayoutBinding,
    private val actionModeCallBack: ActionMode.Callback,
    val context: Context
) : RecyclerView.ViewHolder(binding.root) {

    private var actionMode: ActionMode? = null

    fun onBind(message: MessageModel) {
        val currentUserId: String = Constants.currentUserId
        val fromUserId = message.messageFrom
        val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm")
        val dateTime = sfd.format(Date(message.messageTime))
        val splitString = dateTime.split(" ").toTypedArray()
        val messageTime = splitString[1]

        binding.apply {
            if (fromUserId == currentUserId) {
                if (message.messageType == Constants.MESSAGE_TYPE_TEXT) {
                    llSent.visibility = View.VISIBLE
                    llSentImage.visibility = View.GONE
                } else {
                    llSent.visibility = View.GONE
                    llSentImage.visibility = View.VISIBLE
                }
                llReceived.visibility = View.GONE
                llReceivedImage.visibility = View.GONE
                tvSentMessage.text = message.message
                tvSentMessageTime.text = messageTime
                tvSentImageTime.text = messageTime
                /*Glide.with(context)
                    .load(message.message)
                    .placeholder(R.drawable.ic_image)
                    .into(ivSent)*/
            } else {
                if (message.messageType == Constants.MESSAGE_TYPE_TEXT) {
                    llReceived.visibility = View.VISIBLE
                    llReceivedImage.visibility = View.GONE
                } else {
                    llReceived.visibility = View.GONE
                    llReceivedImage.visibility = View.VISIBLE
                }
                llSent.visibility = View.GONE
                llSentImage.visibility = View.GONE
                tvReceivedMessage.text = message.message
                tvReceivedMessageTime.text = messageTime
                tvReceivedImageTime.text = messageTime
                /*Glide.with(context)
                    .load(message.message)
                    .placeholder(R.drawable.ic_image)
                    .into(ivReceived)*/
            }

            clMessage.setTag(R.id.TAG_MESSAGE, message.message)
            clMessage.setTag(R.id.TAG_MESSAGE_ID, message.messageId)
            clMessage.setTag(R.id.TAG_MESSAGE_TYPE, message.messageType)

            clMessage.setOnClickListener { view ->
                val messageType = view.getTag(R.id.TAG_MESSAGE_TYPE).toString()
                val uri = Uri.parse(view.getTag(R.id.TAG_MESSAGE).toString())
                if (messageType == Constants.MESSAGE_TYPE_VIDEO) {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setDataAndType(uri, "video/mp4")
                    context.startActivity(intent)
                } else if (messageType == Constants.MESSAGE_TYPE_IMAGE) {
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    intent.setDataAndType(uri, "image/jpg")
                    context.startActivity(intent)
                }
            }

            clMessage.setOnLongClickListener(View.OnLongClickListener {
                if (actionMode != null) return@OnLongClickListener false
                MessagesAdapter.selectedView = clMessage
                actionMode =
                    (context as AppCompatActivity).startSupportActionMode(actionModeCallBack)
                clMessage.setBackgroundColor(
                    context.getResources().getColor(R.color.colorAccent)
                )
                true
            })
        }
    }
}
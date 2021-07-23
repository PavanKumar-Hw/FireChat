package com.example.firechat.chat.presentation.adapters

import android.content.Context
import android.content.Intent
import android.view.*
import androidx.appcompat.view.ActionMode
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.R
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.chat.presentation.activities.ChatActivity
import com.example.firechat.common.Constants
import com.example.firechat.databinding.HalfwayLocMessageLayoutBinding
import com.example.firechat.databinding.LocationRequestLayoutBinding
import com.example.firechat.databinding.MessageLayoutBinding

class MessagesAdapter(private val context: Context, private val messageList: List<MessageModel>) :
    ListAdapter<MessageModel, RecyclerView.ViewHolder>(MessageDiffCallback()) {

    companion object {
        var selectedView: ConstraintLayout? = null
    }

    private var acceptFunction: ((item: MessageModel, pos: Int) -> Unit)? = null

    fun addAcceptListener(clickFunction: (MessageModel, Int) -> Unit) {
        this.acceptFunction = clickFunction
    }

    private var rejectFunction: ((item: MessageModel, pos: Int) -> Unit)? = null

    fun addRejectListener(clickFunction: (MessageModel, Int) -> Unit) {
        this.rejectFunction = clickFunction
    }

    private var cancelRequest: ((item: MessageModel, pos: Int) -> Unit)? = null

    fun addCancelListener(clickFunction: (MessageModel, Int) -> Unit) {
        this.cancelRequest = clickFunction
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            Constants.MESSAGE_HOLDER_TYPE_TEXT -> {
                val view =
                    MessageLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                MessageViewHolder(view, actionModeCallBack, context)
            }
            Constants.MESSAGE_HOLDER_TYPE_LOC_REQ -> {
                val view =
                    LocationRequestLayoutBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                LocationRequestViewHolder(
                    view, context, actionModeCallBack, acceptFunction, rejectFunction, cancelRequest
                )
            }
            Constants.MESSAGE_HOLDER_TYPE_LOC -> {
                val view =
                    HalfwayLocMessageLayoutBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false
                    )
                HalfwayLocViewHolder(view, context, actionModeCallBack)
            }
            else -> {
                throw Exception("Error reading holder type")
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        when (holder.itemViewType) {
            Constants.MESSAGE_HOLDER_TYPE_TEXT -> {
                (holder as MessageViewHolder).onBind(message)
            }
            Constants.MESSAGE_HOLDER_TYPE_LOC_REQ -> {
                (holder as LocationRequestViewHolder).onBind(message)
            }
            Constants.MESSAGE_HOLDER_TYPE_LOC -> {
                (holder as HalfwayLocViewHolder).onBind(message)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val message = messageList[position]
        return when (message.messageType) {
            Constants.MESSAGE_TYPE_TEXT -> {
                Constants.MESSAGE_HOLDER_TYPE_TEXT
            }
            Constants.MESSAGE_TYPE_LOC -> {
                Constants.MESSAGE_HOLDER_TYPE_LOC
            }
            Constants.MESSAGE_TYPE_LOC_REQ -> {
                Constants.MESSAGE_HOLDER_TYPE_LOC_REQ
            }
            else -> {
                Constants.MESSAGE_HOLDER_TYPE_TEXT
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    var actionModeCallBack: ActionMode.Callback = object : ActionMode.Callback {
        override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            val inflater = actionMode.menuInflater
            inflater.inflate(R.menu.menu_chat_options, menu)
            val selectedMessageType = selectedView!!.getTag(R.id.TAG_MESSAGE_TYPE).toString()
            if (selectedMessageType == Constants.MESSAGE_TYPE_TEXT) {
                val itemDownload = menu.findItem(R.id.mnuDownload)
                itemDownload.isVisible = false
            }
            return true
        }

        override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
            val selectedMessageId = selectedView!!.getTag(R.id.TAG_MESSAGE_ID).toString()
            val selectedMessage = selectedView!!.getTag(R.id.TAG_MESSAGE).toString()
            val selectedMessageType = selectedView!!.getTag(R.id.TAG_MESSAGE_TYPE).toString()
            val itemId = menuItem.itemId
            when (itemId) {
                R.id.mnuDelete -> {
                    if (context is ChatActivity) {
                        (context as ChatActivity).deleteMessage(
                            selectedMessageId,
                            selectedMessageType
                        )
                    }
                    actionMode.finish()
                }
                R.id.mnuDownload -> {
                    /*if (context is ChatActivity) {
                        (context as ChatActivity).downloadFile(
                            selectedMessageId,
                            selectedMessageType,
                            false
                        )
                    }
                    actionMode.finish()*/
                }
                R.id.mnuShare -> {
                    if (selectedMessageType == Constants.MESSAGE_TYPE_TEXT) {
                        val intentShare = Intent()
                        intentShare.action = Intent.ACTION_SEND
                        intentShare.putExtra(Intent.EXTRA_TEXT, selectedMessage)
                        intentShare.type = "text/plain"
                        context.startActivity(intentShare)
                    } else {
                        /*if (context is ChatActivity) {
                            (context as ChatActivity).downloadFile(
                                selectedMessageId,
                                selectedMessageType,
                                true
                            )
                        }*/
                    }
                    actionMode.finish()
                }
                R.id.mnuForward -> {
                    if (context is ChatActivity) {
                        /*(context as ChatActivity).forwardMessage(
                            selectedMessageId,
                            selectedMessage,
                            selectedMessageType
                        )*/
                    }
                    actionMode.finish()
                }
            }
            return false
        }

        override fun onDestroyActionMode(actionMode: ActionMode) {
            var actionMode: ActionMode? = actionMode
            actionMode = null
            selectedView!!.setBackgroundColor(context.resources.getColor(R.color.chat_background))
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<MessageModel>() {
    override fun areItemsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MessageModel, newItem: MessageModel): Boolean {
        return oldItem.messageTime == newItem.messageTime
    }
}
package com.example.firechat.chats.presentation.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.chats.presentation.adapters.ChatListAdapter.ChatListViewHolder
import com.example.firechat.chat.presentation.activities.ChatActivity
import com.example.firechat.common.Extras
import com.example.firechat.common.Util.getTimeAgo
import com.example.firechat.databinding.ChatListLayoutBinding
import com.example.firechat.chats.data.models.ChatListModel
import com.example.firechat.common.Util.getLastSeen
import com.example.firechat.common.Util.getTime

class ChatListAdapter(
    private val context: Context
) : ListAdapter<ChatListModel, ChatListViewHolder>(ChatDiffCallback()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view = ChatListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chatListModel = getItem(position)
        holder.onBind(chatListModel)
    }

    inner class ChatListViewHolder(private val binding: ChatListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(chatItem: ChatListModel) {
            binding.apply {

                tvFullName.text = chatItem.userName

                var lastMessage = chatItem.lastMessage
                lastMessage?.let {
                    lastMessage =
                        if (it.length > 30) lastMessage?.substring(0, 30) else lastMessage
                }

                tvLastMessage.text = lastMessage

                val lastMessageTime: Any? = chatItem.lastMessageTime
                lastMessageTime?.let {
                    tvLastMessageTime.text = getTime(it.toString().toLong())
                }
                if (chatItem.unreadCount != 0) {
                    tvUnreadCount.visibility = View.VISIBLE
                    tvUnreadCount.text = chatItem.unreadCount.toString()
                } else {
                    tvUnreadCount.visibility = View.GONE
                }

                llChatList.setOnClickListener {
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra(Extras.USER_KEY, chatItem.userId)
                    intent.putExtra(Extras.USER_NAME, chatItem.userName)
                    intent.putExtra(Extras.PHOTO_NAME, chatItem.photoPath)
                    context.startActivity(intent)
                }
            }
        }
    }
}

class ChatDiffCallback : DiffUtil.ItemCallback<ChatListModel>() {
    override fun areItemsTheSame(oldItem: ChatListModel, newItem: ChatListModel): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ChatListModel, newItem: ChatListModel): Boolean {
        return (oldItem?.timeStamp as Long) == (newItem?.timeStamp as Long)
    }
}


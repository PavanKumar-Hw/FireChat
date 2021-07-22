package com.example.firechat.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.adapters.ChatListAdapter.ChatListViewHolder
import com.example.firechat.chat.presentation.activities.ChatActivity
import com.example.firechat.common.Extras
import com.example.firechat.common.Util.getTimeAgo
import com.example.firechat.databinding.ChatListLayoutBinding
import com.example.firechat.models.ChatListModel

class ChatListAdapter(
    private val context: Context,
    private val chatListModelList: List<ChatListModel>
) : RecyclerView.Adapter<ChatListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {
        val view = ChatListLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {
        val chatListModel = chatListModelList[position]
        holder.onBind(chatListModel)
    }

    override fun getItemCount(): Int {
        return chatListModelList.size
    }

    inner class ChatListViewHolder(private val binding: ChatListLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun onBind(chatItem: ChatListModel) {
            binding.apply {

                tvFullName.text = chatItem.userName

                var lastMessage = chatItem.lastMessage
                lastMessage =
                    if (lastMessage.length > 30) lastMessage.substring(0, 30) else lastMessage
                tvLastMessage.text = lastMessage

                val lastMessageTime: String = chatItem.lastMessageTime
                if (lastMessageTime.isNotEmpty())
                    tvLastMessageTime.text = getTimeAgo(lastMessageTime.toLong())

                if (chatItem.unreadCount != "0") {
                    tvUnreadCount.visibility = View.VISIBLE
                    tvUnreadCount.text = chatItem.unreadCount
                } else {
                    tvUnreadCount.visibility = View.GONE
                }

                llChatList.setOnClickListener {
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra(Extras.USER_KEY, chatItem.userId)
                    intent.putExtra(Extras.USER_NAME, chatItem.userName)
                    intent.putExtra(Extras.PHOTO_NAME, chatItem.photoName)
                    context.startActivity(intent)
                }
            }
        }
    }
}
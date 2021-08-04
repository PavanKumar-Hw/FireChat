package com.example.firechat.chats.presentation.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.chat.presentation.adapters.MessagesAdapter
import com.example.firechat.chats.presentation.adapters.ChatListAdapter
import com.example.firechat.common.Constants
import com.example.firechat.common.NodeNames
import com.example.firechat.contacts.presentation.activities.ContactsActivity
import com.example.firechat.databinding.ActivityMainBinding
import com.example.firechat.chats.data.models.ChatListModel
import com.example.firechat.chats.presentation.viewmodels.ChatsViewModel
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var chatListAdapter: ChatListAdapter

    private val chatsViewModel: ChatsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val databaseReferenceUsers = FirebaseDatabase.getInstance().reference
            .child(NodeNames.USERS).child(Constants.currentUserId)

        databaseReferenceUsers.child(NodeNames.ONLINE).setValue(true)
        databaseReferenceUsers.child(NodeNames.ONLINE).onDisconnect().setValue(false)
        databaseReferenceUsers.child(NodeNames.LAST_SEEN).onDisconnect()
            .setValue(ServerValue.TIMESTAMP)
        initViews()
        loadChats()
    }

    private fun initViews() {
        chatListAdapter = ChatListAdapter(this)
        binding.rcvChats.adapter = chatListAdapter
        binding.fabSendNewMsg.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadChats() {
        chatsViewModel.loadChats("${NodeNames.CHATS}/${Constants.currentUserId}")
        observeChats()
    }

    private fun observeChats() {
        chatsViewModel.messageList.observe(this) {
            chatListAdapter.submitList(it)
        }
    }

}
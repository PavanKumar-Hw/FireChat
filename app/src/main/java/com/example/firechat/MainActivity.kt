package com.example.firechat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.firechat.adapters.ChatListAdapter
import com.example.firechat.common.Constants
import com.example.firechat.contacts.ContactsActivity
import com.example.firechat.databinding.ActivityMainBinding
import com.example.firechat.models.ChatListModel
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var query: Query
    private var childEventListener: ChildEventListener? = null
    private lateinit var binding: ActivityMainBinding

    private lateinit var chatListAdapter: ChatListAdapter
    private var chatListModelList: ArrayList<ChatListModel> = ArrayList()
    private val userIds: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val databaseReferenceUsers = FirebaseDatabase.getInstance().reference
            .child(NodeNames.USERS).child(Constants.currentUserId)

        databaseReferenceUsers.child(NodeNames.ONLINE).setValue(true)
        databaseReferenceUsers.child(NodeNames.ONLINE).onDisconnect().setValue(false)

        initDatabase()
        initViews()
    }

    private fun initViews() {
        chatListAdapter = ChatListAdapter(this, chatListModelList)
        binding.rcvChats.adapter = chatListAdapter
        binding.fabSendNewMsg.setOnClickListener {
            val intent = Intent(this, ContactsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initDatabase() {
        database = FirebaseDatabase.getInstance().reference.child("Chats")
            .child(Constants.currentUserId)
        query = database.orderByChild("timeStamp")

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                updateList(dataSnapshot, true, dataSnapshot.key!!)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                updateList(dataSnapshot, false, dataSnapshot.key!!)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                removeChat(dataSnapshot)
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {}
        }
        query.addChildEventListener(childEventListener as ChildEventListener)
    }

    private fun removeChat(dataSnapshot: DataSnapshot) {
        val indexOfClickedUser: Int = userIds.indexOf(dataSnapshot.key!!)
        chatListModelList.removeAt(indexOfClickedUser)
        userIds.removeAt(indexOfClickedUser)
        chatListAdapter.notifyDataSetChanged()
        Toast.makeText(this@MainActivity, "Chat Deleted", Toast.LENGTH_LONG).show()
    }

    private fun updateList(dataSnapshot: DataSnapshot, isNew: Boolean, userId: String) {
        val lastMessage: String =
            if (dataSnapshot.child(NodeNames.LAST_MESSAGE).value != null) dataSnapshot.child(
                NodeNames.LAST_MESSAGE
            ).value.toString() else ""
        val lastMessageTime: String =
            if (dataSnapshot.child(NodeNames.LAST_MESSAGE_TIME).value != null) dataSnapshot.child(
                NodeNames.LAST_MESSAGE_TIME
            ).value.toString() else ""
        val unreadCount: String =
            if (dataSnapshot.child(NodeNames.UNREAD_COUNT).value == null) "0" else dataSnapshot.child(
                NodeNames.UNREAD_COUNT
            ).value.toString()


        val chatListModel = userId?.let {
            ChatListModel(
                it, "", "", unreadCount, lastMessage, lastMessageTime
            )
        }
        if (isNew) {
            chatListModelList.add(chatListModel)
            userIds.add(userId)
        } else {
            val indexOfClickedUser: Int = userIds.indexOf(userId)
            chatListModelList[indexOfClickedUser] = chatListModel
        }
        chatListAdapter.notifyDataSetChanged()

    }

}
package com.example.firechat.chat.presentation.activities

import android.content.Context
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.NodeNames
import com.example.firechat.R
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.chat.presentation.adapters.MessagesAdapter
import com.example.firechat.chat.presentation.viewmodels.ChatViewModel
import com.example.firechat.common.*
import com.example.firechat.common.Constants.Companion.STATUS_OFFLINE
import com.example.firechat.common.Constants.Companion.STATUS_ONLINE
import com.example.firechat.common.Constants.Companion.STATUS_TYPING
import com.example.firechat.common.Constants.Companion.currentUserId
import com.example.firechat.databinding.ActivityChatBinding
import com.example.firechat.databinding.CustomActionBarBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ChatActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityChatBinding
    private lateinit var mRootRef: DatabaseReference
    private lateinit var chatUserId: String
    private lateinit var userName: String
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver
    private lateinit var toolBarBinding: CustomActionBarBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.title = ""
            toolBarBinding =
                CustomActionBarBinding.inflate(
                    layoutInflater,
                    layoutInflater.inflate(R.layout.custom_action_bar, null) as ViewGroup,
                    false
                )
            actionBar.elevation = 0f
            actionBar.customView = toolBarBinding.root
            actionBar.displayOptions = actionBar.displayOptions or ActionBar.DISPLAY_SHOW_CUSTOM
        }
        if (intent.hasExtra(Extras.USER_KEY)) {
            intent.getStringExtra(Extras.USER_KEY)?.let {
                chatUserId = it
                viewModel.chatUserId = it
            }
        }

        initViews()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        Constants.IsLocPermissionGranted = Util.checkLocationPermission(this)
    }

    private fun initViews() {

        binding.ivSend.setOnClickListener(this)
        binding.ivLocation.setOnClickListener(this)

        observerClearMessage()
        mRootRef = FirebaseDatabase.getInstance().reference

        if (intent.hasExtra(Extras.USER_NAME)) {
            userName = intent.getStringExtra(Extras.USER_NAME)!!
        }

        toolBarBinding.apply {
            tvUserName.text = userName
            ivBack.setOnClickListener {
                this@ChatActivity.onBackPressed()
            }
        }

        initRecyclerView()
        loadMessages()

        messagesAdapter.addAcceptListener { messageModel, i ->
            Toast.makeText(
                this,
                "Accepted ${messageModel.location?.requestingUserLoc}",
                Toast.LENGTH_LONG
            ).show()
            if (Util.connectionAvailable(this)) {
                checkPermission(this, Constants.ACCEPT_LOC_REQ, messageModel)
            }
        }
        messagesAdapter.addRejectListener { messageModel, i ->
            Toast.makeText(this, "Rejected", Toast.LENGTH_LONG).show()
        }
        messagesAdapter.addCancelListener { messageModel, i ->
            Toast.makeText(
                this,
                "Cancelled ${messageModel.location?.requestingUserLoc}",
                Toast.LENGTH_LONG
            ).show()
        }

        if (intent.hasExtra(Extras.MESSAGE) &&
            intent.hasExtra(Extras.MESSAGE_ID) &&
            intent.hasExtra(Extras.MESSAGE_TYPE)
        ) {
            val message = intent.getStringExtra(Extras.MESSAGE)
            val messageId = intent.getStringExtra(Extras.MESSAGE_ID)
            val messageType = intent.getStringExtra(Extras.MESSAGE_TYPE)

            if (messageType == Constants.MESSAGE_TYPE_TEXT) {
                message?.let {
                    viewModel.createMessageBody(
                        it, messageType, false, lastKnownLocation,
                        this, false, null
                    )
                }
            }
        }

        val databaseReferenceUsers: DatabaseReference =
            mRootRef.child(NodeNames.USERS).child(chatUserId)
        databaseReferenceUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var status = ""
                if (dataSnapshot.child(NodeNames.ONLINE).value != null) status =
                    dataSnapshot.child(NodeNames.ONLINE).value.toString()
                if (status == "true")
                    toolBarBinding.tvUserStatus.text = STATUS_ONLINE
                else
                    toolBarBinding.tvUserStatus.text = STATUS_OFFLINE
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })


        binding.etMessage.doAfterTextChanged { editable ->
            val currentUserRef: DatabaseReference =
                mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId)
            if (editable.toString() == "") {
                currentUserRef.child(NodeNames.TYPING).setValue(Constants.TYPING_STOPPED)
            } else {
                currentUserRef.child(NodeNames.TYPING).setValue(Constants.TYPING_STARTED)
            }
        }

        val chatUserRef: DatabaseReference =
            mRootRef.child(NodeNames.CHATS).child(chatUserId).child(currentUserId)
        chatUserRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(NodeNames.TYPING).value != null) {
                    val typingStatus = dataSnapshot.child(NodeNames.TYPING).value.toString()
                    if (typingStatus == Constants.TYPING_STARTED)
                        toolBarBinding.tvUserStatus.text = STATUS_TYPING
                    else
                        toolBarBinding.tvUserStatus.text = STATUS_ONLINE
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun observerClearMessage() {
        viewModel.clearText.observe(this) {
            if (it) {
                binding.etMessage.setText("")
            }
        }
    }

    private fun initRecyclerView() {
        listAdapterObserver = (object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                binding.rvMessages.scrollToPosition(positionStart)
            }
        })
        messagesAdapter = MessagesAdapter(this)

        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        messagesAdapter.registerAdapterDataObserver(listAdapterObserver)
        binding.rvMessages.adapter = messagesAdapter
    }

    override fun onDestroy() {
        super.onDestroy()
        messagesAdapter.unregisterAdapterDataObserver(listAdapterObserver)
    }

    private fun loadMessages() {
        val messagesPath = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId
        viewModel.loadMessages(messagesPath)
        observeMessages()
    }

    private fun observeMessages() {
        viewModel.messageList.observe(this) {
            messagesAdapter.submitList(it)
            binding.rvMessages.scrollToPosition(it.size - 1)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivSend -> {
                if (Util.connectionAvailable(this)) {
                    viewModel.createMessageBody(
                        binding.etMessage.text.toString().trim { it <= ' ' },
                        Constants.MESSAGE_TYPE_TEXT,
                        false, lastKnownLocation, this, false, null
                    )
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.ivLocation -> {
                if (Util.connectionAvailable(this)) {
                    checkPermission(this, Constants.SEND_LOC_REQ, null)
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun deleteMessage(messageId: String, messageType: String) {
        val databaseReference = mRootRef.child(NodeNames.MESSAGES)
            .child(currentUserId).child(chatUserId).child(messageId)
        databaseReference.removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val databaseReferenceChatUser = mRootRef.child(NodeNames.MESSAGES)
                    .child(chatUserId).child(currentUserId).child(messageId)
                databaseReferenceChatUser.removeValue().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            this@ChatActivity,
                            R.string.message_deleted_successfully,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@ChatActivity,
                            getString(R.string.failed_to_delete_message, task.exception),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this@ChatActivity, getString(R.string.failed_to_delete_message, task.exception),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkPermission(context: Context, type: Int, messageModel: MessageModel?) {
        viewModel.checkPermission(context, type)
        observePermissionResult(type, messageModel)
    }

    private fun observePermissionResult(type: Int, messageModel: MessageModel?) {
        if (type == Constants.SEND_LOC_REQ) {
            viewModel.isLocPermissionGrantedForReq.observeOnce(this) {
                if (it)
                    getDeviceLocation(Constants.SEND_LOC_REQ, messageModel)
            }
        } else {
            viewModel.isLocPermissionGrantedForRes.observeOnce(this) {
                if (it)
                    getDeviceLocation(Constants.ACCEPT_LOC_REQ, messageModel)
            }
        }
    }

    private fun getDeviceLocation(locReqType: Int, messageModel: MessageModel?) {
        try {
            if (Constants.IsLocPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        val msgType = if (locReqType == Constants.SEND_LOC_REQ)
                            Constants.MESSAGE_TYPE_LOC_REQ else Constants.MESSAGE_TYPE_LOC
                        viewModel.createMessageBody(
                            "",
                            msgType, locReqType == Constants.SEND_LOC_REQ,
                            lastKnownLocation, this, locReqType == Constants.ACCEPT_LOC_REQ,
                            messageModel
                        )
                    } else {
                        Toast.makeText(
                            this,
                            "Could not get location,Please check gps",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onBackPressed() {
        mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId)
            .child(NodeNames.UNREAD_COUNT).setValue(0)
        super.onBackPressed()
    }
}
package com.example.firechat.chat.presentation.activities

import android.Manifest
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
import com.example.firechat.common.Constants
import com.example.firechat.common.Constants.Companion.STATUS_OFFLINE
import com.example.firechat.common.Constants.Companion.STATUS_ONLINE
import com.example.firechat.common.Constants.Companion.STATUS_TYPING
import com.example.firechat.common.Constants.Companion.currentUserId
import com.example.firechat.common.Extras
import com.example.firechat.common.Util
import com.example.firechat.common.wrapSnapshotToClass
import com.example.firechat.databinding.ActivityChatBinding
import com.example.firechat.databinding.CustomActionBarBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.*
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.util.*
import kotlin.collections.ArrayList

class ChatActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityChatBinding
    private lateinit var databaseReferenceMessages: DatabaseReference
    private lateinit var childEventListener: ChildEventListener
    private lateinit var mRootRef: DatabaseReference
    private lateinit var chatUserId: String
    private lateinit var userName: String
    private lateinit var messagesList: ArrayList<MessageModel>
    private lateinit var messagesAdapter: MessagesAdapter
    private lateinit var listAdapterObserver: RecyclerView.AdapterDataObserver
    private var currentPage = 1
    private val RECORD_PER_PAGE = 30
    private lateinit var database: DatabaseReference
    private lateinit var chatDatabase: DatabaseReference
    private lateinit var toolBarBinding: CustomActionBarBinding
    private lateinit var query: Query
    private lateinit var chatQuery: Query
    private lateinit var valueListener: ValueEventListener
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var lastKnownLocation: Location? = null
    private lateinit var chatValueListener: ValueEventListener
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
            }
        }

        checkUserExists()
        initViews()
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)
        Constants.IsLocPermissionGranted = Util.checkLocationPermission(this)
    }

    private fun checkUserExists() {
        database =
            FirebaseDatabase.getInstance().reference.child(NodeNames.CHATS).child(currentUserId)
                .child(chatUserId)
        chatDatabase =
            FirebaseDatabase.getInstance().reference.child(NodeNames.CHATS).child(chatUserId)
                .child(currentUserId)
        query = database.orderByKey()
        chatQuery = chatDatabase.orderByKey()
        addUserValueListeners()
        query.addListenerForSingleValueEvent(valueListener)

    }

    private fun addUserValueListeners() {
        chatValueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    createChatReceiver()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ChatActivity,
                    "Could not fetch data ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        valueListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    createChat()
                    Toast.makeText(this@ChatActivity, "Chat not Available", Toast.LENGTH_LONG)
                        .show()
                } else {
                    chatQuery.addListenerForSingleValueEvent(chatValueListener)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@ChatActivity,
                    "Could not fetch data ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

        }
    }

    private fun createChat() {
        val databaseReferenceChats = FirebaseDatabase.getInstance().reference.child(NodeNames.CHATS)
        databaseReferenceChats.child(currentUserId).child(chatUserId)
            .child(NodeNames.TIME_STAMP).setValue(ServerValue.TIMESTAMP)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    createChatReceiver()
                }
            }
    }

    private fun createChatReceiver() {
        val databaseReferenceChats = FirebaseDatabase.getInstance().reference.child(NodeNames.CHATS)
        databaseReferenceChats.child(chatUserId).child(currentUserId)
            .child(NodeNames.TIME_STAMP).setValue(ServerValue.TIMESTAMP)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    Toast.makeText(this, "Chat created", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun initViews() {

        binding.ivSend.setOnClickListener(this)
        binding.ivLocation.setOnClickListener(this)

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

        messagesList = ArrayList()
        messagesAdapter = MessagesAdapter(this, messagesList)

        binding.rvMessages.layoutManager = LinearLayoutManager(this)
        binding.rvMessages.adapter = messagesAdapter

        loadMessages()

        messagesAdapter.addAcceptListener { messageModel, i ->
            Toast.makeText(
                this,
                "Accepted ${messageModel.location?.requestingUserLoc}",
                Toast.LENGTH_LONG
            ).show()
            if (Util.connectionAvailable(this)) {
                sendLocCheckPermission(this, messageModel)
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

        mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId)
            .child(NodeNames.UNREAD_COUNT).setValue(0)

        binding.rvMessages.scrollToPosition(messagesList.size - 1)

        binding.srlMessages.setOnRefreshListener {
            currentPage++
            loadMessages()
        }

        if (intent.hasExtra(Extras.MESSAGE) && intent.hasExtra(Extras.MESSAGE_ID) &&
            intent.hasExtra(Extras.MESSAGE_TYPE)
        ) {
            val message = intent.getStringExtra(Extras.MESSAGE)
            val messageId = intent.getStringExtra(Extras.MESSAGE_ID)
            val messageType = intent.getStringExtra(Extras.MESSAGE_TYPE)
            val messageRef: DatabaseReference =
                mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId).push()
            val newMessageId = messageRef.key
            if (messageType == Constants.MESSAGE_TYPE_TEXT) {
                message?.let {
                    sendMessage(it, messageType, newMessageId, true, lastKnownLocation)
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

    private fun acceptHalfwayRequest(
        messageModel: MessageModel, msgType: String, pushId: String?, location: Location?
    ) {
        val currentUserRef = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId
        val chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId
        val messageMap: HashMap<String, Any?> = HashMap<String, Any?>()
        messageMap[NodeNames.MESSAGE_ID] = pushId
        messageMap[NodeNames.MESSAGE] = ""
        messageMap[NodeNames.MESSAGE_TYPE] = msgType
        messageMap[NodeNames.MESSAGE_FROM] = currentUserId
        messageMap[NodeNames.MESSAGE_TO] = chatUserId
        val locationMap: HashMap<String, Any?> = HashMap<String, Any?>()
        locationMap[NodeNames.LOCATION_CURRENT] = messageModel.location?.requestingUserLoc
        locationMap[NodeNames.LOCATION_CHAT] =
            location?.latitude?.plus(0.2).toString() + "[$]" + location?.longitude?.plus(0.2)
                .toString()
        messageMap[NodeNames.LOCATION] = locationMap
        messageMap[NodeNames.MESSAGE_TIME] = ServerValue.TIMESTAMP
        val messageUserMap: HashMap<String, Any> = HashMap<String, Any>()
        messageUserMap["$currentUserRef/$pushId"] = messageMap
        messageUserMap["$chatUserRef/$pushId"] = messageMap
        binding.etMessage.setText("")
        prepareNotification(messageUserMap, msgType, "")
    }

    private fun sendCurrentLocation(pushId: String?, messageModel: MessageModel) {
        try {
            if (Constants.IsLocPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        acceptHalfwayRequest(
                            messageModel,
                            Constants.MESSAGE_TYPE_LOC,
                            pushId,
                            lastKnownLocation
                        )
                    } else {
                        Toast.makeText(this, "Could not send location", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun sendMessage(
        msg: String,
        msgType: String,
        pushId: String?,
        isLocation: Boolean,
        lastKnownLocation: Location?
    ) {
        try {
            val currentUserRef = NodeNames.MESSAGES + "/" + currentUserId + "/" + chatUserId
            val chatUserRef = NodeNames.MESSAGES + "/" + chatUserId + "/" + currentUserId
            if (msg != "") {
                val messageMap: HashMap<String, Any?> = HashMap<String, Any?>()
                messageMap[NodeNames.MESSAGE_ID] = pushId
                messageMap[NodeNames.MESSAGE] = msg
                messageMap[NodeNames.MESSAGE_TYPE] = msgType
                messageMap[NodeNames.MESSAGE_FROM] = currentUserId
                messageMap[NodeNames.MESSAGE_TO] = chatUserId
                messageMap[NodeNames.MESSAGE_TIME] = ServerValue.TIMESTAMP
                val messageUserMap: HashMap<String, Any> = HashMap<String, Any>()
                messageUserMap["$currentUserRef/$pushId"] = messageMap
                messageUserMap["$chatUserRef/$pushId"] = messageMap
                binding.etMessage.setText("")
                prepareNotification(messageUserMap, msgType, msg)
            } else if (isLocation) {
                val messageMap: HashMap<String, Any?> = HashMap<String, Any?>()
                messageMap[NodeNames.MESSAGE_ID] = pushId
                messageMap[NodeNames.MESSAGE] = msg
                messageMap[NodeNames.MESSAGE_TYPE] = msgType
                messageMap[NodeNames.MESSAGE_FROM] = currentUserId
                messageMap[NodeNames.MESSAGE_TO] = chatUserId
                val locationMap: HashMap<String, Any?> = HashMap<String, Any?>()
                locationMap[NodeNames.LOCATION_CURRENT] =
                    lastKnownLocation?.latitude.toString() + "[$]" + lastKnownLocation?.longitude.toString()
                locationMap[NodeNames.LOCATION_CHAT] = ""
                messageMap[NodeNames.LOCATION] = locationMap
                val requestOptions: HashMap<String, Any?> = HashMap<String, Any?>()
                requestOptions[NodeNames.REQ_STATUS] = ""
                requestOptions[NodeNames.REQ_EXPIRY] = ""
                messageMap[NodeNames.REQ_OPTIONS] = requestOptions
                messageMap[NodeNames.MESSAGE_TIME] = ServerValue.TIMESTAMP
                val messageUserMap: HashMap<String, Any> = HashMap<String, Any>()
                messageUserMap["$currentUserRef/$pushId"] = messageMap
                messageUserMap["$chatUserRef/$pushId"] = messageMap
                binding.etMessage.setText("")
                prepareNotification(messageUserMap, msgType, msg)
            }
        } catch (ex: Exception) {
            Toast.makeText(
                this@ChatActivity,
                getString(R.string.failed_to_send_message, ex.message),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun prepareNotification(
        messageUserMap: HashMap<String, Any>,
        msgType: String,
        msg: String
    ) {
        mRootRef.updateChildren(messageUserMap, object : CompletionListener {
            override fun onComplete(
                databaseError: DatabaseError?,
                databaseReference: DatabaseReference
            ) {
                if (databaseError != null) {
                    Toast.makeText(
                        this@ChatActivity,
                        getString(R.string.failed_to_send_message, databaseError.message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                run {
                    Toast.makeText(
                        this@ChatActivity, R.string.message_sent_successfully,
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
                    Util.sendNotification(this@ChatActivity, title, msg, chatUserId)
                    val lastMessage = if (title != "New Message") title else msg
                    Util.updateChatDetails(
                        this@ChatActivity, currentUserId, chatUserId, lastMessage
                    )
                }
            }
        })
    }

    private fun loadMessages() {
        messagesList.clear()
        databaseReferenceMessages =
            mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId)

        val messageQuery =
            databaseReferenceMessages.limitToLast(currentPage * RECORD_PER_PAGE)

        if (this::childEventListener.isInitialized)
            messageQuery.removeEventListener(childEventListener)

        childEventListener = object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val message: MessageModel? =
                    wrapSnapshotToClass(MessageModel::class.java, dataSnapshot)
                message?.let {
                    messagesList.add(it)
                    messagesAdapter.notifyDataSetChanged()
                    binding.rvMessages.scrollToPosition(messagesList.size - 1)
                    binding.srlMessages.isRefreshing = false
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                loadMessages()
            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
            override fun onCancelled(databaseError: DatabaseError) {
                binding.srlMessages.isRefreshing = false
            }
        }
        messageQuery.addChildEventListener(childEventListener)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ivSend -> {
                if (Util.connectionAvailable(this)) {
                    val userMessagePush =
                        mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId)
                            .push()
                    val pushId = userMessagePush.key
                    sendMessage(
                        binding.etMessage.text.toString().trim { it <= ' ' },
                        Constants.MESSAGE_TYPE_TEXT,
                        pushId, false, lastKnownLocation
                    )
                } else {
                    Toast.makeText(this, R.string.no_internet, Toast.LENGTH_SHORT).show()
                }
            }
            R.id.ivLocation -> {
                if (Util.connectionAvailable(this)) {
                    val userMessagePush =
                        mRootRef.child(NodeNames.MESSAGES).child(currentUserId).child(chatUserId)
                            .push()
                    val pushId = userMessagePush.key
                    checkPermission(this, pushId)
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

    override fun onBackPressed() {
        mRootRef.child(NodeNames.CHATS).child(currentUserId).child(chatUserId)
            .child(NodeNames.UNREAD_COUNT).setValue(0)
        super.onBackPressed()
    }

    private fun checkPermission(context: Context, pushId: String?) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Constants.IsLocPermissionGranted = true
                        getDeviceLocation(pushId)
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

    private fun sendLocCheckPermission(
        context: Context,
        messageModel: MessageModel
    ) {
        Dexter.withContext(context)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) { // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {
                        Constants.IsLocPermissionGranted = true
                        val userMessagePush =
                            mRootRef.child(NodeNames.MESSAGES).child(currentUserId)
                                .child(chatUserId)
                                .push()
                        val pushId = userMessagePush.key
                        sendCurrentLocation(pushId, messageModel)
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

    private fun getDeviceLocation(pushId: String?) {
        try {
            if (Constants.IsLocPermissionGranted) {
                val locationResult = fusedLocationProviderClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        lastKnownLocation = task.result
                        sendMessage(
                            binding.etMessage.text.toString().trim { it <= ' ' },
                            Constants.MESSAGE_TYPE_LOC_REQ,
                            pushId,
                            true,
                            lastKnownLocation
                        )
                    } else {
                        Toast.makeText(this, "Could not send location", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        query.removeEventListener(valueListener)
    }
}
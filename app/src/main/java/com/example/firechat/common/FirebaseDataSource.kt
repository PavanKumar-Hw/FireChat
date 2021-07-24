package com.example.firechat.common

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import java.util.HashMap
import javax.inject.Inject
import javax.inject.Singleton

class FirebaseReferenceValueObserver @Inject constructor() {
    private var valueEventListener: ValueEventListener? = null
    private var dbRef: DatabaseReference? = null

    fun start(valueEventListener: ValueEventListener, reference: DatabaseReference) {
        reference.addValueEventListener(valueEventListener)
        this.valueEventListener = valueEventListener
        this.dbRef = reference
    }

    fun clear() {
        valueEventListener?.let { dbRef?.removeEventListener(it) }
        valueEventListener = null
        dbRef = null
    }
}

@Singleton
class FirebaseReferenceChildObserver @Inject constructor() {
    private var valueEventListener: ChildEventListener? = null
    private var dbRef: DatabaseReference? = null
    private var isObserving: Boolean = false

    fun start(valueEventListener: ChildEventListener, reference: DatabaseReference) {
        isObserving = true
        reference.addChildEventListener(valueEventListener)
        this.valueEventListener = valueEventListener
        this.dbRef = reference
    }

    fun clear() {
        valueEventListener?.let { dbRef?.removeEventListener(it) }
        isObserving = false
        valueEventListener = null
        dbRef = null
    }

    fun isObserving(): Boolean {
        return isObserving
    }
}

@Singleton
class FirebaseDataSource @Inject constructor() {

    companion object {
        val dbInstance = FirebaseDatabase.getInstance()
    }

    //region Private

    private fun refToPath(path: String): DatabaseReference {
        return dbInstance.reference.child(path)
    }

    private fun <T> attachChildListenerToBlock(
        resultClassName: Class<T>,
        b: ((Result<T>) -> Unit)
    ): ChildEventListener {
        return (object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                b.invoke(Result.success(wrapSnapshotToClass(resultClassName, snapshot)))
            }

            override fun onCancelled(error: DatabaseError) {
                b.invoke(Result.error(error.message))
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onChildRemoved(snapshot: DataSnapshot) {}
        })
    }

    private fun attachValueListenerToTaskCompletion(src: TaskCompletionSource<DataSnapshot>): ValueEventListener {
        return (object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                src.setException(Exception(error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                src.setResult(snapshot)
            }
        })
    }

    private fun attachUpdateListener(src: (String?) -> Unit): DatabaseReference.CompletionListener? {
        return (DatabaseReference.CompletionListener { databaseError, _ ->
            if (databaseError != null) {
                src.invoke(databaseError.message)
            } else {
                src.invoke(null)
            }
        })
    }

    private fun <T> attachValueListenerToBlock(
        resultClassName: Class<T>,
        b: ((Result<T>) -> Unit)
    ): ValueEventListener {
        return (object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                b.invoke(Result.error(error.message))
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (wrapSnapshotToClass(resultClassName, snapshot) == null) {
                    b.invoke(Result.error(msg = snapshot.key ?: ""))
                } else {
                    b.invoke(Result.success(wrapSnapshotToClass(resultClassName, snapshot)))
                }
            }
        })
    }

    private fun attachValueListenerToBlockValue(b: ((DataSnapshot?, Boolean) -> Unit)): ValueEventListener {
        return (object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                b.invoke(null, true)
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                b.invoke(snapshot, false)
            }
        })
    }
    //endregion

    //region Value Observers
    fun <T> attachMessagesObserver(
        resultClassName: Class<T>,
        messagesID: String,
        refObs: FirebaseReferenceChildObserver,
        b: ((Result<T>) -> Unit)
    ) {
        val listener = attachChildListenerToBlock(resultClassName, b)
        refObs.start(listener, refToPath(messagesID))
    }

    fun sendMessageToUser(messageUserMap: HashMap<String, Any>, b: (String?) -> Unit) {
        dbInstance.reference.updateChildren(messageUserMap, attachUpdateListener(b))
    }

    fun attachUserActiveStatus(
        refPath: String,
        activeStateObserver: FirebaseReferenceValueObserver,
        b: (DataSnapshot?, Boolean) -> Unit
    ) {
        val listener = attachValueListenerToBlockValue(b)
        activeStateObserver.start(listener, refToPath(refPath))
    }

    fun updateUserTypingStatus(refPath: String, status: String) {
        refToPath(refPath).setValue(status)
    }

    fun attachSenderTypingStatus(
        refPath: String,
        typingObserver: FirebaseReferenceValueObserver,
        b: (DataSnapshot?, Boolean) -> Unit
    ) {
        val listener = attachValueListenerToBlockValue(b)
        typingObserver.start(listener, refToPath(refPath))
    }
    //end region
}
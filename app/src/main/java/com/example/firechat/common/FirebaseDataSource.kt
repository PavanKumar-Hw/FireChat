package com.example.firechat.common

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.database.*
import javax.inject.Singleton

@Singleton
class FirebaseReferenceValueObserver {
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
class FirebaseReferenceChildObserver {
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
class FirebaseDataSource {

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
                b.invoke(Result.Success(wrapSnapshotToClass(resultClassName, snapshot)))
            }

            override fun onCancelled(error: DatabaseError) {
                b.invoke(Result.Error(error.message))
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

    //endregion

    //region Value Observers
    fun <T> attachMessagesObserver(
        resultClassName: Class<T>,
        messagesID: String,
        refObs: FirebaseReferenceChildObserver,
        b: ((Result<T>) -> Unit)
    ) {
        val listener = attachChildListenerToBlock(resultClassName, b)
        refObs.start(listener, refToPath("messages/$messagesID"))
    }

    fun <T> createAndObserveChat(chatId: String, b: (Result<T>) -> Unit) {

    }

    private fun attachChatRecordListener(path: String): Task<DataSnapshot> {
        val src = TaskCompletionSource<DataSnapshot>()
        val listener = attachValueListenerToTaskCompletion(src)
        refToPath(path).addListenerForSingleValueEvent(listener)
        return src.task
    }
    //end region
}
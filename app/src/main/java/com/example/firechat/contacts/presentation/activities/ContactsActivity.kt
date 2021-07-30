package com.example.firechat.contacts.presentation.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.firechat.chat.presentation.activities.ChatActivity
import com.example.firechat.common.Constants
import com.example.firechat.common.Extras
import com.example.firechat.contacts.data.models.ContactModel
import com.example.firechat.contacts.ContactsAdapter
import com.example.firechat.databinding.ActivityContactsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

@AndroidEntryPoint
class ContactsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactsBinding
    private lateinit var adapter: ContactsAdapter

    var listUsers = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        listUsers.add("6bOtNlitcKTDxLxrEjdAiyYpM513")
        listUsers.add("OnJzTrm6MtOOqsSjjWTj4y7LbIk2")
        listUsers.add("jwUOcUWEnJWgiGELCqOcQxNga5Y2")
        initViews()
    }

    private fun initViews() {
        adapter = ContactsAdapter()
        adapter.addClickListener { contactModel, i ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(Extras.USER_NAME, contactModel.contactName)
            intent.putExtra(Extras.USER_KEY, contactModel.contactId)
            startActivity(intent)
            finish()
        }
        binding.rcvContacts.adapter = adapter
        adapter.updateContactList(getContactList())
    }

    private fun getContactList(): ArrayList<ContactModel> {
        val list = ArrayList<ContactModel>()
        listUsers.forEach {
            if (it != Constants.currentUserId) {
                when (it) {
                    "jwUOcUWEnJWgiGELCqOcQxNga5Y2" -> {
                        list.add(ContactModel(it, "Pavan Kumar", "", "", "", ""))
                    }
                    "6bOtNlitcKTDxLxrEjdAiyYpM513" -> {
                        list.add(ContactModel(it, "Giridhar", "", "", "", ""))
                    }
                    else -> {
                        list.add(ContactModel(it, "Madhu", "", "", "", ""))
                    }
                }
            }
        }
        return list
    }

}
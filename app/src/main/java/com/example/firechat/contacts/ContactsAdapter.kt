package com.example.firechat.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.databinding.ContactViewBinding

class ContactsAdapter : RecyclerView.Adapter<ContactsViewHolder>() {

    private var contactList: ArrayList<ContactModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val binding = ContactViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ContactsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        holder.onBind(contactList[position],clickFunction)
    }

    override fun getItemCount(): Int = contactList.size

    fun updateContactList(contactListTemp: ArrayList<ContactModel>) {
        contactList.clear()
        contactList.addAll(contactListTemp)
        notifyDataSetChanged()
    }


    private var clickFunction: ((item: ContactModel, pos: Int) -> Unit)? = null

    fun addClickListener(clickFunction: (ContactModel, Int) -> Unit) {
        this.clickFunction = clickFunction
    }
}

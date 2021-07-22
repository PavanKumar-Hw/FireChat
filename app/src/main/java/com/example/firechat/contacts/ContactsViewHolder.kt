package com.example.firechat.contacts

import androidx.recyclerview.widget.RecyclerView
import com.example.firechat.databinding.ContactViewBinding

class ContactsViewHolder(val binding: ContactViewBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun onBind(
        contactModel: ContactModel,
        clickFunction: ((item: ContactModel, pos: Int) -> Unit)?
    ) {

        binding.apply {
            tvUserName.text = contactModel.userName
            root.setOnClickListener {
                clickFunction?.invoke(contactModel, adapterPosition)
            }
        }
    }
}

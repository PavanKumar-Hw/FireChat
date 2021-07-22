package com.example.firechat.chat.presentation.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.firechat.R
import com.example.firechat.chat.data.models.MessageModel
import com.example.firechat.common.Constants
import com.example.firechat.databinding.HalfwayLocMessageLayoutBinding
import java.text.SimpleDateFormat
import java.util.*

class HalfwayLocViewHolder(
    private val binding: HalfwayLocMessageLayoutBinding,
    val context: Context,
    private val actionModeCallBack: ActionMode.Callback
) : RecyclerView.ViewHolder(binding.root) {

    private var actionMode: ActionMode? = null

    fun onBind(message: MessageModel) {
        val currentUserId: String = Constants.currentUserId
        val fromUserId = message.messageFrom
        val sfd = SimpleDateFormat("dd-MM-yyyy HH:mm")
        val dateTime = sfd.format(Date(message.messageTime))
        val splitString = dateTime.split(" ").toTypedArray()
        val messageTime = splitString[1]

        val requestingLocation = message.location?.requestingUserLoc
        val senderLocation = message.location?.senderUserLoc

        val mapUrl = getUrl(requestingLocation, senderLocation)
        binding.apply {
            if (fromUserId == currentUserId) {
                llHalfwaySent.visibility = View.VISIBLE
                llHalfwayReceived.visibility = View.GONE
                tvTimeSent.text = messageTime

                Glide.with(context)
                    .load(mapUrl)
                    .apply {
                        this.error(R.drawable.default_profile).centerCrop()
                    }
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            e?.logRootCauses("LocationLoading")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                    })
                    .into(ivHlafwaySent)

            } else {
                llHalfwaySent.visibility = View.GONE
                llHalfwayReceived.visibility = View.VISIBLE
                tvTimeRecieved.text = messageTime

                Glide.with(context)
                    .load(mapUrl)
                    .placeholder(R.drawable.ic_image)
                    .into(ivHlafwayRecieved)

            }

            clLocShare.setTag(R.id.TAG_MESSAGE, message.message)
            clLocShare.setTag(R.id.TAG_MESSAGE_ID, message.messageId)
            clLocShare.setTag(R.id.TAG_MESSAGE_TYPE, message.messageType)

            clLocShare.setOnLongClickListener(View.OnLongClickListener {
                if (actionMode != null) return@OnLongClickListener false
                MessagesAdapter.selectedView = clLocShare
                actionMode =
                    (context as AppCompatActivity).startSupportActionMode(actionModeCallBack)
                clLocShare.setBackgroundColor(
                    context.getResources().getColor(R.color.colorAccent)
                )
                true
            })
        }
    }

    private fun getUrl(requestingLocation: String?, senderLocation: String?): String {

        val locPrimary = requestingLocation?.split("[$]")
        val locSecondary = senderLocation?.split("[$]")
        val url = "http://maps.google.com/maps/api/staticmap?center=${
            locPrimary?.get(0)
        },${locPrimary?.get(1)}&zoom=9&size=400x400&sensor=false&markers=color:blue%7C${
            locPrimary?.get(0)
        },${locPrimary?.get(1)}&markers=color:blue%7C${
            locSecondary?.get(0)
        },${locSecondary?.get(1)}&key=AIzaSyAXYZlp1wnB7v5VzyHvfMwbYB_eolUDHfE"

        Log.e("LocationUrl", url)

        return url
    }
}
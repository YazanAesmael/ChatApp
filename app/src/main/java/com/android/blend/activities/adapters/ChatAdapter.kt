package com.android.blend.activities.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.blend.R
import com.android.blend.activities.models.MessageList
import com.android.blend.databinding.OptionsMessageLayoutBinding
import com.android.blend.databinding.ReceiveMessageLayoutBinding
import com.android.blend.databinding.SendMessageLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ChatAdapter(
    var context: Context,
    messages: ArrayList<MessageList>?
) : RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private lateinit var messages: ArrayList<MessageList>
    private val itemSent: Int = 1
    private val itemReceived: Int = 2
    var firebaseUser: FirebaseUser? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == itemSent) {
            val view =
                LayoutInflater.from(context).inflate(R.layout.send_message_layout, parent, false)
            SentMessageHolder(view)
        }else {
            val view =
                LayoutInflater.from(context).inflate(R.layout.receive_message_layout, parent, false)
            ReceivedMessageHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        firebaseUser = FirebaseAuth.getInstance().currentUser
        if (messages[position].senderId == firebaseUser!!.uid) {
            return itemSent
        } else {
            return itemReceived
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]

        val view = LayoutInflater.from(context).inflate(R.layout.options_message_layout, null)
        val binding: OptionsMessageLayoutBinding = OptionsMessageLayoutBinding.bind(view)
        val dialog = AlertDialog.Builder(context).setView(binding.root).create()

        return if (holder.javaClass == SentMessageHolder::class.java) {
            val viewHolder = holder as SentMessageHolder
            viewHolder.binding.tvMessageSendLayout.text = message.message.trim()
            viewHolder.itemView.setOnLongClickListener {
                binding.firstOptionOptionsLayout.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance().reference
                            .child("Chat")
                            .child(it1)
                            .setValue(null)
                    }
                    dialog.dismiss()
                }
                binding.secondOptionOptionsLayout.visibility = View.GONE
                binding.thirdOptionOptionsLayout.setOnClickListener {
                    Toast.makeText(context, "sent at:\n${message.timeStamp}\n${message.dateStamp}", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }else {
            val viewHolder = holder as ReceivedMessageHolder
            viewHolder.binding.tvMessageReceiveLayout.text = message.message.trim()
            viewHolder.itemView.setOnLongClickListener {

                binding.firstOptionOptionsLayout.visibility = View.GONE
                binding.secondOptionOptionsLayout.visibility = View.GONE
                binding.thirdOptionOptionsLayout.setOnClickListener {
                    Toast.makeText(context, "sent at:\n${message.timeStamp}\n${message.dateStamp}", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                }
                dialog.show()
                false
            }
        }
    }

    override fun getItemCount(): Int = messages.size

    inner class SentMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: SendMessageLayoutBinding = SendMessageLayoutBinding.bind(itemView)
    }

    inner class ReceivedMessageHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding: ReceiveMessageLayoutBinding = ReceiveMessageLayoutBinding.bind(itemView)
    }

    init {
        if (messages != null) {
            this.messages = messages
        }
    }
}


// Sent
//            if (message.message.equals("photo")) {
//                viewHolder.binding.sentImageView.visibility = View.VISIBLE
//                viewHolder.binding.tvMessageSendLayout.visibility = View.GONE
//                viewHolder.binding.messageSendLayout.visibility = View.GONE
//                Glide.with(context)
//                    .load(message.imageUrl)
//                    .placeholder(R.color.black)
//                    .into(holder.binding.sentImageView)
//            }

// Received
//            if (message.message.equals("photo")) {
//                Log.d("photoCheck", "Adapter")
//                viewHolder.binding.receivedImageView.visibility = View.VISIBLE
//                viewHolder.binding.tvMessageReceiveLayout.visibility = View.GONE
//                viewHolder.binding.messageReceiveLayout.visibility = View.GONE
//                Glide.with(context)
//                    .load(message.imageUrl)
//                    .placeholder(R.color.black)
//                    .into(holder.binding.receivedImageView)
//            }


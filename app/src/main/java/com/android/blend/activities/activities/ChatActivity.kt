@file:Suppress("DEPRECATION")

package com.android.blend.activities.activities

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.blend.R
import com.android.blend.R.drawable.online_presence
import com.android.blend.R.drawable.user_presence
import com.android.blend.activities.adapters.ChatAdapter
import com.android.blend.activities.models.MessageList
import com.android.blend.databinding.ActivityChatBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class ChatActivity : AppCompatActivity() {
    companion object {
        const val log: String = "myLog"
    }
    private var binding: ActivityChatBinding? = null
    private var adapter: ChatAdapter? = null
    private var firebaseUser: FirebaseUser? = null
    private var receiverUid: String? = null
    private var database: FirebaseDatabase? = null
    var messages: ArrayList<MessageList>? = null
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        database = FirebaseDatabase.getInstance()
        messages = ArrayList()
        setContentView(binding!!.root)

        receiverUid = intent.getStringExtra("uid")
        val name = intent.getStringExtra("name")
        val profilePhoto = intent.getStringExtra("image")
        val bioRef = database!!.reference
            .child("/users")
            .child(receiverUid.toString())
            .child("/bio")

        binding!!.chatUserName.text = name
        Glide.with(this@ChatActivity)
            .load(profilePhoto)
            .placeholder(R.color.black)
            .into(binding!!.chatUserProfileImage)

        binding!!.chatUserName.setOnClickListener {
            bioRef.addValueEventListener( object: ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val receiverBio = snapshot.value.toString()
                        if (receiverBio != "null") {
                            binding!!.chatUserName.visibility = View.GONE
                            binding!!.tvStatus.visibility = View.VISIBLE
                            binding!!.tvStatus.text = receiverBio
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
        binding!!.tvStatus.setOnClickListener {
            binding!!.tvStatus.visibility = View.GONE
            binding!!.chatUserName.visibility = View.VISIBLE
        }

        val topAppBar = binding!!.appBarLayout
        topAppBar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.arrow_back_button)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        database!!.reference.child("presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("UseCompatLoadingForDrawables", "SetTextI18n")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status == "Offline") {
                            binding!!.chatUserName.text = name
                            val offLinePresence: Drawable? = getDrawable(user_presence)
                            binding!!.chatUserName.background = offLinePresence
                            return
                        }
                        if (status == "typing...") {
                            binding!!.chatUserName.text = "typing..."
                            val onlinePresence: Drawable? = getDrawable(online_presence)
                            binding!!.chatUserName.background = onlinePresence
                        } else {
                            binding!!.chatUserName.text = name
                            val onlinePresence: Drawable? = getDrawable(online_presence)
                            binding!!.chatUserName.background = onlinePresence
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        adapter = ChatAdapter(this@ChatActivity, messages)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        binding!!.rvLinearLeft.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.rvLinearLeft.adapter = adapter
        binding!!.rvLinearLeft.smoothScrollToPosition(adapter!!.itemCount)

        val handler = Handler()
        binding!!.etMessageBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                database!!.reference.child("presence")
                    .child(firebaseUser!!.uid)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            var userStoppedTyping = Runnable {
                database!!.reference.child("presence")
                    .child(firebaseUser!!.uid)
                    .setValue("Online")
            }
        })

        binding!!.sendButtonMessageBox.setOnClickListener {
            val messageText: String = binding!!.etMessageBox.text.toString()
            if (messageText.trim().isEmpty()) {
                return@setOnClickListener
            }
            sendMessage(firebaseUser!!.uid, receiverUid!!, messageText)
        }
        receiveMessage(firebaseUser!!.uid, receiverUid!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessage(senderId: String, receiverId: String, message: String) {
        binding!!.etMessageBox.setText("")
        binding!!.etMessageBox.hint = "Sent!"

        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference
        val randomId = reference.push().key.toString().trim()

        fun timeMain(): String {
            val simpleTime = SimpleDateFormat.getTimeInstance()
            return simpleTime.format(Date())
        }
        fun dateMain(): String {
            val simpleDate = SimpleDateFormat.getDateInstance()
            return simpleDate.format(Date())
        }
        val dateMain = dateMain()
        val timeMain = timeMain()

        val hashMap: HashMap<String, String> = HashMap()
        hashMap["senderId"] = senderId
        hashMap["receiverId"] = receiverId
        hashMap["message"] = message
        hashMap["messageId"] = randomId
        hashMap["timeStamp"] = timeMain()
        hashMap["dateStamp"] = dateMain()
        reference.child("Chat").child(randomId).setValue(hashMap)

        latestMessage(message, randomId, senderId, receiverId, dateMain, timeMain)
    }

    private fun latestMessage(message: String,  messageId: String, senderId: String, receiverId: String, dateMain: String, timeMain: String) {
        val reference: DatabaseReference = FirebaseDatabase.getInstance().reference
        reference.child("/Latest Messages")
            .child(senderId.trim())
            .child(receiverId.trim())
            .child("/last message")
            .setValue(message)

        reference.child("/Latest Messages")
            .child(senderId.trim())
            .child(receiverId.trim())
            .child("/last message time")
            .setValue(timeMain)

        reference.child("/Latest Messages")
            .child(senderId.trim())
            .child(receiverId.trim())
            .child("/last message date")
            .setValue(dateMain)

        reference.child("/Latest Messages")
            .child(receiverId.trim())
            .child(senderId.trim())
            .child("/last message time")
            .setValue(timeMain)

        reference.child("/Latest Messages")
            .child(receiverId.trim())
            .child(senderId.trim())
            .child("/last message date")
            .setValue(dateMain)

        reference.child("/Latest Messages")
            .child(receiverId.trim())
            .child(senderId.trim())
            .child("/last message")
            .setValue(message)
    }

    private fun receiveMessage(senderId: String?, receiverId: String?){
        val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("Chat")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messages!!.clear()
                for (dataSnapShot: DataSnapshot in snapshot.children) {
                    val messageList: MessageList? = dataSnapShot.getValue(MessageList::class.java)


                    if (messageList!!.senderId == senderId && messageList.receiverId == receiverId ||
                        messageList.senderId == receiverId && messageList.receiverId == senderId
                    ) {
                        messages!!.add(messageList)
                    }
                }
                adapter = ChatAdapter(this@ChatActivity, messages)
                binding!!.rvLinearLeft.adapter = adapter
                binding!!.rvLinearLeft.smoothScrollToPosition(adapter!!.itemCount)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!)
            .setValue("Offline")
    }
}

//         binding!!.attachmentButtonMessageBox.setOnClickListener {
//            val intent = Intent()
//            intent.action = Intent.ACTION_GET_CONTENT
//            intent.type = "image/"
//            startActivityForResult(intent, 25)
//        }

//    @Deprecated("Deprecated in Java")
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if(requestCode == 25){
//            if(data != null){
//                if(data.data != null) {
//                    val selectedImage = data.data
//                    val calender = Calendar.getInstance()
//                    val refence = storage!!.reference.child("chats")
//                        .child(calender.timeInMillis.toString() + "")
//                    dialog!!.show()
//                    refence.putFile(selectedImage!!).addOnCompleteListener{ task->
//                        dialog!!.dismiss()
//                        if(task.isSuccessful) {
//                            refence.downloadUrl.addOnSuccessListener { uri->
//                                val filePath: String = uri.toString()
//                                val messageText: String = binding!!.etMessageBox.text.toString()
//                                val date = Date()
//                                val message  = Message(messageText, senderUid, date.time)
//                                message.message = "photo"
//                                message.imageUrl = filePath
//                                binding!!.etMessageBox.setText("")
//                                val randomKey = database!!.reference.push().key
//                                val lastMessageObj = HashMap<String, Any>()
//                                lastMessageObj["lastMessage"] = message.message!!
//                                lastMessageObj["lastMessageTime"] = date.time
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }















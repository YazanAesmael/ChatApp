package com.android.blend.activities.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.blend.R
import com.android.blend.activities.activities.ChatActivity
import com.android.blend.activities.activities.ChatActivity.Companion.log
import com.android.blend.activities.models.User
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LatestChatsAdapter(var context: Context, private var userList: ArrayList<User>): RecyclerView.Adapter<LatestChatsAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_message_user, parent, false)
        return  ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.username
        val database = FirebaseDatabase.getInstance()
        database.reference
            .child("/Latest Messages")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(user.uid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastMessage =snapshot.child("/last message").value
                    Log.d(log, lastMessage.toString())
                    holder.userLastMessage.text = lastMessage.toString()
                }
                override fun onCancelled(error: DatabaseError) {}

            })

        database.reference
            .child("/Latest Messages")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(user.uid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastMessageDate =snapshot.child("/last message date").value
                    Log.d(log, lastMessageDate.toString())
                    holder.dateStamp.text = lastMessageDate.toString()
                }
                override fun onCancelled(error: DatabaseError) {}

            })

        database.reference
            .child("/Latest Messages")
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .child(user.uid)
            .addValueEventListener(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lastMessageTime =snapshot.child("/last message time").value
                    Log.d(log, lastMessageTime.toString())
                    holder.timeStamp.text = lastMessageTime.toString()
                }
                override fun onCancelled(error: DatabaseError) {}

            })

        Glide.with(context)
            .load(user.profileImageUrl)
            .placeholder(R.color.black)
            .into(holder.userImage)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("name", user.username)
            intent.putExtra("image", user.profileImageUrl)
            intent.putExtra("uid", user.uid)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var userImage: ImageView
        var userName: TextView
        var userLastMessage: TextView
        var timeStamp: TextView
        var dateStamp: TextView

        init {
            userImage = itemView.findViewById(R.id.circle_image_view)
            userName = itemView.findViewById(R.id.user_name_database)
            userLastMessage = itemView.findViewById(R.id.user_last_message_database)
            timeStamp = itemView.findViewById(R.id.tv_time_stamp)
            dateStamp = itemView.findViewById(R.id.tv_date_stamp)
        }
    }
}
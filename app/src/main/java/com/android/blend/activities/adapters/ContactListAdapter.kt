package com.android.blend.activities.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.blend.R
import com.android.blend.activities.activities.ChatActivity
import com.android.blend.activities.models.User
import com.bumptech.glide.Glide

class ContactListAdapter(var context: Context, var userList: ArrayList<User>): RecyclerView.Adapter<ContactListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.latest_message_user, parent, false)
        return  ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val user = userList[position]
            holder.userName.text = user.username
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
        init {
            userImage = itemView.findViewById(R.id.iv_user_photo)
            userName = itemView.findViewById(R.id.tv_last_message_user_name)

            itemView.setOnClickListener {
                val position: Int = adapterPosition
            }
        }
    }
}
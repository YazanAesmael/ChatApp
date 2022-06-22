package com.android.blend.activities.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.blend.R
import com.android.blend.activities.adapters.LatestChatsAdapter
import com.android.blend.activities.models.User
import com.android.blend.databinding.ActivityLatestChatsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LatestChatsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLatestChatsBinding
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: LatestChatsAdapter? = null
    private lateinit var auth: FirebaseAuth
    private var users: ArrayList<User>? = null
    private var database: FirebaseDatabase? = null
    var user: User? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLatestChatsBinding.inflate(LayoutInflater.from(this))
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        layoutManager = LinearLayoutManager(this)
        binding.rvLatestMessages.layoutManager = layoutManager
        adapter = LatestChatsAdapter(this@LatestChatsActivity, users!!)
        binding.rvLatestMessages.adapter = adapter

        val newChatButton = binding.newChatButton
        newChatButton.setOnClickListener {
            val intent = Intent(this@LatestChatsActivity, ContactListActivity::class.java)
            startActivity(intent)
        }

        val topAppBar = binding.topAppBarMenu
        topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_settings -> {
                    val intent = Intent(this@LatestChatsActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        database!!.reference.child("users")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    users!!.clear()
                    for (snapshot1 in snapshot.children) {
                        val user: User? = snapshot1.getValue(User::class.java)
                        val currentUser = FirebaseAuth.getInstance().uid.toString()

                        if (user != null && user.uid != currentUser) {
                            database!!.reference
                                .child("/Latest Messages")
                                .child(user.uid)
                                .child(currentUser)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (snapshot in snapshot.children) {
                                            if (snapshot.exists()) {
                                                users!!.remove(user)
                                                users!!.add(user)
                                            }
                                            adapter!!.notifyDataSetChanged()
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Offline")
    }
}
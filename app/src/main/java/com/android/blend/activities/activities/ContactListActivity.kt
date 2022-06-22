package com.android.blend.activities.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.blend.R
import com.android.blend.activities.adapters.ContactListAdapter
import com.android.blend.activities.models.User
import com.android.blend.databinding.ActivityContactListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ContactListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContactListBinding
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var userContactListAdapter: ContactListAdapter? = null
    var users: ArrayList<User>? = null
    var database: FirebaseDatabase? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        val topAppBar = binding.topAppBar
        topAppBar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.arrow_back_button)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

        val rvContactList = binding.rvContactList

        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        layoutManager = LinearLayoutManager(this)
        rvContactList.layoutManager = layoutManager
        userContactListAdapter = ContactListAdapter(this@ContactListActivity, users!!)
        rvContactList.adapter = userContactListAdapter

        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }
                override fun onCancelled(error: DatabaseError) {}
            })

        rvContactList.adapter = userContactListAdapter
        database!!.reference
            .child("users")
            .addValueEventListener( object : ValueEventListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot1 in snapshot.children){
                    val user: User?= snapshot1.getValue(User::class.java)
                    if (user!!.uid != FirebaseAuth.getInstance().uid) users!!.add(user)
                }
                userContactListAdapter!!.notifyDataSetChanged()
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
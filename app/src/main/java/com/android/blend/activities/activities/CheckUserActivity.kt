package com.android.blend.activities.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CheckUserActivity : AppCompatActivity() {
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK

        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        databaseReference = Firebase.database.reference
        databaseReference
            .child("/users")
            .child(currentUser)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val intent = Intent(this@CheckUserActivity, LatestChatsActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val intent = Intent(this@CheckUserActivity, SignUpActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
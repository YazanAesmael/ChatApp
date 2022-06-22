package com.android.blend.activities.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.blend.R
import com.android.blend.activities.models.User
import com.android.blend.databinding.ActivityUserDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class UserDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserDetailsBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDetailsBinding.inflate(LayoutInflater.from(this))
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()
        val phoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()

        binding.frameProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        binding.finishButton.setOnClickListener {
            uploadSelectedImageToFirebaseStorage()
        }
    }

    private var selectedProfilePicture: Uri? = null
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && data != null) {
            selectedProfilePicture = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedProfilePicture)
            val flProfilePicture = binding.frameProfilePicture
            val selectedCircleFrame = binding.selectedPictureCircleFrame
            selectedCircleFrame.setImageBitmap(bitmap)
            flProfilePicture.alpha = 0f
        }
    }

    private fun uploadSelectedImageToFirebaseStorage() {
        if (selectedProfilePicture == null) {
            return
        }
        val fileName = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")

        ref.putFile(selectedProfilePicture!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()
                    saveUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener {
                saveUserToDatabase(it.toString())
            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val tilUserName = binding.tilUserName.text.toString()
        val tilPhoneNumber = FirebaseAuth.getInstance().currentUser?.phoneNumber.toString()
        val uid = FirebaseAuth.getInstance().uid.toString()
        val database = Firebase.database(getString(R.string.AdminDatabaseURL))
        val myRef = database.getReference("/users/$uid")
        val user = User(uid, tilPhoneNumber, tilUserName, profileImageUrl)

        myRef.setValue(user)
            .addOnSuccessListener {
                val intent = Intent(this@UserDetailsActivity, CheckUserActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(baseContext, "Something went wrong, try again.", Toast.LENGTH_SHORT).show()
            }
    }
}
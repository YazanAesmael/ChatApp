package com.android.blend.activities.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.blend.R
import com.android.blend.databinding.ActivitySettingsBinding
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private var firebaseUser: FirebaseUser? = null
    private lateinit var databaseReference: DatabaseReference
    private var database: FirebaseDatabase? = null
    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(LayoutInflater.from(this))
        databaseReference = Firebase.database.reference
        database = FirebaseDatabase.getInstance()
        firebaseUser = FirebaseAuth.getInstance().currentUser
        setContentView(binding.root)

        val topAppBar = binding.topAppBar
        topAppBar.navigationIcon = ContextCompat.getDrawable(this, R.drawable.arrow_back_button)
        topAppBar.setNavigationOnClickListener {
            finish()
        }

            val currentUser = firebaseUser!!.uid
            databaseReference.child("/users")
                .child(currentUser)
                .child("/profileImageUrl")
                .get()
                .addOnSuccessListener {
                    val profilePhoto = it.value
                    Glide.with(this@SettingsActivity)
                        .load(profilePhoto)
                        .placeholder(R.drawable.user_profile_picture)
                        .into(binding.selectedPictureCircleFrame)
                }
            databaseReference.child("/users")
                .child(currentUser)
                .child("/username")
                .get()
                .addOnSuccessListener {
                    val userName = it.value.toString()
                    binding.userNameTil.hint = userName
                }
        databaseReference.child("/users")
            .child(currentUser)
            .child("/bio")
            .get()
            .addOnSuccessListener {
                val userBio = it.value.toString()
                if (userBio == "null"){
                    binding.userBirthdayTil.hint = "Bio"
                }else {
                    binding.userBioTil.hint = userBio
                }
            }
        databaseReference.child("/users")
            .child(currentUser)
            .child("/phoneNumber")
            .get()
            .addOnSuccessListener {
                    val userNumber = it.value.toString()
//                    binding.userPhoneNumberTil.hint = userNumber
                }
        databaseReference.child("/users")
            .child(currentUser)
            .child("/birthday")
            .get()
            .addOnSuccessListener {
                val userBirthday = it.value.toString()
                binding.userBirthdayTil.hint = userBirthday
            }

        binding.selectedPictureCircleFrame.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        binding.savePhotoTickButton.setOnClickListener {
            uploadSelectedImageToFirebaseStorage()
        }
        binding.deletePhotoButton.setOnClickListener {
            deleteCurrentPhoto()
        }
        binding.updateNameCheck.setOnClickListener {
            updateName()
        }
        binding.updateBioTick.setOnClickListener {
            updateBio()
        }
        binding.editBirthdayTick.setOnClickListener {
            updateBirthday()
        }
        binding.deleteAccountButton.setOnClickListener {
            deleteAccount()
        }
        binding.signOutButton.setOnClickListener {
            val intent = Intent(this@SettingsActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
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

            Glide.with(this@SettingsActivity)
                .load(bitmap)
                .placeholder(R.drawable.user_profile_picture)
                .into(binding.selectedPictureCircleFrame)
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
                    val imageUrl = it.toString()
                    uploadNewPhoto(imageUrl)
                }
            }
            .addOnFailureListener {
                return@addOnFailureListener
            }
    }

    private fun uploadNewPhoto(profileImageUrl: String) {
        databaseReference = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        databaseReference
            .child("/users")
            .child(currentUser)
            .child("/profileImageUrl")
            .setValue(profileImageUrl)
    }

    private fun deleteCurrentPhoto() {
        databaseReference = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        databaseReference
            .child("/users")
            .child(currentUser)
            .child("/profileImageUrl")
            .setValue("")
    }

    private fun updateName() {
        val tilUserName = binding.userNameTil.text.toString()
        databaseReference = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        databaseReference
            .child("/users")
            .child(currentUser)
            .child("/username")
            .setValue(tilUserName)
    }

    private fun updateBio() {
        val tilBio = binding.userBioTil.text.toString()
        databaseReference = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        databaseReference
            .child("/users")
            .child(currentUser)
            .child("/bio")
            .setValue(tilBio)
    }

    private fun updateBirthday() {
        val tilBirthday = binding.userBirthdayTil.text.toString()
        databaseReference = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        databaseReference
            .child("/users")
            .child(currentUser)
            .child("/birthday")
            .setValue(tilBirthday)
    }

    private fun deleteAccount() {
        databaseReference = Firebase.database.reference
        firebaseUser = FirebaseAuth.getInstance().currentUser
        val currentUser = firebaseUser!!.uid
        val userData = databaseReference.child("/users").child(currentUser)
//        val user = Firebase.auth.currentUser!!
//        user.delete()
        userData.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Account deleted successfully.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this@SettingsActivity, CheckUserActivity::class.java)
                    startActivity(intent)
                    finish()
                }else {
                    Toast.makeText(baseContext, "Something went wrong.", Toast.LENGTH_SHORT).show()
                }
            }
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
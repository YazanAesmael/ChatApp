package com.android.blend.activities.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.blend.databinding.ActivityAuthenticatePhoneBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class AuthenticatePhoneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthenticatePhoneBinding
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticatePhoneBinding.inflate(LayoutInflater.from(this))
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)

        val storedVerificationId=intent.getStringExtra("storedVerificationId")
        val tilConfirmationCode = binding.tilConfirmationCode
        val nextButton = binding.nextButton

        nextButton.setOnClickListener {

            val confirmationCode=tilConfirmationCode.text.toString()
            if(confirmationCode.isNotEmpty()){
                val credential : PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    storedVerificationId.toString(), confirmationCode)
                signInWithPhoneAuthCredential(credential)
            }else{
                tilConfirmationCode.error = "Enter 6 Digit Confirmation Code"
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(baseContext, "Verification Completed", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@AuthenticatePhoneActivity, UserDetailsActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        val tilConfirmationCode = binding.tilConfirmationCode
                        tilConfirmationCode.error = "Enter Valid Confirmation Code Firebase"
                    }
                }
            }
        }
    }
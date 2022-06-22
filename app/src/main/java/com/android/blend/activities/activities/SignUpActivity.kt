package com.android.blend.activities.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.blend.databinding.ActivitySignUpBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    lateinit var storedVerificationId: String
    lateinit var resendToken: PhoneAuthProvider.ForceResendingToken

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(LayoutInflater.from(this))
        auth = FirebaseAuth.getInstance()
        setContentView(binding.root)

        val tilPhoneNumber = binding.tilPhoneNumber
        val startButton = binding.finishButton

        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Toast.makeText(baseContext, "Verification Completed", Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(baseContext, "Verification Failed", Toast.LENGTH_SHORT).show()
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                Toast.makeText(baseContext, "Code Sent ", Toast.LENGTH_SHORT).show()
                storedVerificationId = verificationId
                resendToken = token
                val intent = Intent(applicationContext, AuthenticatePhoneActivity::class.java)
                intent.putExtra("storedVerificationId", storedVerificationId)
                startActivity(intent)
                finish()
            }
        }

        startButton.setOnClickListener {
            if (TextUtils.isEmpty(tilPhoneNumber.text.toString())) {
                tilPhoneNumber.error = "Enter valid phone number"
            } else {
                login()
            }
        }
    }

    private fun login() {
        val mobileNumber = binding.tilPhoneNumber
        val number = mobileNumber.text.toString().trim()

        if (number.isNotEmpty()) {
            sendVerificationCode(number)
        } else {
            mobileNumber.error = "Enter a valid phone number"
        }
    }

    private fun sendVerificationCode(number: String) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
}















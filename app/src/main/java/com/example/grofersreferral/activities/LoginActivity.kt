package com.example.grofersreferral.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.grofersreferral.R
import com.example.grofersreferral.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.*
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity()  {

    lateinit var sharedPreferences: SharedPreferences
    private lateinit var binding: ActivityLoginBinding
    var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (isLoggedIn)
        {
                val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                intent.putExtra("Mode", "Customer")
                startActivity(intent)
                finish()

        }

        mAuth = FirebaseAuth.getInstance()


        binding.etPhone.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
                binding.getOtp.visibility =View.VISIBLE
            }

            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {

            }
        })


        binding.getOtp.setOnClickListener {
            if (validatePhone()) {
                binding.loginProgress.visibility = View.VISIBLE
                binding.etPhone.isEnabled = false
                binding.getOtp.isEnabled = false
                val phoneNumber = "+91" + binding.etPhone.text.toString()
                sendVerificationCode(phoneNumber)
            }
        }
    }

    private fun validatePhone(): Boolean {
        val userPhone: String = binding.etPhone.text.toString().trim()
        return if (userPhone.length == 10) {
            true
        } else {
            Toast.makeText(this@LoginActivity, "Enter a valid mobile number", Toast.LENGTH_SHORT).show()
            false
        }
    }


    private fun sendVerificationCode(phoneNumber : String) {
        val build = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber(phoneNumber)
            .setActivity(this@LoginActivity)
            .setTimeout(50L, TimeUnit.SECONDS)
            .setCallbacks(mCallBack)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(build)


    }
    private val mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
            super.onCodeSent(p0, p1)

            val intent = Intent(this@LoginActivity, LoginOTPActivity::class.java)
            intent.putExtra("otpCode", p0)
            intent.putExtra("mobileNo", "+91"+binding.etPhone.text.trim())
            intent.putExtra("resendingToken", p1)
            startActivity(intent)
            binding.loginProgress.visibility = View.GONE


        }

        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.d("hi", "onVerificationCompleted")

        }

        override fun onVerificationFailed(p0: FirebaseException) {
            binding.etPhone.text.clear()
            binding.loginProgress.visibility = View.GONE
            if (p0.javaClass.canonicalName == "com.google.firebase.FirebaseTooManyRequestsException") {
                    Toast.makeText(this@LoginActivity, "Too many attempts. Try after sometime",
                        Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@LoginActivity, p0.toString(),
                    Toast.LENGTH_LONG).show()
            }

        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        this.finishAffinity()
    }


}
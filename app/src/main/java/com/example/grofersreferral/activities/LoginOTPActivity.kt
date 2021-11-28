package com.example.grofersreferral.activities

import android.content.*
import android.content.ContentValues.TAG
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.grofersreferral.R
import com.example.grofersreferral.databinding.ActivityLoginOtpBinding
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class LoginOTPActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    var otpCode:String = ""
    private var etotpCode = ""
    lateinit var mobileNo:String
    lateinit var resendingToken: ForceResendingToken
    private lateinit var otpReciver:BroadcastReceiver

    private lateinit var binding: ActivityLoginOtpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        otpCode = intent.getStringExtra("otpCode").toString()
        mobileNo = intent.getStringExtra("mobileNo").toString()
        resendingToken = intent.getParcelableExtra("resendingToken")!!
        startSmartUserConsent()
        startCountdown()

        binding.enterManually.setOnClickListener {
            binding.autoOtpRetrieval.visibility = View.GONE
            binding.mainContent.visibility = View.VISIBLE
            binding.etOTP.isEnabled = true
            binding.btnLogin.isEnabled = true
            binding.btnLogin.visibility = View.VISIBLE
        }

        binding.btnLogin.setOnClickListener {
            loginUsingOTP()
        }

    }


    private fun loginUsingOTP() {
        if (validateOTP()) {
            binding.loginProgress.visibility = View.VISIBLE
            etotpCode = binding.etOTP.text.toString()
            val credential = PhoneAuthProvider.getCredential(otpCode, etotpCode)
            mAuth.signInWithCredential(credential).addOnCompleteListener(this@LoginOTPActivity) { task ->
                if (task.isSuccessful) {
                    binding.loginProgress.visibility = View.GONE
                    val customercheck = FirebaseFirestore.getInstance()
                    customercheck.collection("Users")
                        .document(mAuth.currentUser?.uid!!).get()
                        .addOnSuccessListener { document ->
                        if (document.data != null) {

                            val intent = Intent(this@LoginOTPActivity, HomeActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

                            val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putBoolean("isLoggedIn", true).apply()
                            editor.commit()
                            startActivity(intent)
                            finish()
                        } else {
                            val intent = Intent(this, RegisterActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name), Context.MODE_PRIVATE)
                            sharedPreferences?.edit()?.clear()?.apply()
                            startActivity(intent)
                            this@LoginOTPActivity.finish()
                        }
                    }.addOnFailureListener {
                        Toast.makeText(this@LoginOTPActivity,"1st err $it", Toast.LENGTH_LONG).show()
                        binding.loginProgress.visibility = View.GONE
                        binding.etOTP.text.clear()
                    }

                } else {
                    Toast.makeText(this, "Something is wrong", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener { exception ->
                    if (exception.javaClass.canonicalName ==
                        "com.google.firebase.auth.FirebaseAuthInvalidCredentialsException") {
                        Toast.makeText(this@LoginOTPActivity,"Incorrect Otp", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(
                            this@LoginOTPActivity,
                            "database error -- " + exception,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    binding.loginProgress.visibility = View.GONE
                    binding.etOTP.text.clear()
                }
        }
    }


    private fun startSmartUserConsent() {
        val client = SmsRetriever.getClient(this)
        client.startSmsRetriever().addOnFailureListener {
            Log.d("errr",it.toString())
        }
    }


    private fun registerBroadcastReciever(){
        otpReciver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (SmsRetriever.SMS_RETRIEVED_ACTION == intent?.action) {
                    val extras = intent.extras
                    val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                    when(smsRetrieverStatus.statusCode){
                        CommonStatusCodes.SUCCESS -> {
                            val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                            binding.enterManually.visibility = View.INVISIBLE
                            getOtpFromMessage(message)
                        }
                        CommonStatusCodes.TIMEOUT -> {

                        }
                    }
                }
            }

        }

        val intentfilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(otpReciver,intentfilter)
    }


    override fun onStart() {
        super.onStart()
        registerBroadcastReciever()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(otpReciver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200) {
            if (resultCode == RESULT_OK && data != null) {
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        }
    }

    private fun getOtpFromMessage(message: String?) {
        val otpPattern = Pattern.compile("(|^)\\d{6}")
        Log.d("otp", message.toString())
        val matcher = otpPattern.matcher(message.toString())
        if (matcher.find()) {
            binding.etOTP.setText(matcher.group(0))
            loginUsingOTP()
        }
    }

    private fun startCountdown() {
        object : CountDownTimer(60000, 1000) {
            var text = ""

            override fun onTick(millisUntilFinished: Long) {
                text = "Resend OTP in ${millisUntilFinished / 1000} seconds"
                binding.resendCountdown.text = text
            }

            override fun onFinish() {
                binding.resendCountdown.setText("Resend OTP")
                binding.resendCountdown.paint.isUnderlineText = true
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                    binding.resendCountdown.setTextColor(getColor(R.color.purple_700))
                binding.resendCountdown.setOnClickListener {
                    resendVerificationCode(mobileNo, resendingToken)
                }
            }
        }.start()
    }

    private fun resendVerificationCode(
        phoneNumber: String,
        token: ForceResendingToken
    ) {
        binding.resendProgress.visibility = View.VISIBLE
        val options = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallBack) // OnVerificationStateChangedCallbacks
            .setForceResendingToken(token) // ForceResendingToken from callbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onCodeSent(p0: String, p1: ForceResendingToken) {
            super.onCodeSent(p0, p1)
            binding.resendProgress.visibility = View.GONE
            binding.resendCountdown.paint.isUnderlineText = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                binding.resendCountdown.setTextColor(getColor(R.color.purple_700))
            Toast.makeText(this@LoginOTPActivity, "Sent", Toast.LENGTH_SHORT).show()
            startCountdown()
        }

        override fun onVerificationCompleted(p0: PhoneAuthCredential) {
            Log.d(TAG, "onVerificationCompleted")
        }

        override fun onVerificationFailed(p0: FirebaseException) {
            binding.etOTP.text.clear()
            binding.loginProgress.visibility = View.GONE
            binding.resendProgress.visibility = View.GONE
            if (p0.javaClass.canonicalName == "com.google.firebase.FirebaseTooManyRequestsException") {
                Toast.makeText(this@LoginOTPActivity, "Too many Attempts",
                    Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@LoginOTPActivity, "$p0", Toast.LENGTH_LONG).show()
            }

        }
    }


    private fun validateOTP(): Boolean {
        etotpCode = binding.etOTP.text.toString()
        return if (etotpCode == "") {
            Toast.makeText(this@LoginOTPActivity, "Please enter OTP", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }

    }
}
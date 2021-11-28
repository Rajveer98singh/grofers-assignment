package com.example.grofersreferral.activities

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.grofersreferral.R
import com.example.grofersreferral.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.StringBuilder
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class RegisterActivity : AppCompatActivity() {


    lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityRegisterBinding
    var name:String=""
    var code:String=""
    var referred:Boolean=false
    var refereeid:String=""

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
// creting instance for firebaseAuth
        mAuth = FirebaseAuth.getInstance()
// on pressing signup button it will first validate the name and then the referral code
        binding.signUpConfirm.setOnClickListener {
            if (validateName()){
                validateCode()
            }
        }
    }

    // this is for validating name, if user name has characters less than 3 then it will return false else true
    private fun validateName(): Boolean {
        var userName = binding.etName.text.trim()
        userName= userName.replace("\\s+".toRegex(), " ")
        val len = userName.length
        return if (len >= 3) {
            name=userName
            true
        } else {
            Toast.makeText(this, "Username should be at least 3 characters", Toast.LENGTH_SHORT)
                .show()
            false
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun validateCode() {
// it will validate code if length of referral code box is 0 then it is valid then we assumes that user
// don't have a referral code. if the length of string in referral input box is not zero then it will check in
// firebase for the existing referral code

        var codeX = binding.etReferral.text.trim()
        codeX= codeX.replace("\\s+".toRegex(), " ")

        val len = codeX.length

        if(len==0){
            referred=false
            addUserDetails1(false,"")

        }else if (len == 5) {
            val collectionReference = FirebaseFirestore.getInstance().collection("Users")
            val query: Query = collectionReference
                .whereEqualTo("refCode", codeX).limit(1)
            query.get().addOnSuccessListener {
                if(it.isEmpty){
                    Toast.makeText(this, "Invalid reference code", Toast.LENGTH_SHORT).show()
                }else{
                    code=codeX
                    for (doc in it) {
                        val user: MutableMap<String, Any> =
                            HashMap()
                        val count=doc.getLong("refCount")
                        user["refCount"]=count!!+1
                        FirebaseFirestore.getInstance().collection("Users").document(doc.id).update(user)
                        referred = true
                        refereeid = doc.id
                        addUserDetails1(true,refereeid)
                    }


                }
            }
        } else {
            Toast.makeText(this, "Code should 5 characters long", Toast.LENGTH_SHORT)
                .show()
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun addUserDetails0(genCode:String, referred:Boolean, referredid:String) {
        val userID= mAuth.currentUser?.uid!!
        val collectionReference = FirebaseFirestore.getInstance().collection("Users")
        val documentReference: DocumentReference =
            FirebaseFirestore.getInstance().collection("Users").document(userID)
        val newCode=genCode
// here we will register user details(name,userid,mobile number,referrerd,enrolled,refcode,refcount,referredid,timestamp) in firebase
        val user: MutableMap<String, Any> =
            HashMap()
        user["name"] = name
        user["userId"]=userID
        user["mobileNo"]= mAuth.currentUser?.phoneNumber!!
        user["referred"] = referred
        user["enrolled"]=false
        user["refCode"]=newCode
        user["refCount"]=0
        user["referredid"] = referredid
        user["timestamp"] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        documentReference.set(user).addOnSuccessListener {
            val sharedPreferences = getSharedPreferences(getString(R.string.preference_file_name),Context.MODE_PRIVATE)
            val editor = sharedPreferences?.edit()
            editor?.putBoolean("isLoggedIn", true)?.apply()
            editor?.putBoolean("Customer",true)
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addUserDetails1(referred:Boolean, referredid:String) {
        val userID= mAuth.currentUser?.uid!!
        val collectionReference = FirebaseFirestore.getInstance().collection("Users")
        val documentReference: DocumentReference =
            FirebaseFirestore.getInstance().collection("Users").document(userID)

        // here a random string of referral code is generated using the randomString() function
            val genCode=randomString()
        // this will check in Firebase if the generated referral code matches any other previously generated referral code as every user
        // has unique referral code
            collectionReference.whereEqualTo("refCode", genCode).get().addOnSuccessListener {
                //Toast.makeText(this, "Query success+ "+ it.size(), Toast.LENGTH_SHORT).show()
        // if referral code does not match to any other users referral code then query will register the user in database
        // else the function will run recursively until it generates a unique referral code
                if(it.size()==0){
                    addUserDetails0(genCode,referred,referredid)
                }
                else{
                    addUserDetails1(referred,referredid)
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Query failed + $it", Toast.LENGTH_SHORT).show()
                val newCode=randomString()
                addUserDetails0(newCode,referred,referredid)

            }
    }

// this function is to generate the random string of length 5 from a given string
    fun randomString(): String {
        val chars1 = "ABCDEF012GHIJKL345MNOPQR678STUVWXYZ9".toCharArray()
        val sb1 = StringBuilder()
        val random1 = Random()
        for (i in 0..4) {
            val c1 = chars1[random1.nextInt(chars1.size)]
            sb1.append(c1)
        }
        return sb1.toString()
    }
}
package com.example.grofersreferral.activities

import android.app.AlertDialog
import android.content.*
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.example.grofersreferral.R
import com.example.grofersreferral.databinding.ActivityHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
//import kotlinx.android.synthetic.main.activity_register.*
//import kotlinx.android.synthetic.main.drawer_header.*
import java.util.*


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var mAuth=FirebaseAuth.getInstance()
    private lateinit var txtdrawerHeading:TextView
    lateinit var listenerRegistration:ListenerRegistration
    val collectionReference = FirebaseFirestore.getInstance().collection("Users")

    val docReference = FirebaseFirestore.getInstance().collection("Users")
        .document(mAuth.currentUser!!.uid)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpToolBar()
        val header = binding.navigationView.getHeaderView(0)
        txtdrawerHeading = header.findViewById(R.id.txtDrawer_Heading)

        listenerRegistration=docReference.addSnapshotListener { value, error ->
            txtdrawerHeading.text=value!!.getString("name")
            collectionReference.whereEqualTo("referredid",mAuth.currentUser!!.uid).limit(5).get().addOnSuccessListener {
// the mobile number of new user who signup using refrral code is fetched
//  the limit is set up to 5 mobile number( mobile numbers are unique for signup)
                if(it.size()>0){
                    binding.user1.text=it.documents[0].getString("mobileNo")
                }
                if(it.size()>1){
                    binding.user2.text=it.documents[1].getString("mobileNo")
                }
                if(it.size()>2){
                    binding.user3.text=it.documents[2].getString("mobileNo")
                }
                if(it.size()>3){
                    binding.user4.text=it.documents[3].getString("mobileNo")
                }
                if(it.size()>4){
                    binding.user5.text=it.documents[4].getString("mobileNo")
                }
            }
// this will display the user id of user who referred to this current user
            binding.txtReferred.text="You have been referred by: " + value.getString("referredid")
//if user is enrolled in program then the referral code will be visible in home screen page and vice versa
            if(value!!.getBoolean("enrolled")==true){


                binding.txtEnrollmentStatus.text="Enrolled in the Referral Program"


                binding.buttonAction.text="Withdraw from program"
                binding.txtRefCode.visibility=View.VISIBLE
                binding.codeCopy.visibility=View.VISIBLE

//this is the number of people who sign up using the referral code of current user
                binding.txtRefCount.text= "Number of people you have reffered : "+value.getLong("refCount").toString()

                binding.txtRefCode.text= value.getString("refCode")
                binding.codeCopy.setOnClickListener {
                        val clipboard =
                            this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied Text", value.getString("refCode"))
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(this, "referral code Copied", Toast.LENGTH_SHORT).show()
                }
// if current user has signed up using referral code of another user then the incentive is calculated
// by adding 1 to the number of user referred by the current user
                if(value.getBoolean("referred")==true){
                    if((1+value.getLong("refCount")!!)>4){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 500"
                        binding.txtNextTarget.text="You have reached the target of 5 referrals"
                    }else if((1+value.getLong("refCount")!!)>2){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 100"
                        binding.txtNextTarget.text="Your new target is 5 referrals"
                    }
                    else{
                        binding.txtAmountEarned.text= "Current Earnings : Rs 00"
                        binding.txtNextTarget.text="Your target is 3 referrals"
                    }
                }else{
// if current user has NOT signed up using referral code of another user then the incentive is calculated
// by  the number of user referred by the current user
                    if(value.getLong("refCount")!!>4){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 500"
                        binding.txtNextTarget.text="You have reached the target of 5 referrals"
                    }else if(value.getLong("refCount")!!>2){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 100"
                        binding.txtNextTarget.text="Your new target is 5 referrals"
                    }
                    else{
                        binding.txtAmountEarned.text= "Current Earnings : Rs 00"
                        binding.txtNextTarget.text="Your target is 3 referrals"
                    }
                }

// this is the butten for the withdawal from  the referral program if the current status is "enrolled"
                binding.buttonAction.setOnClickListener {
// this is for updating data in firebase
                    val user: MutableMap<String, Any> =
                        HashMap()
                    user["enrolled"] = false
                    docReference.update(user)
                }
            }else{
                binding.txtEnrollmentStatus.text="Withdrawn from the Referral Program"

                binding.buttonAction.text="Enroll in program"



                binding.txtRefCount.text= "Number of people you have reffered : "+value.getLong("refCount").toString()

                binding.txtRefCode.visibility=View.GONE
                binding.codeCopy.visibility=View.GONE


// if current user has signed up using referral code of another user then the incentive is calculated
// by adding 1 to the number of user referred by the current user
                if(value.getBoolean("referred")==true){
                    if((1+value.getLong("refCount")!!)>4){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 500"
                        binding.txtNextTarget.text="You have reached the target of 5 referrals"
                    }else if((1+value.getLong("refCount")!!)>2){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 100"
                        binding.txtNextTarget.text="Your new target is 5 referrals"
                    }
                    else{
                        binding.txtAmountEarned.text= "Current Earnings : Rs 00"
                        binding.txtNextTarget.text="Your target is 3 referrals"
                    }
                }else{
// if current user has NOT signed up using referral code of another user then the incentive is calculated
// by  the number of user referred by the current user
                    if(value.getLong("refCount")!!>4){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 500"
                        binding.txtNextTarget.text="You have reached the target of 5 referrals"
                    }else if(value.getLong("refCount")!!>2){
                        binding.txtAmountEarned.text= "Current Earnings : Rs 100"
                        binding.txtNextTarget.text="Your new target is 5 referrals"
                    }
                    else{
                        binding.txtAmountEarned.text= "Current Earnings : Rs 00"
                        binding.txtNextTarget.text="Your target is 3 referrals"
                    }
                }
// this is the butten for the enrolling into the referral program if the current status is "withdrawn"

                binding.buttonAction.setOnClickListener {
                    val user: MutableMap<String, Any> =
                        HashMap()
                    user["enrolled"] = true
                    docReference.update(user)
                }
            }
        }




        val actionBarDrawerToggle=
            ActionBarDrawerToggle(this@HomeActivity,binding.drawerLayout,
                R.string.open_drawer,
                R.string.close_drawer)
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()


        binding.navigationView.setNavigationItemSelectedListener {

            when(it.itemId){
                R.id.home ->{
                    binding.drawerLayout.closeDrawers()

                }

                R.id.logout ->{

                    AlertDialog.Builder(this@HomeActivity)
                        .setTitle(R.string.confirmation)
                        .setMessage(R.string.log_out_conf)
                        .setCancelable(false)
                        .setPositiveButton(
                            R.string.yes
                        ) { _: DialogInterface, _: Int ->
                            val sharedPreferences1 = getSharedPreferences(getString(R.string.preference_file_name),
                                Context.MODE_PRIVATE
                            )
                            sharedPreferences1.edit().clear().apply()
                            mAuth.signOut()
                            val lastIntent = Intent(this@HomeActivity, LoginActivity::class.java)
                            lastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            lastIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            lastIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(lastIntent)
                            this.finishAffinity()

                        }
                        .setNegativeButton("No", null)
                        .show()
                }

            }
            return@setNavigationItemSelectedListener true
        }


    }


    private fun setUpToolBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title=getString(R.string.app_name)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id=item.itemId
        if(id==android.R.id.home){
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }
    //dismiss keyboard when not in current focus
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }

   override fun onBackPressed() {
       if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
           binding.drawerLayout.closeDrawer(GravityCompat.START)
       } else {
           finishAffinity();
       }
    }



    override fun onResume() {
        super.onResume()
        for (i in 0 until binding.navigationView.menu.size()) {
            if (binding.navigationView.menu.getItem(i).itemId != R.id.home) {
                binding.navigationView.menu.getItem(i).isChecked = false
                binding.navigationView.setCheckedItem(R.id.home)
            } 
        }

    }

    override fun onPause() {
        super.onPause()
        listenerRegistration.remove()
    }





}

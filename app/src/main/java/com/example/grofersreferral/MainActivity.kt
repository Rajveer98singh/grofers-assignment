package com.example.grofersreferral

import android.content.Intent
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.grofersreferral.activities.LoginActivity

class MainActivity : AppCompatActivity() {
    private val mHandler = Handler(Looper.getMainLooper())
    private val mLauncher: Launcher = Launcher()
    override fun onStart() {
        super.onStart()
        mHandler.postDelayed(mLauncher, SPLASH_DELAY.toLong())
    }

    override fun onStop() {
        mHandler.removeCallbacks(mLauncher)
        super.onStop()
    }

    private fun launch() {

        Handler(Looper.getMainLooper()).postDelayed({
            val startAct = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(startAct)
        }, 200)
    }


    private inner class Launcher : Runnable {
        override fun run() {
            launch()
        }
    }

    companion object {
        private const val SPLASH_DELAY = 200
        private const val TAG = "MainActivity"
    }
}
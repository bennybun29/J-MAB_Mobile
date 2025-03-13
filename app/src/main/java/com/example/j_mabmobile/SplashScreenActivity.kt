package com.example.j_mabmobile

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var jmabLogo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        window.navigationBarColor = resources.getColor(R.color.white, theme)

        jmabLogo = findViewById(R.id.jmabLogo)

        // Show splash screen for at least 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserLoginStatus()
        }, 3000)
    }

    private fun checkUserLoginStatus() {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("jwt_token", null)

        if (token != null && isTokenValid()) {
            if (isInternetAvailable()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, NoInternetActivity::class.java))
            }
        } else {
            if (isInternetAvailable()) {
                startActivity(Intent(this, SignUpActivity::class.java))
            } else {
                startActivity(Intent(this, NoInternetActivity::class.java))
            }
        }
        finish()
    }

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
    }

    private fun isTokenValid(): Boolean {
        val sharedPreferences = getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE)
        val expiryTime = sharedPreferences.getLong("token_expiry_time", 0)
        return System.currentTimeMillis() < expiryTime
    }
}

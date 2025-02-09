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

        jmabLogo = findViewById(R.id.jmabLogo)

        // Rotate the image view as a wheel (360 degrees)
        val rotateAnimator = ObjectAnimator.ofFloat(jmabLogo, "rotation", 0f, 360f)
        rotateAnimator.duration = 2000 // Rotate for 2 seconds (you can adjust this duration)

        // Move the wheel off the screen after 3 seconds
        val translateAnimator = ObjectAnimator.ofFloat(jmabLogo, "translationX", 0f, 1000f) // Move 1000px off the screen
        translateAnimator.duration = 1000 // Takes 1 second to move off screen

        // Combine both animations
        rotateAnimator.start()

        // Wait for 2-3 seconds before starting to move off screen
        Handler(Looper.getMainLooper()).postDelayed({
            translateAnimator.start()
        }, 2000) // Delay for 2 seconds before it starts translating off the screen

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

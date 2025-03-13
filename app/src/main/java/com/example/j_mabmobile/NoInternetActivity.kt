package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class NoInternetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_internet)

        window.navigationBarColor = resources.getColor(R.color.j_mab_blue, theme)

        val retryButton: Button = findViewById(R.id.retryButton)
        retryButton.setOnClickListener {
            startActivity(Intent(this, SplashScreenActivity::class.java))
            finish()
        }
    }
}

package com.example.j_mabmobile

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class AccountAndSecurityActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the CartActivity
        setContentView(R.layout.activity_account_and_activity)

        backBtn = findViewById(R.id.backButton)

        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
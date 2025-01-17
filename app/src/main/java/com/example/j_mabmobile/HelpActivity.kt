package com.example.j_mabmobile

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class HelpActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the CartActivity
        setContentView(R.layout.activity_help)

        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener {
            onBackPressed()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}
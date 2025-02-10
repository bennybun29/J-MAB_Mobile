package com.example.j_mabmobile

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class CheckoutActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        backBtn = findViewById(R.id.backBtn)

        backBtn.setOnClickListener({
            onBackPressed()
        })

    }
}
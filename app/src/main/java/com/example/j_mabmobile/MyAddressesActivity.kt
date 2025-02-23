package com.example.j_mabmobile

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MyAddressesActivity : AppCompatActivity() {

    lateinit var backBtn: ImageButton
    lateinit var addAddressBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for the CartActivity
        setContentView(R.layout.activity_my_addresses)

        backBtn = findViewById(R.id.backButton)
        addAddressBtn = findViewById(R.id.addAddressBtn)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        addAddressBtn.setOnClickListener{
            onPause()
            val intent = Intent(this, NewAddressActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}